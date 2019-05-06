package org.lbd;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lbd.ifc.RootEntity;
import org.lbd.rdf.CanonizedPattern;
import org.rdfcontext.signing.RDFC14Ner;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

import fi.aalto.lbd.AaltoABEAuthenticator;
import fi.aalto.lbd.AaltoABEPublisher;
import io.ipfs.api.MerkleNode;
import io.ipfs.multihash.Multihash;

public class IFCtoIPFS_ABEPublisher {
	private final Model jena_guid_directory_model = ModelFactory.createDefaultModel();
	private final Property jena_property_random;
	private final Property jena_property_merkle_node;

	
	private final String project_name;
	private final String baseURI = "http://ipfs/bim/";
	
	private final CanonizedPattern canonized_pattern=new CanonizedPattern();
	private final Random random_number_generator = new Random(System.currentTimeMillis());

	private final AaltoABEPublisher publisher;

	final public String encryption_policy = "and a b"; // syntax!!!
	String[] attributes1 = { "a", "b" };
	final AaltoABEAuthenticator authority;
	
	public IFCtoIPFS_ABEPublisher(String project_name) {
		this.publisher=new AaltoABEPublisher("QmezCbZRUjwHqThpcsyF7sjNy2sjvqtCtzreetSFRywHsw");
		this.authority= new AaltoABEAuthenticator("QmezCbZRUjwHqThpcsyF7sjNy2sjvqtCtzreetSFRywHsw", attributes1);
		publisher.addAuthorityPublicKeys(authority.getAk().getPublicKeys());
		
		this.jena_property_random = jena_guid_directory_model.createProperty("http://ipfs/random");
		this.jena_property_merkle_node = jena_guid_directory_model.createProperty("http://ipfs/merkle_node");
		this.project_name=project_name;
	}


