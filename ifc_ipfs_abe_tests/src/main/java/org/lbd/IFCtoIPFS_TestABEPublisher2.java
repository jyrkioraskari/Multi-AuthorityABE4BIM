package org.lbd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fi.aalto.lbd.AaltoABEAuthenticator;
import fi.aalto.lbd.AaltoABEPublisher;

public class IFCtoIPFS_TestABEPublisher2 extends IFCtoIPFS_TestABECommon {

	public IFCtoIPFS_TestABEPublisher2(String project_name, int attribute_count) {
		super(project_name, attribute_count);
		System.out.println("attributes: " + attribute_count);
		char c = 'a';

		List<String> attributes = new ArrayList<>();
		List<AaltoABEAuthenticator> authority = new ArrayList<>();;

		for (int i = 0; i < attribute_count; i++) {
			char ch = (char) (c + i);
			if(i>0)
				 encryption_policy.insert(0, "and "+ch +" ");
			else
		     encryption_policy.insert(0, ch + " ");
			attributes.add(""+ch);
		}
		System.out.println(encryption_policy.toString().trim());
		System.out.println(attributes);

		this.publisher = new AaltoABEPublisher("QmezCbZRUjwHqThpcsyF7sjNy2sjvqtCtzreetSFRywHsw",false);
		for (int i = 0; i < attribute_count; i++) {
			String[] author_attributes= {""+attributes.get(i)};
			authority.add(new AaltoABEAuthenticator("QmezCbZRUjwHqThpcsyF7sjNy2sjvqtCtzreetSFRywHsw",""+i,author_attributes,false));
			publisher.addAuthorityPublicKeys(authority.get(i).getAk().getPublicKeys());
		}

	}

	public static void main(String[] args) {

		System.out.println("...");
		for (int attribute_count = 0; attribute_count < 10; attribute_count++)
			for (int n = 0; n < 5; n++) {
				try {
					if (args.length > 0) {
						IFCtoIPFS_TestABEPublisher2 ifc_ipfs = new IFCtoIPFS_TestABEPublisher2("IFC/IPFS project",
								attribute_count);
						ifc_ipfs.add(args[0]);
					}
				} catch (InterruptedException | IOException e) {
					e.printStackTrace();
				}
			}

	}

}
