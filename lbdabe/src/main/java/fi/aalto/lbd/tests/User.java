package fi.aalto.lbd.tests;

import java.util.Base64;

import fi.aalto.lbd.AaltoABEGlobal;
import fi.aalto.lbd.AaltoABEUser;
import fi.aalto.lbd.lib.AaltoIPFSConnection;

public class User {
	private final AaltoIPFSConnection ipfs = AaltoIPFSConnection.getInstance();
	private final AaltoABEUser abe_user;
	private String name;

	private class IPFS_Subscription extends Thread
	{
		String channel;
		public IPFS_Subscription(String channel)
		{
			this.channel=channel;
			this.start();
		}
		public void run()
		{
			// Public subscribe!
			try {
				ipfs.getIpfs().get().pubsub.sub(this.channel).forEach(lhm->{
					Object encoded_data = lhm.get("data");
					if (encoded_data != null) {
						byte[] decoded = Base64.getDecoder().decode(encoded_data.toString());
						String msg = new String(decoded);
						System.out.println("Message hash was: "+msg);
						abe_user.decrypt(msg);
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public User(String name)
	{
		this.name=name;
		abe_user=new AaltoABEUser(AaltoABEGlobal.global_parameters_hash,name);
		new  IPFS_Subscription("bim.messages");
	}
	public boolean addPrivateKeyString(String key_base64) {
		 return abe_user.addPrivateKeyString(key_base64);
	}
	
	// Should this be a file or a message???
	public boolean addKeyFile(String keyFile) 
	{
	  return abe_user.addKeyFile(keyFile);
	}


	public String getName() {
		return name;
	}
	
	public String decrypt(String content_hash) {
		return abe_user.decrypt(content_hash);
	}
	
}
