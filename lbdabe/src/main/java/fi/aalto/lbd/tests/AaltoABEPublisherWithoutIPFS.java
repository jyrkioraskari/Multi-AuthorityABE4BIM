package fi.aalto.lbd.tests;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.aalto.lbd.AaltoABEPublisher;
import sg.edu.ntu.sce.sands.crypto.dcpabe.Ciphertext;
import sg.edu.ntu.sce.sands.crypto.dcpabe.DCPABE;
import sg.edu.ntu.sce.sands.crypto.dcpabe.Message;
import sg.edu.ntu.sce.sands.crypto.dcpabe.PublicKeys;
import sg.edu.ntu.sce.sands.crypto.dcpabe.ac.AccessStructure;
import sg.edu.ntu.sce.sands.crypto.utility.Utility;

public class AaltoABEPublisherWithoutIPFS extends AaltoABEPublisher {
	public AaltoABEPublisherWithoutIPFS(String global_parameters_hash) {
		super(global_parameters_hash);
	}

	public AaltoABEPublisherWithoutIPFS(String global_parameters_hash, boolean readFile) {
		super(global_parameters_hash,readFile);
	}

	public AaltoABEPublisherWithoutIPFS(String global_parameters_hash, String name) {
		super(global_parameters_hash,name);
	}


	public Save_result encrypt_save(String content, String policy) {
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
				return new Save_result("q1234",content.length());
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		} catch (IOException | DataLengthException | InvalidCipherTextException | IllegalStateException e) {
			e.printStackTrace();
		}
		return null;
	}


}