	public void add(String ifc_file) throws InterruptedException, IOException {
		Splitted_IfcOWL ifcrdf = new Splitted_IfcOWL(ifc_file);
		publishEntityNodes2IPFS(ifcrdf.getEntitys(), ifcrdf.getURI2GUID_map());
		
		String project_table_hash = publishDirectoryNode2IPFS(this.project_name, jena_guid_directory_model);
		System.out.println("Directory hash: "+project_table_hash);
		// IPNS
		try {
			Multihash filePointer = Multihash.fromBase58(project_table_hash);
			Map pub = publisher.getIpfs().name.publish(filePointer);
			System.out.println(pub);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		jena_guid_directory_model.write(System.out, "TTL");
	}
	
	private Map<String, Resource> resources_map = new HashMap<>();

	private void publishEntityNodes2IPFS(List<RootEntity> root_entitys, Map<String, String> uri2guid) {

		for (RootEntity g1 : root_entitys) {
			Resource guid_subject = null;
			for (Statement triple : g1.getTriples()) {

				String s_uri = triple.getSubject().getURI();
				Resource subject = null;
				String sg = uri2guid.get(s_uri); // The map sets the coding
				if (sg != null) {
					String sn = s_uri.substring(0, (s_uri.lastIndexOf("/") + 1)) + sg;
					subject = ResourceFactory.createResource(sn);
					guid_subject = subject;
					resources_map.put(s_uri, subject);
				}
			}
		}

		for (RootEntity g : root_entitys) {
			Resource guid_subject = null;
			Model entity_model = ModelFactory.createDefaultModel();
			boolean random_added = false;
			for (Statement triple : g.getTriples()) {

				String s_uri = triple.getSubject().getURI();
				Resource subject = null;
				String sg = uri2guid.get(s_uri); // The map sets the coding
				if (sg != null) {
					String sn = s_uri.substring(0, (s_uri.lastIndexOf("/") + 1)) + sg;
					subject = entity_model.createResource(sn);
					guid_subject = subject;
					resources_map.put(s_uri, subject);
					
					if (!random_added) {
						Literal random_number_literal = entity_model
								.createLiteral("" + random_number_generator.nextInt());
						entity_model.add(entity_model.createStatement(subject, this.jena_property_random,
								random_number_literal));
						random_added = true;
					}
				}

				if (subject == null) {
					subject = resources_map.get(s_uri);
					if (subject == null) {
						subject = entity_model.createResource();
						resources_map.put(s_uri, subject);
					}
				}

				Property property = entity_model
						.getProperty(triple.getPredicate().getURI());
				RDFNode object = triple.getObject();
				if (object.isResource()) {
					Resource or = resources_map.get(object.asResource().getURI());
					if (or == null) {
						if (property.toString().equals(
								"http://www.buildingsmart-tech.org/ifcOWL/IFC2X3_TC1#relatedObjects_IfcRelDecomposes"))
							System.out.println("decompo: " + object.asResource().getURI());
						char last = object.asResource().getURI()
								.charAt(object.asResource().getURI().length() - 1);
						if (object.asResource().getURI().contains("_") && Character.isDigit(last)) {
							or = entity_model.createResource();
						} else
							or = entity_model.createResource(object.asResource().getURI());
						resources_map.put(object.asResource().getURI(), or);
					}

					String og = uri2guid.get(or.getURI());
					if (og != null) {
						String on = or.getURI().substring(0, (or.getURI().lastIndexOf("/") + 1)) + og;
						or = entity_model.createResource(on);
					}
					entity_model.add(entity_model.createStatement(subject, property, or));
				} else {
					Literal hp_literal = entity_model.createLiteral(object.toString());
					entity_model.add(entity_model.createStatement(subject, property, hp_literal));
				}

			}
			createMerkleNode(g.getGuid(), entity_model, guid_subject);

		}
	}
	
	private boolean directory_random_created = false;


	private void createMerkleNode(String guid, Model model, Resource guid_subject) {
		try {
			RDFC14Ner r1=new RDFC14Ner(model);

			String cleaned=canonized_pattern.clean(r1.getCanonicalString());
			
			String entity_ipfs_hash=publisher.encrypt_save(cleaned, this.encryption_policy);
			if (!directory_random_created) {
				Resource directory_recource = jena_guid_directory_model.createResource(); // empty
				Literal random_number_literal = jena_guid_directory_model
						.createLiteral("" + random_number_generator.nextInt());
				jena_guid_directory_model.add(jena_guid_directory_model.createStatement(directory_recource,
						this.jena_property_random, random_number_literal));
				directory_random_created = true;
			}
			if (guid_subject != null) {
				Resource guid_resource = jena_guid_directory_model.createResource(baseURI + URLEncoder.encode(guid));
				Literal hash_literal = jena_guid_directory_model.createLiteral(entity_ipfs_hash);
				jena_guid_directory_model.add(jena_guid_directory_model.createStatement(guid_resource, this.jena_property_merkle_node, hash_literal));

				Property hp_type = model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
				RDFNode guid_class = null;
				
				for (Statement st : guid_subject.listProperties(hp_type).toList())
					guid_class = st.getObject();
				Resource apache_guid_resource = jena_guid_directory_model.createResource(guid_resource.getURI());
				if(guid_class==null)
				{
					System.err.println("No GUID type.");
					return;
				}

				if(!guid_class.isResource())
					return;
				jena_guid_directory_model.add(jena_guid_directory_model.createStatement(apache_guid_resource, RDF.type, guid_class));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String publishDirectoryNode2IPFS(String project_name, Model model) {
		List<MerkleNode> node = null;

		try {
			RDFC14Ner r1=new RDFC14Ner(model);
			String cleaned=canonized_pattern.clean(r1.getCanonicalString());
			return publisher.encrypt_save(cleaned, this.encryption_policy);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	

	public static void main(String[] args) {
		System.out.println("...");
		try {
			if(args.length>0)
			{
			IFCtoIPFS_ABEPublisher ifc_ipfs=new IFCtoIPFS_ABEPublisher("IFC/IPFS project");
			 ifc_ipfs.add(args[0]);
			}
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}

}
