package fi.aalto.lbd.tests;

import sg.edu.ntu.sce.sands.crypto.dcpabe.AuthorityKeys;
import sg.edu.ntu.sce.sands.crypto.dcpabe.Ciphertext;
import sg.edu.ntu.sce.sands.crypto.dcpabe.DCPABE;
import sg.edu.ntu.sce.sands.crypto.dcpabe.GlobalParameters;
import sg.edu.ntu.sce.sands.crypto.dcpabe.Message;
import sg.edu.ntu.sce.sands.crypto.dcpabe.PersonalKeys;
import sg.edu.ntu.sce.sands.crypto.dcpabe.PublicKeys;
import sg.edu.ntu.sce.sands.crypto.dcpabe.ac.AccessStructure;

public class PolicyTest1 {

	public static void main(String[] args) {
		
		String test_message="Aalto Iconic lab tests";
		
		GlobalParameters gp = DCPABE.globalSetup(160);
        PublicKeys publicKeys = new PublicKeys();

        AuthorityKeys authority1 = DCPABE.authoritySetup("a1", gp, "a", "b","c","d");
        publicKeys.subscribeAuthority(authority1.getPublicKeys());

        PersonalKeys pkeys = new PersonalKeys("Alice");
        pkeys.addKey(DCPABE.keyGen("Alice", "a", authority1.getSecretKeys().get("a"), gp));
        pkeys.addKey(DCPABE.keyGen("Alice", "b", authority1.getSecretKeys().get("b"), gp));
        pkeys.addKey(DCPABE.keyGen("Alice", "c", authority1.getSecretKeys().get("c"), gp));
        pkeys.addKey(DCPABE.keyGen("Alice", "d", authority1.getSecretKeys().get("d"), gp));

        AccessStructure as = AccessStructure.buildFromPolicy("and a and b and c d");

        Message message = new Message(test_message.getBytes());
        
        Ciphertext ct = DCPABE.encrypt(message, as, gp, publicKeys);

        Message dmessage = DCPABE.decrypt(ct, pkeys, gp);
        
        System.out.println("Done: "+new String(dmessage.getM()).trim());
	}

}
