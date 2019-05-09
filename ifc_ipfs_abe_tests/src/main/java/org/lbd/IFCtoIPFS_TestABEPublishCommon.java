package org.lbd;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.IPFS_Logging;
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

import fi.aalto.lbd.AaltoABEPublisher;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;

public class IFCtoIPFS_TestABEPublishCommon extends IPFS_Logging {	
	protected final CanonizedPattern canonized_pattern = new CanonizedPattern();
	protected final Random random_number_generator = new Random(System.currentTimeMillis());
	protected final String project_name;
	protected final String baseURI = "http://ipfs/bim/";

	protected final int attribute_count;
	protected final Model jena_guid_directory_model = ModelFactory.createDefaultModel();
	protected final Property jena_property_random;
	protected final Property jena_property_merkle_node;
	protected AaltoABEPublisher publisher;
	final public StringBuilder encryption_policy = new StringBuilder();

	
	public IFCtoIPFS_TestABEPublishCommon(String project_name, int attribute_count) {
		super();
		this.attribute_count = attribute_count;
		this.jena_property_random = jena_guid_directory_model.createProperty("http://ipfs/random");
		this.jena_property_merkle_node = jena_guid_directory_model.createProperty("http://ipfs/merkle_node");
		this.project_name = project_name;

	}

	protected void add(String ifc_file) throws InterruptedException, IOException {
		String fn = new File(ifc_file).getName();
		this.node = "Publishing_"+project_name+"_" + fn + "_" + this.attribute_count;
		timelog.clear();
		Splitted_IfcOWL ifcrdf = new Splitted_IfcOWL(ifc_file);
		long start;
		long end;
		start = System.nanoTime();
		publishEntityNodes2IPFS(ifcrdf.getEntitys(), ifcrdf.getURI2GUID_map());

		String project_table_hash = publishDirectoryNode2IPFS(this.project_name, jena_guid_directory_model);
		System.out.println("Directory hash: " + project_table_hash);
		end = System.nanoTime();
		System.out.println("Round " + this.attribute_count + " read in: total " + (end - start) / 1000000f
				+ " ms hash: " + project_table_hash);
		addLog("Round " + this.attribute_count + " published in: total " + (end - start) / 1000000f + " ms hash: "
				+ project_table_hash);

		// IPNS
		/*
		 * try { Multihash filePointer = Multihash.fromBase58(project_table_hash); Map
		 * pub = publisher.getIpfs().name.publish(filePointer); System.out.println(pub);
		 * } catch (IOException e1) { e1.printStackTrace(); }
		 * jena_guid_directory_model.write(System.out, "TTL");
		 */
		timelog.stream().forEach(txt -> writeToFile(txt, false));
		System.out.println("added");
	}

	private Map<String, Resource> resources_map = new HashMap<>();

	private void publishEntityNodes2IPFS(List<RootEntity> root_entitys, Map<String, String> uri2guid) {

		for (RootEntity g1 : root_entitys) {
			for (Statement triple : g1.getTriples()) {

				String s_uri = triple.getSubject().getURI();
				Resource subject = null;
				String sg = uri2guid.get(s_uri); // The map sets the coding
				if (sg != null) {
					String sn = s_uri.substring(0, (s_uri.lastIndexOf("/") + 1)) + sg;
					subject = ResourceFactory.createResource(sn);
					resources_map.put(s_uri, subject);
				}
			}
		}

		for (RootEntity g : root_entitys) {
			Resource guid_subject = null;
			Model entity_model = ModelFactory.createDefaultModel();
			boolean random_added = false;
			long start;
			long end;
			start = System.nanoTime();
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

				Property property = entity_model.getProperty(triple.getPredicate().getURI());
				RDFNode object = triple.getObject();
				if (object.isResource()) {
					Resource or = resources_map.get(object.asResource().getURI());
					if (or == null) {
						if (property.toString().equals(
								"http://www.buildingsmart-tech.org/ifcOWL/IFC2X3_TC1#relatedObjects_IfcRelDecomposes"))
							System.out.println("decompo: " + object.asResource().getURI());
						char last = object.asResource().getURI().charAt(object.asResource().getURI().length() - 1);
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
			String entity_ipfs_hash = createMerkleNode(g.getGuid(), entity_model, guid_subject);
			end = System.nanoTime();
			addLog("File " + this.attribute_count + " published in: total " + (end - start) / 1000000f + " ms hash: "
					+ entity_ipfs_hash);

		}
	}

	private boolean directory_random_created = false;

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
				entity_ipfs_hash = publisher.encryptABEAES_save(cleaned, this.encryption_policy.toString()).ipfs_hash;
			if (!directory_random_created) {
				Resource directory_recource = jena_guid_directory_model.createResource(); // empty
				Literal random_number_literal = jena_guid_directory_model
						.createLiteral("" + random_number_generator.nextInt());
				jena_guid_directory_model.add(jena_guid_directory_model.createStatement(directory_recource,
						this.jena_property_random, random_number_literal));
				directory_random_created = true;
			}
			if (guid_subject != null) {
				Resource guid_resource = jena_guid_directory_model.createResource(baseURI + URLEncoder.encode(guid, "UTF-8"));
				Literal hash_literal = jena_guid_directory_model.createLiteral(entity_ipfs_hash);
				jena_guid_directory_model.add(jena_guid_directory_model.createStatement(guid_resource,
						this.jena_property_merkle_node, hash_literal));

				Property hp_type = model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
				RDFNode guid_class = null;

				for (Statement st : guid_subject.listProperties(hp_type).toList())
					guid_class = st.getObject();
				Resource apache_guid_resource = jena_guid_directory_model.createResource(guid_resource.getURI());
				if (guid_class == null) {
					System.err.println("No GUID type.");
					return null;
				}

				if (!guid_class.isResource())
					return null;
				jena_guid_directory_model
						.add(jena_guid_directory_model.createStatement(apache_guid_resource, RDF.type, guid_class));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return entity_ipfs_hash;
	}

	protected String publishDirectoryNode2IPFS(String project_name, Model model) {
		try {
			RDFC14Ner r1 = new RDFC14Ner(model);
			String cleaned = canonized_pattern.clean(r1.getCanonicalString());
			if (this.attribute_count == 0) {

				NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper(project_name,
						cleaned.getBytes());
				List<MerkleNode> node = publisher.getIpfs().add(file);
				if (node.size() > 0)
					return node.get(0).hash.toBase58();
				else
					return null;
			} else
				return publisher.encryptABEAES_save(cleaned, this.encryption_policy.toString()).ipfs_hash;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
