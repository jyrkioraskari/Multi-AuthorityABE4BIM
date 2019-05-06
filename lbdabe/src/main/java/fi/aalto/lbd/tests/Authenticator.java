package fi.aalto.lbd.tests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

import fi.aalto.lbd.AaltoABEAuthenticator;
import fi.aalto.lbd.AaltoABEGlobal;
import fi.aalto.lbd.lib.AaltoIPFSConnection;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import sg.edu.ntu.sce.sands.crypto.dcpabe.AuthorityKeys;

public class Authenticator {
	private final AaltoIPFSConnection ipfs = AaltoIPFSConnection.getInstance();
	private final AaltoABEAuthenticator author;
	
	public Authenticator(String name,String[] attrs)
	{
		 author=new AaltoABEAuthenticator(AaltoABEGlobal.global_parameters_hash,name, attrs);
		 publishPublicKeys2IPFS(author.getAk());
	}
	
	public Authenticator(String name,String[] attrs,boolean hasPubSub)
	{
		 author=new AaltoABEAuthenticator(AaltoABEGlobal.global_parameters_hash,name, attrs);
		 if(hasPubSub)
		   publishPublicKeys2IPFS(author.getAk());
	}
	
	public boolean userKeyGen(User user, String attr) {
		String key_base64=author.userKeyGenBase64String(user.getName(), attr);
		user.addPrivateKeyString(key_base64);
		return key_base64!=null;
	}
	
	private void publishPublicKeys2IPFS(AuthorityKeys ak) {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(MimeUtility.encode(bos, "base64"));) {
			out.writeObject(ak.getPublicKeys());
			out.flush();
			System.out.println("pub: " + bos.toString());
			NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper("certificate",
					bos.toByteArray());
			List<MerkleNode> node = ipfs.add(file);
			if (node.isEmpty()) {
				System.err.println("IPFS Node not created.");
				return;
			}
			System.out.println("IPFS hash: " + node.get(0).hash.toBase58());

			ipfs.getIpfs().get().pubsub.pub("bim.cerificate", URLEncoder.encode(node.get(0).hash.toBase58(), "UTF-8"));
		} catch (MessagingException e) {

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Set<String> getAttributes()
	{
		return this.author.getAk().getPublicKeys().keySet();
	}
	
	public String getPublicKeys()
	{
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(MimeUtility.encode(bos, "base64"));) {
			out.writeObject(author.getAk().getPublicKeys());
			out.flush();
			return bos.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MessagingException e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
}
