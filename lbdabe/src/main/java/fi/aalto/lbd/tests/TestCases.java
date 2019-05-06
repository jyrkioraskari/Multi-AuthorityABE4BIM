package fi.aalto.lbd.tests;

import java.io.File;

import fi.aalto.lbd.AaltoABEGlobal;

public class TestCases {
	public TestCases() {
		AaltoABEGlobal.global_parameters_hash="QmeWS5fzqUdRTVA2rXD3Gt4bo4o3bL6du7uh14yquhDzy6";
		//deleteDirectory(new File(Tests.secure_directory));

		// Use case 1
		Publisher A_ltd = new Publisher("A ltd");

		String[] attribute_B_ltd = { "B.works_for" };
		Authenticator B_ltd = new Authenticator("B ltd", attribute_B_ltd);
		String B_ltd_key = B_ltd.getPublicKeys();
		A_ltd.addAuthorityKey(B_ltd_key);

		String[] attribute_C_ltd = { "C.works_for" };
		Authenticator C_ltd = new Authenticator("C ltd", attribute_C_ltd);
		String C_ltd_key = C_ltd.getPublicKeys();
		A_ltd.addAuthorityKey(C_ltd_key);

		User Bob = new User("Bob");
		B_ltd.userKeyGen(Bob, "B.works_for");

		User Claudia = new User("Claudia");
		C_ltd.userKeyGen(Claudia, "C.works_for");

		final String policy_Area1 = "B.works_for";
		String secret_message1_hash = A_ltd.encrypt("BIM.model.area1", policy_Area1);
		final String policy_Area2 = "C.works_for";
		String secret_message2_hash = A_ltd.encrypt("BIM.model.area2", policy_Area2);

		String bob_message = Bob.decrypt(secret_message1_hash);
		System.out.println("Bob message: " + bob_message);

		String claudia_message = Claudia.decrypt(secret_message2_hash);
		System.out.println("Claudia message: " + claudia_message);

		// Use case 2		
		String[] attribute_D_ltd = { "D.works_for" };
		Authenticator D_ltd = new Authenticator("D ltd", attribute_D_ltd);
		String D_ltd_key = D_ltd.getPublicKeys();
		A_ltd.addAuthorityKey(D_ltd_key);

		User David = new User("David");
		D_ltd.userKeyGen(David, "D.works_for");
		final String policy_Area1_forD = "D.works_for";
		String secret_message3_hash = A_ltd.encrypt("BIM.model.area1", policy_Area1_forD);
		String david_message = David.decrypt(secret_message3_hash);
		System.out.println("David message: " + david_message);		
		
		// Use case 3
		Publisher PC_ltd = new Publisher("C ltd");
		String attr_works_for_Windows= "Windows.works_for";
		String[] attribute_Windows_ltd = { attr_works_for_Windows };
		Authenticator Windows_ltd = new Authenticator("Windows ltd", attribute_Windows_ltd);
		String Windows_ltd_key = Windows_ltd.getPublicKeys();
		PC_ltd.addAuthorityKey(Windows_ltd_key);
		
		final String policy_Area1_forWindows = "Windows.works_for";
		String windows_model=claudia_message; // Filtering
		String secret_message_hash3 = PC_ltd.encrypt(windows_model, policy_Area1_forWindows);
		
		User William = new User("William");
		Windows_ltd.userKeyGen(William, attr_works_for_Windows);
		String william_message = William.decrypt(secret_message_hash3);
		System.out.println("William message: " + william_message);		
		
		// wait&force threads to stop
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.exit(1);
	}

	// from: https://www.baeldung.com/java-delete-directory
	boolean deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}

	public static void main(String[] args) {
		new TestCases();
	}
}
