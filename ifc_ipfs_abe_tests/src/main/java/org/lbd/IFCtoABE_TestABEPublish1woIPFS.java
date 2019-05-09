package org.lbd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fi.aalto.lbd.AaltoABEAuthenticator;
import fi.aalto.lbd.tests.AaltoABEPublisherWithoutIPFS;

public class IFCtoABE_TestABEPublish1woIPFS extends IFCtoIPFS_TestABEPublishCommonWoIPFS {

	public IFCtoABE_TestABEPublish1woIPFS(String project_name, int attribute_count, String gp_hash) {
		super(project_name, attribute_count);
		List<String> attributes = new ArrayList<>();
		System.out.println("attributes: " + attribute_count);
		char c = 'a';

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

		this.publisher = new AaltoABEPublisherWithoutIPFS(gp_hash, false);
		AaltoABEAuthenticator authority = new AaltoABEAuthenticator(gp_hash,""+attribute_count,
				attributes.toArray(new String[0]), true);
		publisher.addAuthorityPublicKeys(authority.getAk().getPublicKeys());
	}



	public static void main(String[] args) {
		System.out.println("...");
		int start = 9;
		for (int attribute_count = start; attribute_count >= 0; attribute_count--)
			try {
				if (args.length > 1) {
					IFCtoABE_TestABEPublish1woIPFS ifc_ipfs = new IFCtoABE_TestABEPublish1woIPFS("1woi",attribute_count, args[1]);
					ifc_ipfs.add(args[0]);
				}
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}


	}

}
