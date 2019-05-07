package org.lbd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fi.aalto.lbd.AaltoABEAuthenticator;
import fi.aalto.lbd.AaltoABEPublisher;

public class IFCtoIPFS_TestABEPublish2 extends IFCtoIPFS_TestABEPublishCommon {

	public IFCtoIPFS_TestABEPublish2(String project_name, int attribute_count, String gp_hash) {
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

		this.publisher = new AaltoABEPublisher(gp_hash, false);
		for (int i = 0; i < attribute_count; i++) {
			String[] author_attributes = { "" + attributes.get(i) };
			authorities.add(new AaltoABEAuthenticator(gp_hash, "" + i+""+attribute_count, author_attributes, true));
			publisher.addAuthorityPublicKeys(authorities.get(i).getAk().getPublicKeys());
		}

	}

	public static void main(String[] args) {

		System.out.println("...");
		int start = 9;
		for (int attribute_count = start; attribute_count >= 0; attribute_count--)
			try {
				if (args.length > 1) {
					IFCtoIPFS_TestABEPublish2 ifc_ipfs = new IFCtoIPFS_TestABEPublish2("2",
							attribute_count, args[1]);
					ifc_ipfs.add(args[0]);
				}
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}

	}

}
