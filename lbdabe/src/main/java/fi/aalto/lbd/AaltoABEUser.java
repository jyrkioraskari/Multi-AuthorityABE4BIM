package fi.aalto.lbd;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.aalto.lbd.lib.AaltoABEActor;
import io.ipfs.multihash.Multihash;
import sg.edu.ntu.sce.sands.crypto.dcpabe.Ciphertext;
import sg.edu.ntu.sce.sands.crypto.dcpabe.DCPABE;
import sg.edu.ntu.sce.sands.crypto.dcpabe.Message;
import sg.edu.ntu.sce.sands.crypto.dcpabe.PersonalKeys;
import sg.edu.ntu.sce.sands.crypto.dcpabe.key.PersonalKey;
import sg.edu.ntu.sce.sands.crypto.utility.Utility;

public class AaltoABEUser extends AaltoABEActor {
	private final File user_keys_serialization_file;

	private PersonalKeys pks;
	private String name;

	public AaltoABEUser(String global_parameters_hash) {
		super(global_parameters_hash);
		System.out.println("user start");
		this.name = ipfs.getID();
		System.out.println(name);

		this.pks = new PersonalKeys(name);
		this.user_keys_serialization_file = new File(this.security_directory + "U_" + name + ".json");

		if (user_keys_serialization_file.exists()) {
			try {
				String contents = new String(Files.readAllBytes(Paths.get(user_keys_serialization_file.toURI())));
				pks = new ObjectMapper().readerFor(PersonalKeys.class).readValue(contents);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public AaltoABEUser(String global_parameters_hash, String name) {
		super(global_parameters_hash);
		System.out.println("user start");
		this.name = name;
		System.out.println(name);
		this.pks = new PersonalKeys(name);
		this.user_keys_serialization_file = new File(this.security_directory + "U_" + name + ".ser");

		if (user_keys_serialization_file.exists()) {
			System.out.println("read in  user 2");
			try {
				String contents = new String(Files.readAllBytes(Paths.get(user_keys_serialization_file.toURI())));
				pks = new ObjectMapper().readerFor(PersonalKeys.class).readValue(contents);

			} catch (IOException e) {
				e.printStackTrace();
			}

			for (String key : pks.getAttributes())
				System.out.println("attribute: " + key);
		}
	}

	public boolean getStatus() {
		if (pks == null)
			return false;
		else
			return true;

	}

	public boolean addPrivateKeyString(String key_base64) {
		if (key_base64 == null) {
			System.err.println("Key_base64 not set");
			return false;
		}

		try (ByteArrayInputStream bis = new ByteArrayInputStream(key_base64.getBytes());
				ObjectInputStream oIn = new ObjectInputStream(MimeUtility.decode(bis, "base64"))) {
			PersonalKey pkey = (PersonalKey) oIn.readObject();
			pks.addKey(pkey);
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
			return false;
		} catch (MessagingException e1) {
			e1.printStackTrace();
		}

		try {
			String json_string = new ObjectMapper().writeValueAsString(pks);
			System.out.println(json_string);
			Files.write(Paths.get(user_keys_serialization_file.toURI()), json_string.getBytes());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	public boolean addKeyFile(String keyFile) {
		try {
			pks.addKey(Utility.readPersonalKey(this.security_directory + keyFile));
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
			return false;
		}

		try {
			String json_string = new ObjectMapper().writeValueAsString(pks);
			System.out.println(json_string);
			Files.write(Paths.get(user_keys_serialization_file.toURI()), json_string.getBytes());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	public String decrypt(String content_hash) {

		Multihash filePointer = Multihash.fromBase58(content_hash);
		try (ByteArrayInputStream bis = new ByteArrayInputStream(ipfs.cat(filePointer));
				ObjectInputStream oIn = new ObjectInputStream(MimeUtility.decode(bis, "base64"))) {

			Ciphertext ct = Utility.readCiphertext(oIn);
			Message m;
			try {
				m = DCPABE.decrypt(ct, pks, gp.get());
			} catch (IllegalArgumentException e) {
				System.err.println("\nDecryption was not done. Check the user attributes.");
				return null;
			}

			PaddedBufferedBlockCipher aes = Utility.initializeAES(m.getM(), false);

			try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
					BufferedOutputStream out = new BufferedOutputStream(bos)) {
				encryptOrDecryptPayload(aes, oIn, out);

				out.flush();
				return new String(bos.toByteArray());
			} catch (InvalidCipherTextException e) {
				System.err.println("Global parameter missmatch/Authenticator regenerated?: " + e.getMessage() + " " + e.getClass().getName());
				e.printStackTrace();
				try {
					String content = new String(ipfs.cat(filePointer));
					System.out.println("1. tried to decrypt: " + content);
					System.exit(1);
				} catch (TimeoutException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
			return null;
		} catch (IOException | ClassNotFoundException | DataLengthException | IllegalStateException e) {
			System.err.println("Global parameter missmatch / Authenticator regenerated?: " + e.getMessage() + " " + e.getClass().getName());

			
			try {
				String content = new String(ipfs.cat(filePointer));
				System.out.println("2. tried to decrypt: " + content);
				System.exit(1);
			} catch (TimeoutException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			e.printStackTrace();
			return null;
		} catch (MessagingException e1) {
			e1.printStackTrace();
			return null;
		} catch (java.lang.NullPointerException e1) {
			e1.printStackTrace();
			return null;
		} catch (TimeoutException e2) {
			System.out.println("IPFS hash not found in 15 sec");
			return null;
		}
	}

	private void encryptOrDecryptPayload(PaddedBufferedBlockCipher cipher, InputStream is, OutputStream os)
			throws DataLengthException, IllegalStateException, InvalidCipherTextException, IOException {
		byte[] inBuff = new byte[cipher.getBlockSize()];
		byte[] outBuff = new byte[cipher.getOutputSize(inBuff.length)];
		int nbytes;
		while (-1 != (nbytes = is.read(inBuff, 0, inBuff.length))) {
			int length1 = cipher.processBytes(inBuff, 0, nbytes, outBuff, 0);
			os.write(outBuff, 0, length1);
		}
		System.out.println("outlen: "+outBuff.length);
		System.out.println("bs: "+cipher.getBlockSize());
		try {
		nbytes = cipher.doFinal(outBuff, 0);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("castle nbytes: "+nbytes);
		os.write(outBuff, 0, nbytes);
	}

	public String getName() {
		return name;
	}

	/*
	 * For simulation made up names (not the IPFS IDs could ne used)
	 */
	public static void main(String[] args) {
		AaltoABEUser user1 = new AaltoABEUser(AaltoABEGlobal.global_parameters_hash, "c:\\jo\\security\\");
		user1.addKeyFile("A1_" + user1.getName() + "_a.key");
		user1.addKeyFile("A1_" + user1.getName() + "_b.key");
		user1.decrypt("QmR7tPquSTUka3Mb35szHwJ6Ut7BgFu91AUkqnR9xP4wdG");
	}

}
