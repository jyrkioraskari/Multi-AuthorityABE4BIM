package org.lbd;

import java.io.ByteArrayInputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;

import fi.aalto.lbd.AaltoABEAuthenticator;
import fi.aalto.lbd.AaltoABEUser;

public class IPFS_ABEFetchDir {
	private final Property merkle_node;

	private final Model guid_directory_model = ModelFactory.createDefaultModel();
	private final Model temp_model = ModelFactory.createDefaultModel();

	private final AaltoABEAuthenticator authority;
	private final AaltoABEUser user;

	public IPFS_ABEFetchDir(String dir_hash) {
		this.merkle_node = guid_directory_model.createProperty("http://ipfs/merkle_node");
		this.authority = new AaltoABEAuthenticator("QmezCbZRUjwHqThpcsyF7sjNy2sjvqtCtzreetSFRywHsw");
		this.user = new AaltoABEUser("QmezCbZRUjwHqThpcsyF7sjNy2sjvqtCtzreetSFRywHsw");
		
		String keya_base64 = authority.userKeyGenBase64String(user.getName(), "a");
		user.addPrivateKeyString(keya_base64);
		String keyb_base64 = authority.userKeyGenBase64String(user.getName(), "b");
		user.addPrivateKeyString(keyb_base64);

		fetch(dir_hash);
	}

	public void fetch(String dir_hash) {
		readInGuidTable(dir_hash);
		long start;
		long end;
		System.out.println("START");
		start = System.nanoTime();
		this.guid_directory_model.listStatements().toList().stream()
				.filter(s -> s.getPredicate().equals(this.merkle_node)).map(s -> s.getObject()).forEach(x -> {
					readInNode(x.asLiteral().getLexicalForm());
				});

		end = System.nanoTime();
		System.out.println("Round read in: total " + (end - start) / 1000000f + " ms");
	}

	private void readInNode(String key) {
		temp_model.removeAll();
		String content = this.user.decrypt(key);
		System.out.println("element:\n "+content);
		ByteArrayInputStream bi = new ByteArrayInputStream(content.getBytes());
		temp_model.read(bi, null, "TTL");
	}

	private void readInGuidTable(String key) {
		guid_directory_model.removeAll();
		String content = this.user.decrypt(key);
		if(content==null)
			return;
		System.out.println("directory:\n "+content);
		guid_directory_model.read(new ByteArrayInputStream(content.getBytes()), null, "TTL");

	}

	public static void main(String[] args) {
		String hash = "QmU4wtrto3x5PnN7PYyjrUJyaHVYnZDGQiCYHKmYGWiJL7";
		new IPFS_ABEFetchDir(hash);
	}

}