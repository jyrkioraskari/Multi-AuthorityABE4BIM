package fi.aalto.lbd;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.aalto.lbd.lib.AaltoABEActor;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import sg.edu.ntu.sce.sands.crypto.dcpabe.Ciphertext;
import sg.edu.ntu.sce.sands.crypto.dcpabe.DCPABE;
import sg.edu.ntu.sce.sands.crypto.dcpabe.Message;
import sg.edu.ntu.sce.sands.crypto.dcpabe.PersonalKeys;
import sg.edu.ntu.sce.sands.crypto.dcpabe.PublicKeys;
import sg.edu.ntu.sce.sands.crypto.dcpabe.ac.AccessStructure;
import sg.edu.ntu.sce.sands.crypto.dcpabe.key.PublicKey;
import sg.edu.ntu.sce.sands.crypto.utility.Utility;

public class AaltoABEPublisher extends AaltoABEActor {
	protected File pks_file;
	protected PublicKeys pks = new PublicKeys();

	public AaltoABEPublisher(String global_parameters_hash) {
		super(global_parameters_hash);
		String name = ipfs.getID();
		this.pks_file = new File(this.security_directory + "P_" + name + ".json");
		if (pks_file.exists()) {
			try {
				String contents = new String(Files.readAllBytes(Paths.get(this.pks_file.toURI())));
				pks = new ObjectMapper().readerFor(PublicKeys.class).readValue(contents);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public AaltoABEPublisher(String global_parameters_hash, boolean readFile) {
		super(global_parameters_hash);
		String name = ipfs.getID();
		this.pks_file = new File(this.security_directory + "P_" + name + ".json");
		if (readFile) {
			if (pks_file.exists()) {
				try {
					String contents = new String(Files.readAllBytes(Paths.get(this.pks_file.toURI())));
					pks = new ObjectMapper().readerFor(PublicKeys.class).readValue(contents);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public AaltoABEPublisher(String global_parameters_hash, String name) {
		super(global_parameters_hash);
		this.pks_file = new File(this.security_directory + "P_" + name + ".ser");
		if (pks_file.exists()) {
			try {
				String contents = new String(Files.readAllBytes(Paths.get(this.pks_file.toURI())));
				pks = new ObjectMapper().readerFor(PublicKeys.class).readValue(contents);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean getStatus() {
		if (pks == null)
			return false;
		else
			return true;

	}

	public boolean addAuthorityPublicKeys(Map<String, PublicKey> keysmap) {
		pks.subscribeAuthority(keysmap);
		try {
			String json_string = new ObjectMapper().writeValueAsString(pks);
			System.out.println(json_string);
			Files.write(Paths.get(this.pks_file.toURI()), json_string.getBytes());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	public void addAuthorityPublicKeyString(String key_base64) {
		System.out.println("add a key: " + key_base64);
		try (ByteArrayInputStream bis = new ByteArrayInputStream(key_base64.getBytes());
				ObjectInputStream oIn = new ObjectInputStream(MimeUtility.decode(bis, "base64"))) {
			Map<String, PublicKey> keysmap = (Map<String, PublicKey>) oIn.readObject();
			addAuthorityPublicKeys(keysmap);
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		} catch (MessagingException e1) {
			e1.printStackTrace();
		}
	}

	public boolean addAuthorityPublicKeyFile(String apFileP) {
		try {
			pks.subscribeAuthority(Utility.readPublicKeys(this.security_directory + apFileP));
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
			return false;
		}
		try {
			String json_string = new ObjectMapper().writeValueAsString(pks);
			System.out.println(json_string);
			Files.write(Paths.get(this.pks_file.toURI()), json_string.getBytes());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	public class Save_result
	{
		public String ipfs_hash;
		public long encryption_size;
		public Save_result(String ipfs_hash, long encryption_Size) {
			super();
			this.ipfs_hash = ipfs_hash;
			this.encryption_size = encryption_Size;
		}
	}
	/*
	 * base64 since IPFS.add/cat does not allow binary
	 */

	public Save_result encryptABEAES_save(String content, String policy) {
		try {
			AccessStructure arho = AccessStructure.buildFromPolicy(policy);
			Message m = DCPABE.generateRandomMessage(gp.get());
			Ciphertext ct;
			try {
				ct = DCPABE.encrypt(m, arho, gp.get(), pks);
			} catch (Exception e) {
				System.err.println("Encryption was not done. Check the policy.");
				e.printStackTrace();
				return null;
			}

			try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ObjectOutputStream out = new ObjectOutputStream(MimeUtility.encode(bos, "base64"));

					ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes());
					BufferedInputStream in = new BufferedInputStream(bis);) {
				out.writeObject(ct);

				PaddedBufferedBlockCipher aes = Utility.initializeAES(m.getM(), true);

				encryptOrDecryptPayload(aes, bis, out);
				out.flush();
				NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper("gp", bos.toByteArray());
				List<MerkleNode> node = ipfs.add(file);
				if (node.isEmpty()) {
					System.err.println("IPFS Node not created.");
					return null;
				}
				System.out.println("File hash: " + node.get(0).hash.toBase58());
				return new Save_result(node.get(0).hash.toBase58(),bos.size());
				
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		} catch (IOException | DataLengthException | InvalidCipherTextException | IllegalStateException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected void encryptOrDecryptPayload(PaddedBufferedBlockCipher cipher, InputStream is, OutputStream os)
			throws DataLengthException, IllegalStateException, InvalidCipherTextException, IOException {
		byte[] inBuff = new byte[cipher.getBlockSize()];
		byte[] outBuff = new byte[cipher.getOutputSize(inBuff.length)];
		int nbytes;
		while (-1 != (nbytes = is.read(inBuff, 0, inBuff.length))) {
			int length1 = cipher.processBytes(inBuff, 0, nbytes, outBuff, 0);
			os.write(outBuff, 0, length1);
		}
		nbytes = cipher.doFinal(outBuff, 0);
		os.write(outBuff, 0, nbytes);
	}

	
	public Save_result encryptABE_save(String content, String policy) {
		try {
			AccessStructure arho = AccessStructure.buildFromPolicy(policy);
			Message m = new Message(content.getBytes());
			System.out.println("Message content "+(m.getM().length==content.length())+" "+content.length());
			Ciphertext ct;
			try {
				ct = DCPABE.encrypt(m, arho, gp.get(), pks);
			} catch (Exception e) {
				System.err.println("Encryption was not done. Check the policy.");
				e.printStackTrace();
				return null;
			}
			
			try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ObjectOutputStream out = new ObjectOutputStream(MimeUtility.encode(bos, "base64"));

					ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes());
					BufferedInputStream in = new BufferedInputStream(bis);) {
				out.writeObject(ct);
				out.flush();
				System.out.println("bos size: "+bos.size()+" -"+bos.toByteArray().length);
				NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper("gp", bos.toByteArray());
				List<MerkleNode> node = ipfs.add(file);
				if (node.isEmpty()) {
					System.err.println("IPFS Node not created.");
					return null;
				}
				System.out.println("File hash: " + node.get(0).hash.toBase58());
				return new Save_result(node.get(0).hash.toBase58(),bos.toByteArray().length);
				
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		} catch (IOException | DataLengthException | IllegalStateException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {

		final String policy = "and a b";

		AaltoABEPublisher publisher = new AaltoABEPublisher("1");
		publisher.addAuthorityPublicKeyFile("authority_1.pk");
		publisher.addAuthorityPublicKeyFile("authority_2.pk");
		// The problem here is the salt!! Every time there is a new file! Should we keep
		// it static?

		publisher.encryptABEAES_save("Content", policy);
	}

}
