package fi.aalto.lbd;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.aalto.lbd.lib.AaltoABEActor;
import sg.edu.ntu.sce.sands.crypto.dcpabe.AuthorityKeys;
import sg.edu.ntu.sce.sands.crypto.dcpabe.DCPABE;
import sg.edu.ntu.sce.sands.crypto.dcpabe.key.PersonalKey;
import sg.edu.ntu.sce.sands.crypto.dcpabe.key.SecretKey;
import sg.edu.ntu.sce.sands.crypto.utility.Utility;

public class AaltoABEAuthenticator extends AaltoABEActor {
	private String authority_file;
	private final String name;
	private AuthorityKeys ak;

	public AaltoABEAuthenticator(String global_parameters_hash, String[] attrs) {
		super(global_parameters_hash);
		System.out.println("Authority:");
		this.name = this.ipfs.getID();
		this.authority_file = this.security_directory + "authority_" + name + ".json";

		if ((new File(authority_file).exists())) {
			readAuthorityFileSerialization(authority_file);
		} else {
			createAndSave(attrs);
		}
	}

	public AaltoABEAuthenticator(String global_parameters_hash, String[] attrs, boolean readFile) {
		super(global_parameters_hash);
		System.out.println("Authority:");
		this.name = this.ipfs.getID();
		this.authority_file = this.security_directory + "authority_" + name + ".json";
		if (readFile) {
			if ((new File(authority_file).exists())) {
				readAuthorityFileSerialization(authority_file);
			} else {
				createAndSave(attrs);
			}
		} else {
			createAndSave(attrs);
		}

	}

	public AaltoABEAuthenticator(String global_parameters_hash, String name, String[] attrs) {
		super(global_parameters_hash);
		this.name = name;
		this.authority_file = this.security_directory + "authority_" + name + ".json";

		if ((new File(this.security_directory + authority_file)).exists()) {
			readAuthorityFileSerialization(authority_file);
		} else {
			createAndSave(attrs);
		}
	}
	
	public AaltoABEAuthenticator(String global_parameters_hash, String name, String[] attrs, boolean readFile) {
		super(global_parameters_hash);
		this.name = name;
		this.authority_file = this.security_directory + "authority_" + name + ".json";
		if (readFile) {
			if ((new File(authority_file).exists())) {
				readAuthorityFileSerialization(authority_file);
			} else {
				createAndSave(attrs);
			}
		} else {
			createAndSave(attrs);
		}
	}

	public AaltoABEAuthenticator(String global_parameters_hash, String name) {
		super(global_parameters_hash);
		this.authority_file = this.security_directory + "authority_" + name + ".json";
		this.name = name;
		readAuthorityFileSerialization(authority_file);
	}

	public AaltoABEAuthenticator(String global_parameters_hash) {
		super(global_parameters_hash);
		this.name = this.ipfs.getID();
		this.authority_file = this.security_directory + "authority_" + name + ".json";
		readAuthorityFileSerialization(authority_file);

	}

	private void createAndSave(String[] attrs) {
		System.out.println("Create Authority");
		this.ak = DCPABE.authoritySetup("authority_" + name, gp.get(), attrs);

		try {
			String json_string = new ObjectMapper().writeValueAsString(this.ak);
			System.out.println(json_string);
			Files.write(Paths.get(new File(authority_file).toURI()), json_string.getBytes());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readAuthorityFileSerialization(String authority_file) {
		try {
			String contents = new String(Files.readAllBytes(Paths.get((new File(authority_file)).toURI())));
			this.ak = new ObjectMapper().readerFor(AuthorityKeys.class).readValue(contents);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String userKeyGenBase64String(String username, String attr) {
		try {
			if (ak == null) {
				System.err.println("AuthorityKeys not set.");
				return null;
			}
			SecretKey sk = ak.getSecretKeys().get(attr);

			if (null == sk) {
				System.err.println("Attribute does not exist.");
				return null;
			}

			PersonalKey pk = DCPABE.keyGen(username, attr, sk, gp.get());
			try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ObjectOutputStream out = new ObjectOutputStream(MimeUtility.encode(bos, "base64"));) {
				out.writeObject(pk);
				out.flush();
				return bos.toString();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
		return null;
	}

	public boolean userKeyGenFile(String username, String attr) {
		try {
			SecretKey sk = ak.getSecretKeys().get(attr);

			if (null == sk) {
				System.err.println("Attribute does not exist.");
				return false;
			}

			PersonalKey pk = DCPABE.keyGen(username, attr, sk, gp.get());
			Utility.writePersonalKey(this.security_directory + "A" + this.name + "_" + username + "_" + attr + ".key",
					pk);
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
		return true;
	}

	public AuthorityKeys getAk() {
		return ak;
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		String security_base = "c:\\jo\\security\\";

		String[] attributes1 = { "a", "b" };
		AaltoABEAuthenticator author1 = new AaltoABEAuthenticator(AaltoABEGlobal.global_parameters_hash, "1",
				attributes1);

		author1.userKeyGenFile("QmTQfAMNNXYeoy92YvWTwSWbUgmQcLQpN8zxwdVExF38NQ", "a");
		author1.userKeyGenFile("QmTQfAMNNXYeoy92YvWTwSWbUgmQcLQpN8zxwdVExF38NQ", "b");

		String[] attributes2 = { "c", "d" };
		new AaltoABEAuthenticator(security_base, "2", attributes2);
		AaltoABEAuthenticator author2 = new AaltoABEAuthenticator(AaltoABEGlobal.global_parameters_hash, "2");
		// author2.userKeyGen("user2", "c");
		// author2.userKeyGen("user2", "bd); */

	}

}
