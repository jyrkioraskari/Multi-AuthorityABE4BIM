package fi.aalto.lbd.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Base64;

import fi.aalto.lbd.AaltoABEGlobal;
import fi.aalto.lbd.AaltoABEPublisher;
import fi.aalto.lbd.lib.AaltoIPFSConnection;
import io.ipfs.multihash.Multihash;

public class Publisher {
	private final AaltoIPFSConnection ipfs = AaltoIPFSConnection.getInstance();
	private final AaltoABEPublisher publisher;
	private IPFS_Subscription sub;


	public Publisher() {
		this.publisher = new AaltoABEPublisher(AaltoABEGlobal.global_parameters_hash);
	}

	public Publisher(String name) {
		this.publisher = new AaltoABEPublisher(AaltoABEGlobal.global_parameters_hash, name);
	}

	public Publisher(String name,boolean hasPubSub) {
		this.publisher = new AaltoABEPublisher(AaltoABEGlobal.global_parameters_hash, name);
		if(hasPubSub)
		{
			sub=new IPFS_Subscription("bim.cerificate");
		}
	}

	public String encrypt(String content, String policy) {
		String hash = publisher.encrypt_save(content, policy);
		return hash;
	}
	
	public String encryptAndPublish(String content, String policy) {
		String hash = publisher.encrypt_save(content, policy);
		if (hash != null) {
			try {
				ipfs.getIpfs().get().pubsub.pub("bim.messages",
						URLEncoder.encode(hash, "UTF-8"));
				return hash;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
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
						try {
							String cert_hash=URLDecoder.decode(msg, "UTF-8");
							Multihash filePointer = Multihash.fromBase58(cert_hash);
							String authorizatorPublicKey = new String(ipfs.getIpfs().get().cat(filePointer));
							publisher.addAuthorityPublicKeyString(authorizatorPublicKey);
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

   public void addAuthorityKey(String key_base64) {
	   publisher.addAuthorityPublicKeyString(key_base64);
   }

}
