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

public class IPFS_ABEFetchDir extends IPFS_Logging{
	private final Property merkle_node;
	boolean jena=true;

	private final Model guid_directory_model = ModelFactory.createDefaultModel();
	private final Model temp_model = ModelFactory.createDefaultModel();

	private final AaltoABEUser user;
	protected final int attribute_count;

	public IPFS_ABEFetchDir(String directory,int attribute_count,String gp_hash) {
		this.attribute_count = attribute_count;
		this.node = "Fetching_"+ this.attribute_count;
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
		
		List<AaltoABEAuthenticator> authorities = new ArrayList<>();
		for (int i = 0; i < attribute_count; i++) {
			String[] author_attributes = { "" + attributes.get(i) };
			AaltoABEAuthenticator authority=new AaltoABEAuthenticator(gp_hash, "" + i+""+attribute_count, author_attributes, true);
			authorities.add(authority);
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
		if(!f.getName().startsWith("Publishing"))
			return;
		String[] filename=f.getName().split("_");
		if(filename.length>=4)
		{
			if(filename[1].equals(""+2)&&filename[4].equals(""+attribute_count))
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
							System.out.println(s[8]);
							fetch(s[8]);
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
		
		
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
		System.out.println("Round " + this.attribute_count + " read in: total " + (end - start) / 1000000f
				+ " ms hash: " + dir_hash);
		addLog("Round " + this.attribute_count + " read in: total " + (end - start) / 1000000f
				+ " ms hash: " + dir_hash);
	}

	private void readInNode(String key) {
		if(jena)
		  temp_model.removeAll();
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
		 content = this.user.decrypt(key);
		if(content==null)
			return;
		System.out.println("element:\n "+content);
		ByteArrayInputStream bi = new ByteArrayInputStream(content.getBytes());
		if(jena)
		  temp_model.read(bi, null, "TTL");
	}

	private void readInGuidTable(String key) {
		if(jena)
		  guid_directory_model.removeAll();
		
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
		  content = this.user.decrypt(key);
		if(content==null)
			return;
		System.out.println("directory:\n "+content);
		if(jena)
		  guid_directory_model.read(new ByteArrayInputStream(content.getBytes()), null, "TTL");

	}

	public static void main(String[] args) {
		if(args.length==2)
		{
			int start = 9;
			for (int attribute_count = start; attribute_count >= 0; attribute_count--)
		      new IPFS_ABEFetchDir(args[0],attribute_count,args[1]);
		}
		else
		  System.out.println("Usage: java -jar main.jar dir global_parameter_ipfs_hash");	
	}

}