package org.lbd;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.rdfcontext.signing.RDFC14Ner;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

import fi.aalto.lbd.AaltoABEAuthenticator;
import fi.aalto.lbd.AaltoABEPublisher;
import fi.aalto.lbd.AaltoABEUser;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;

public class IFCtoIPFS_TestABEFunctionalityTest extends IFCtoIPFS_TestABEPublishCommon {
	private final AaltoABEUser user;

	public IFCtoIPFS_TestABEFunctionalityTest(String project_name, int attribute_count, String gp_hash) {
		super(project_name, attribute_count);
		System.out.println("attributes: " + attribute_count);
		char c = 'a';

		List<String> attributes = new ArrayList<>();
		List<AaltoABEAuthenticator> authorities = new ArrayList<>();
		

		for (int i = 0; i < attribute_count; i++) {
			char ch = (char) (c + i);
			if (i > 0)
				encryption_policy.insert(0, "and " + ch + " ");
			else
				encryption_policy.insert(0, ch + " ");
			attributes.add("" + ch);
		}
		System.out.println(encryption_policy.toString().trim());
		System.out.println(attributes);
		this.user = new AaltoABEUser(gp_hash);
		this.publisher = new AaltoABEPublisher(gp_hash, false);
		for (int i = 0; i < attribute_count; i++) {
			String[] author_attributes = { "" + attributes.get(i) };
			AaltoABEAuthenticator authority=new AaltoABEAuthenticator(gp_hash, "" + i, author_attributes, false);
			authorities.add(authority);
			
			publisher.addAuthorityPublicKeys(authorities.get(i).getAk().getPublicKeys());
			String keya_base64 = authority.userKeyGenBase64String(user.getName(), attributes.get(i));
			user.addPrivateKeyString(keya_base64);
		}

	}
	protected String createMerkleNode(String guid, Model model, Resource guid_subject) {
		String entity_ipfs_hash = null;
		try {
			RDFC14Ner r1 = new RDFC14Ner(model);
			String cleaned = canonized_pattern.clean(r1.getCanonicalString());

			if (this.attribute_count == 0) {

				NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper(project_name,
						cleaned.getBytes());
				List<MerkleNode> node = publisher.getIpfs().add(file);
				if (node.size() > 0)
					entity_ipfs_hash = node.get(0).hash.toBase58();
				else {
					System.err.println("No node created!!");
					return null;
				}
			} else
				entity_ipfs_hash = publisher.encryptABE_save(cleaned, this.encryption_policy.toString()).ipfs_hash;
			
			String content = this.user.decryptABE(entity_ipfs_hash);
			if(!content.equals(cleaned))
			{
				System.err.println("Encrypted and Decrypted do not match!");
				System.exit(0);
			}
			else System.out.println("Created");
			

		} catch (Exception e) {
			e.printStackTrace();
		}
		return entity_ipfs_hash;
	}


	public static void main(String[] args) {

		System.out.println("...");
		int start = 9;
		for (int attribute_count = start; attribute_count >= 0; attribute_count--)
			try {
				if (args.length > 1) {
					IFCtoIPFS_TestABEFunctionalityTest ifc_ipfs = new IFCtoIPFS_TestABEFunctionalityTest("2",
							attribute_count, args[1]);
					ifc_ipfs.add(args[0]);
				}
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}

	}

}
