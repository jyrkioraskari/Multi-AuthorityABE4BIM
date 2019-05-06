package org.lbd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lbd.ifc.RootEntity;
import org.lbd.rdf.IFC_NS;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;

import be.ugent.IfcSpfReader;
import nl.tue.ddss.convert.Header;
import nl.tue.ddss.convert.HeaderParser;
import nl.tue.ddss.convert.IfcVersion;
import nl.tue.ddss.convert.IfcVersionException;

public class Splitted_IfcOWL {

	private final Model model;
	private final Model model_inference;
	private final Set<Statement> processed = new HashSet<>();
	private final InfModel inference_model;

	private final List<RootEntity> guid_sets = new ArrayList<>();
	private final Map<String, String> uri_guid = new HashMap<>();

	private final Map<String, Resource> rootmap = new HashMap<>();
	private final Set<String> roots = new HashSet<>();
	
	private final Set<String> geometry = new HashSet<>();
	private final Set<Resource> common = new HashSet<>();

	private IfcVersion version = null;
	private String ifc_version = "";

	private int total_count = 0;
	
	long generated_id=0;

	public Splitted_IfcOWL(String ifc_file) {
		IfcVersion.initDefaultIfcNsMap();

		FileInputStream input;
		System.out.println(ifc_file);
		try {
			input = new FileInputStream(new File(ifc_file));
			Header header = HeaderParser.parseHeader(input);
			if (header.getSchema_identifiers().size() > 0)
				this.ifc_version = header.getSchema_identifiers().get(0);
			version = IfcVersion.getIfcVersion(header);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IfcVersionException e) {
			e.printStackTrace();
		}

		model_inference = createJenaModel(ifc_file);
		Reasoner reasoner = ReasonerRegistry.getRDFSReasoner();
		inference_model = ModelFactory.createInfModel(reasoner, model_inference);

		model = createJenaModel(ifc_file);

		readInOntologyTTL(inference_model, version.getLabel() + ".ttl");
		split();
	}

