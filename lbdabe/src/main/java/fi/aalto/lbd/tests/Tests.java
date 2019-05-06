package fi.aalto.lbd.tests;

import java.io.File;

public class Tests {

	public Tests() {
		//deleteDirectory(new File(security_base));
		
		//Case 1
		String[] attributes1 = { "B", "" };
		String[] attributes2 = { "c", "d" };
		final String policy = "and a b";
		Publisher p1 = new Publisher( "1",true); // 1st since of online pubsub
		Authenticator a1 = new Authenticator("1", attributes1,true);
		System.out.println(a1.getAttributes());
		
		//Authority a2 = new Authority(security_base, "1", attributes1,true);
		User u1 = new User("1");
		a1.userKeyGen(u1, "a");
		a1.userKeyGen(u1, "b");
		p1.encrypt("Content1", policy);
		p1.encrypt("Content2", policy);
		
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
		new Tests();
	}
}
