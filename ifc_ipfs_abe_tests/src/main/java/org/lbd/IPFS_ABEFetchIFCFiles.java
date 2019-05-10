package org.lbd;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.IPFS_Logging;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;

import fi.aalto.lbd.AaltoABEAuthenticator;
import fi.aalto.lbd.AaltoABEUser;
import io.ipfs.multihash.Multihash;

public class IPFS_ABEFetchIFCFiles extends IPFS_Logging{
	private final Property merkle_node;
	boolean jena=true;

	private final Model guid_directory_model = ModelFactory.createDefaultModel();
	private final Model temp_model = ModelFactory.createDefaultModel();

	private final AaltoABEUser user;
	protected final int attribute_count;

	public IPFS_ABEFetchIFCFiles(String directory,int attribute_count,String gp_hash) {
		this.attribute_count = attribute_count;
		this.node = "Fetching_ifc_"+ this.attribute_count;
		List<String> attributes = new ArrayList<>();
		System.out.println("attributes: " + attribute_count);
		char c = 'a';

		for (int i = 0; i < attribute_count; i++) {
			char ch = (char) (c + i);
			attributes.add("" + ch);
		}
		System.out.println(attributes);
		this.merkle_node = guid_directory_model.createProperty("http://ipfs/merkle_node");
		this.user = new AaltoABEUser(gp_hash);
		
		AaltoABEAuthenticator authority = new AaltoABEAuthenticator(gp_hash, "" + attribute_count,
				attributes.toArray(new String[0]), true);
		
		for (int i = 0; i < attribute_count; i++) {
			String keya_base64 = authority.userKeyGenBase64String(user.getName(), attributes.get(i));
			user.addPrivateKeyString(keya_base64);
		}
		File curDir = new File(directory);
		File[] filesList = curDir.listFiles();
		for (File f : filesList) {
			if (f.isFile()) {
				handleFile(f,attribute_count);
			}
		}		
		timelog.stream().forEach(txt -> writeToFile(txt, false));
	}

	private void handleFile(File f, int attribute_count) {
		if(!f.getName().startsWith("Publishing_files_"))
			return;
		String[] filename=f.getName().split("_");
		if(filename.length>=4)
		{
			if(filename[2].equals(""+attribute_count))
			{  
				System.out.println(f.getName());
				Scanner sc;
				try {
					sc = new Scanner(f);
					sc.useDelimiter("\n");

					while (sc.hasNext()) {
						String line = sc.next();
						if (line.startsWith("Round ")) {
							String[] s = line.split(" ");
							fetch(s[9]);
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
		
		
	}

	public void fetch(String ipfs_hash) {
		
		long start;
		long end;
		System.out.println("START");
		start = System.nanoTime();
		long size=readInNode(ipfs_hash);
		end = System.nanoTime();
		System.out.println("Round " + this.attribute_count +" "+size+" read in: total " + (end - start) / 1000000f
				+ " ms hash: " + ipfs_hash);
		addLog("Round " + this.attribute_count +" "+size+" read in: total " + (end - start) / 1000000f
				+ " ms hash: " + ipfs_hash);
	}

	private long  readInNode(String key) {
		System.out.println("key is "+key);
		String content=null;
		if(this.attribute_count ==0)
		{
			Multihash filePointer = Multihash.fromBase58(key);
			try {
				content = new String(user.getIpfs().cat(filePointer));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else
		 content = this.user.decryptABEAES(key);
		return content.length();
	}

	
	public static void main(String[] args) {
		if(args.length==2)
		{
			int start = 9;
			for (int attribute_count = start; attribute_count >= 0; attribute_count--)
		      new IPFS_ABEFetchIFCFiles(args[0],attribute_count,args[1]);
		}
		else
		  System.out.println("Usage: java -jar main.jar dir global_parameter_ipfs_hash");	
	}

}