	public Model createJenaModel(String ifc_file) {
		try {
			IfcSpfReader rj = new IfcSpfReader();
			try {

				String uriBase = "http://ipfs/bim/";
				Model m = ModelFactory.createDefaultModel();
				ByteArrayOutputStream stringStream = new ByteArrayOutputStream();
				rj.convert(ifc_file, stringStream, uriBase);
				InputStream stream = new ByteArrayInputStream(
						stringStream.toString().getBytes(StandardCharsets.UTF_8.name()));
				m.read(stream, null, "TTL");
				return m;
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();

		}
		System.out.println("IFC-RDF conversion not done");
		return ModelFactory.createDefaultModel();
	}

	private RootEntity current_root_entity;
	private final Set<Statement> current_root_entity_triples = new HashSet<>();

	long empty_item = 0;

	private void split() {
		Resource ifcRoot = inference_model.createResource(IFC_NS.IFC2BASE + this.version.getLabel() + "#IfcRoot");
		ResIterator rit = inference_model.listResourcesWithProperty(IFC_NS.type, ifcRoot);
		rit.forEachRemaining(x -> {
			roots.add(x.getURI());
			rootmap.put(x.getURI(), x);

		});

		/*
		 * Naming cannot be made global.
		 */
		
		Resource ifcProductRepresentation = inference_model
				.createResource(IFC_NS.IFC2BASE + this.version.getLabel() + "#IfcProductRepresentation");
		ResIterator rpt = inference_model.listResourcesWithProperty(IFC_NS.type, ifcProductRepresentation);
		rpt.forEachRemaining(x -> {
			//roots.add(x.getURI());
			//rootmap.put(x.getURI(), x);
			geometry.add(x.getURI());
		});
		
		Resource ifcRepresentation = inference_model
				.createResource(IFC_NS.IFC2BASE + this.version.getLabel() + "#IfcRepresentation");
		ResIterator rep = inference_model.listResourcesWithProperty(IFC_NS.type, ifcRepresentation);
		rep.forEachRemaining(x -> {
			//roots.add(x.getURI());
			//rootmap.put(x.getURI(), x);
			geometry.add(x.getURI());


		});
		
		
		

		Resource ifcGeometricRepresentationItem = inference_model
				.createResource(IFC_NS.IFC2BASE + this.version.getLabel() + "#IfcGeometricRepresentationItem");
		ResIterator gr = inference_model.listResourcesWithProperty(IFC_NS.type, ifcGeometricRepresentationItem);
		gr.forEachRemaining(x -> {
			//roots.add(x.getURI());
			//rootmap.put(x.getURI(), x);
			geometry.add(x.getURI());


		});

		Resource ifcRepresentationContext = inference_model
				.createResource(IFC_NS.IFC2BASE + this.version.getLabel() + "#IfcRepresentationContext");
		ResIterator rc = inference_model.listResourcesWithProperty(IFC_NS.type, ifcRepresentationContext);
		rc.forEachRemaining(x -> {
			//roots.add(x.getURI());
			//rootmap.put(x.getURI(), x);
			geometry.add(x.getURI());

		});

		Resource ifcRepresentationMap = inference_model
				.createResource(IFC_NS.IFC2BASE + this.version.getLabel() + "#IfcRepresentationMap");
		ResIterator rmp = inference_model.listResourcesWithProperty(IFC_NS.type, ifcRepresentationMap);
		rmp.forEachRemaining(x -> {
			//roots.add(x.getURI());
			//rootmap.put(x.getURI(), x);
			geometry.add(x.getURI());


		});

		Resource ifcObjectPlacement = inference_model
				.createResource(IFC_NS.IFC2BASE + this.version.getLabel() + "#IfcObjectPlacement");
		ResIterator op = inference_model.listResourcesWithProperty(IFC_NS.type, ifcObjectPlacement);
		op.forEachRemaining(x -> {
			//roots.add(x.getURI());
			//rootmap.put(x.getURI(), x);
			geometry.add(x.getURI());


		});

		Resource ifcSurfaceStyleShading = inference_model
				.createResource(IFC_NS.IFC2BASE + this.version.getLabel() + "#IfcSurfaceStyleShading");
		ResIterator sss = inference_model.listResourcesWithProperty(IFC_NS.type, ifcSurfaceStyleShading);
		sss.forEachRemaining(x -> {
			//roots.add(x.getURI());
			//rootmap.put(x.getURI(), x);
			geometry.add(x.getURI());

		});
		
		
		
		Resource ifcPresentationStyleAssignment = inference_model
				.createResource(IFC_NS.IFC2BASE + this.version.getLabel() + "#IfcPresentationStyleAssignment");
		ResIterator psa = inference_model.listResourcesWithProperty(IFC_NS.type, ifcPresentationStyleAssignment);
		psa.forEachRemaining(x -> {
			//roots.add(x.getURI());
			//rootmap.put(x.getURI(), x);
			geometry.add(x.getURI());

		});
		
		Resource ifcPresentationLayerAssignment = inference_model
				.createResource(IFC_NS.IFC2BASE + this.version.getLabel() + "#IfcPresentationLayerAssignment");
		ResIterator pla = inference_model.listResourcesWithProperty(IFC_NS.type, ifcPresentationLayerAssignment);
		pla.forEachRemaining(x -> {
			//roots.add(x.getURI());
			//rootmap.put(x.getURI(), x);
			geometry.add(x.getURI());


		});
		
		
		
		Resource ifcMaterial = inference_model
				.createResource(IFC_NS.IFC2BASE + this.version.getLabel() + "#IfcMaterial");
		ResIterator material = inference_model.listResourcesWithProperty(IFC_NS.type, ifcMaterial);
		material.forEachRemaining(x -> {
			common.add(x);

		});

		Set<Resource> unreferenced = new HashSet<>();

		/*
		 * model.listStatements().forEachRemaining(x -> { if
		 * (x.getPredicate().toString().endsWith("#ownerHistory_IfcRoot")) try {
		 * roots.add(x.getObject().asResource().getURI());
		 * rootmap.put(x.getObject().asResource().getURI(), x.getObject().asResource());
		 * } catch (Exception e) { System.out.println(x); e.printStackTrace(); } });
		 */
        
		
		
		roots.stream().forEach(x -> {
			current_root_entity = new RootEntity();
			traverse(x);
			current_root_entity.setURI(x);
			current_root_entity.setResource(rootmap.get(x));
			current_root_entity.addTriples(current_root_entity_triples);
			if(current_root_entity.getGuid()!=null)
			   uri_guid.put(current_root_entity.getResource().getURI(), current_root_entity.getGuid());
			else
			{
			   uri_guid.put(current_root_entity.getResource().getURI(), ""+generated_id++);
			}
			guid_sets.add(current_root_entity);
			current_root_entity_triples.clear();
		});

		unreferenced.addAll(getlistOfNonGUIDSubjectsNotReferenced());
		// System.out.println("unreferenced: "+unreferenced.size());
		// unreferenced.forEach(x -> roots.add(x.getURI()));
		// unreferenced.forEach(x -> rootmap.put(x.getURI(), x));
		unreferenced.stream().forEach(x -> {
			// System.out.println("unreferenced: "+x);
			current_root_entity = new RootEntity();
			current_root_entity.setGuid("" + empty_item++);
			traverse(x.getURI());
			current_root_entity.setURI(x.getURI());
			current_root_entity.setResource(x);
			current_root_entity.addTriples(current_root_entity_triples);
			// uri_guid.put(current_root_entity.getResource().getURI(), ""); // not
			// referenced
			guid_sets.add(current_root_entity);
			current_root_entity_triples.clear();
		});
		
		common.stream().forEach(x -> {
			current_root_entity = new RootEntity();
			current_root_entity.setGuid("");
			traverse(x.getURI());
			current_root_entity.setURI(x.getURI());
			current_root_entity.setResource(x);
			current_root_entity.addTriples(current_root_entity_triples);
			guid_sets.add(current_root_entity);
			current_root_entity_triples.clear();
		});
	}

	private void traverse(String r) {
		Resource rm = model.getResource(r); // The same without inferencing
		rm.listProperties().forEachRemaining(x -> {
			if (x.getPredicate().toString().endsWith("#ownerHistory_IfcRoot"))
				return;

			if (x.getPredicate().toString().endsWith("#globalId_IfcRoot")) {
				String guid = x.getObject().asResource()
						.getProperty(model.getProperty("https://w3id.org/express#hasString")).getObject().asLiteral()
						.getLexicalForm();
				current_root_entity.setAdjustedGuid(guid); // just create a new GUIDSet Note: there should not be many
			}

			// These are version independent:
			
			if (x.getPredicate().toString().endsWith("#representation_IfcProduct")) // no complete graph filtering
				return;
			if (x.getPredicate().toString().endsWith("#objectPlacement_IfcProduct"))
				return;
				

			processed.add(x);

			if (current_root_entity_triples.add(x)) {
				this.total_count += 1;
				if (x.getObject().isResource()) {
					if (!roots.contains(x.getObject().asResource().getURI())&& !geometry.contains(x.getObject().asResource().getURI())&& !common.contains(x.getObject().asResource()))
						traverse(x.getObject().asResource().getURI());
				}

			}
		});

	}

	private void readInOntologyTTL(Model model, String ontology_file) {

		InputStream in = null;
		try {
			in = Splitted_IfcOWL.class.getResourceAsStream("/" + ontology_file);
			if (in == null) {
				try {
					in = Splitted_IfcOWL.class.getResourceAsStream("/resources/" + ontology_file);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
			model.read(in, null, "TTL");
			in.close();

		} catch (Exception e) {
			System.out.println("missing file: " + ontology_file);
			e.printStackTrace();
		}

	}

	public List<RootEntity> getEntitys() {
		return guid_sets;
	}

	public Map<String, String> getURI2GUID_map() {
		return uri_guid;
	}

	public String getIfc_version() {
		return ifc_version;
	}

	public void setIfc_version(String ifc_version) {
		this.ifc_version = ifc_version;
	}

	private Set<Resource> getlistOfNonGUIDSubjectsNotReferenced() {
		final Set<Resource> list = new HashSet<>();
		model.listStatements().forEachRemaining(x -> {
			if (!model.listStatements(null, null, x.getSubject()).hasNext())
				if (!this.rootmap.containsKey(x.getSubject().toString()))
					list.add(x.getSubject());
		});
		return list;
	}

	public Model getModel() {
		return model;
	}

	public void writeOWLFile(String output_directory) {

		try {
			FileOutputStream bufout = new FileOutputStream(new File(output_directory + "ifcOwl.ttl"));
			model.write(bufout, "TTL");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void printDifference() {
		final Set<Statement> temp = new HashSet<>();

		System.out.println("1");
		model.listStatements().forEachRemaining(x -> {
			if (x.getPredicate().toString().endsWith("#ownerHistory_IfcRoot"))
				return;
			temp.add(x);
		});

		System.out.println("2");
		processed.forEach(x -> {
			temp.remove(x);
		});

		System.out.println("3");

		System.out.println("Difference: " + temp.size());
		temp.forEach(x -> {
			System.out.println("-- " + x);
		});
		System.out.println("done");

	}

	public InfModel getInference_model() {
		return inference_model;
	}

	public int getTotal_count() {
		return total_count;
	}

}
