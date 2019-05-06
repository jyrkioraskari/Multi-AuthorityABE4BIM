package fi.aalto.lbd.tests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

import sg.edu.ntu.sce.sands.crypto.dcpabe.AuthorityKeys;
import sg.edu.ntu.sce.sands.crypto.dcpabe.Ciphertext;
import sg.edu.ntu.sce.sands.crypto.dcpabe.DCPABE;
import sg.edu.ntu.sce.sands.crypto.dcpabe.GlobalParameters;
import sg.edu.ntu.sce.sands.crypto.dcpabe.Message;
import sg.edu.ntu.sce.sands.crypto.dcpabe.PersonalKeys;
import sg.edu.ntu.sce.sands.crypto.dcpabe.PublicKeys;
import sg.edu.ntu.sce.sands.crypto.dcpabe.ac.AccessStructure;

public class EncryptionTestPolicy {

	public static void main(String[] args) {
	String test_message="Aalto Iconic lab tests";
		
		GlobalParameters gp = DCPABE.globalSetup(160);
        PublicKeys publicKeys = new PublicKeys();

        AuthorityKeys authority1 = DCPABE.authoritySetup("Sub contractor_s1", gp, "works_for_s1", "sub_project1");
        publicKeys.subscribeAuthority(authority1.getPublicKeys());

        PersonalKeys pkeys = new PersonalKeys("Alice");
        pkeys.addKey(DCPABE.keyGen("Alice", "works_for_s1", authority1.getSecretKeys().get("works_for_s1"), gp));
        pkeys.addKey(DCPABE.keyGen("Alice", "sub_project1", authority1.getSecretKeys().get("sub_project1"), gp));

        AccessStructure as = AccessStructure.buildFromPolicy("and works_for_s1 sub_project1");

        Message message = new Message("Test message1".getBytes());        
        Ciphertext ct1 = DCPABE.encrypt(message, as, gp, publicKeys);
        Ciphertext ct2 = DCPABE.encrypt(message, as, gp, publicKeys);
        
        try (
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream( MimeUtility.encode(bos, "base64"));
		) {
			out.writeObject(ct1);
			out.flush();
			System.out.println("c1: "+bos.toString());
        } catch (IOException e) {
			e.printStackTrace();
		} catch (MessagingException e1) {
			e1.printStackTrace();
		}
        
        try (
 				ByteArrayOutputStream bos = new ByteArrayOutputStream();
 				ObjectOutputStream out = new ObjectOutputStream( MimeUtility.encode(bos, "base64"));
 		) {
 			out.writeObject(ct2);
 			out.flush();
 			System.out.println("c2: "+bos.toString());
         } catch (IOException e) {
 			e.printStackTrace();
 		} catch (MessagingException e1) {
 			e1.printStackTrace();
 		}
        Message dmessage = DCPABE.decrypt(ct1, pkeys, gp);
        
        
        System.out.println("Done: "+new String(dmessage.getM()));

	}

}
