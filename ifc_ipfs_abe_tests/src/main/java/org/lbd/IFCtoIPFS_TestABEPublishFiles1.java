package org.lbd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.IPFS_Logging;

import fi.aalto.lbd.AaltoABEAuthenticator;
import fi.aalto.lbd.AaltoABEPublisher;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;

public class IFCtoIPFS_TestABEPublishFiles1 extends IPFS_Logging {

	protected AaltoABEPublisher publisher;
	final public StringBuilder encryption_policy = new StringBuilder();
	protected final int attribute_count;

	public IFCtoIPFS_TestABEPublishFiles1(String project_name, int attribute_count, String gp_hash) {
		this.attribute_count = attribute_count;
		this.node = "Publishing_" + project_name + "_" + this.attribute_count;
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

		this.publisher = new AaltoABEPublisher(gp_hash, false);
		AaltoABEAuthenticator authority = new AaltoABEAuthenticator(gp_hash, "" + attribute_count,
				attributes.toArray(new String[0]), true);
		publisher.addAuthorityPublicKeys(authority.getAk().getPublicKeys());
	}

	private void add(String directory) {
		File curDir = new File(directory);
		File[] filesList = curDir.listFiles();
		for (File f : filesList) {
			if (f.isFile()) {
				if (f.getName().endsWith(".ifc")) {
					String ipfs_hash="";
					long start;
					long end;
					long size=-1;
					start = System.nanoTime();
					FileInputStream fis;
					try {
						fis = new FileInputStream(f);
						byte[] data = new byte[(int) f.length()];
						fis.read(data);
						fis.close();

						String content = new String(data, "UTF-8");
						size=content.length();

						
						if (this.attribute_count == 0) {

							NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper("files",
									content.getBytes());
							List<MerkleNode> node = publisher.getIpfs().add(file);
							if (node.size() > 0)
								ipfs_hash=node.get(0).hash.toBase58();
						} else
							ipfs_hash=publisher.encrypt_save(content, this.encryption_policy.toString()).ipfs_hash;
						;
					} catch (FileNotFoundException e) {
					} catch (IOException e) {
						e.printStackTrace();
					}
					end = System.nanoTime();
					addLog("Round " + this.attribute_count + " "+size+" published in: total " + (end - start) / 1000000f + " ms hash: "
							+ ipfs_hash);

					
				}
			}
		}
		timelog.stream().forEach(txt -> writeToFile(txt, false));
	}

	public static void main(String[] args) {
		System.out.println("...");
		int start = 9;
		for (int attribute_count = start; attribute_count >= 0; attribute_count--) {
		
		if (args.length > 1) {
			IFCtoIPFS_TestABEPublishFiles1 ifc_ipfs = new IFCtoIPFS_TestABEPublishFiles1("files", attribute_count,
					args[1]);
			ifc_ipfs.add(args[0]);
		}
		}
		System.out.println("done");
		System.exit(0);
	}

}
