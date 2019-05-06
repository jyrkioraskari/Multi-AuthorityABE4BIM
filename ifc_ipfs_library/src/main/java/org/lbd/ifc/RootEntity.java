package org.lbd.ifc;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class RootEntity {
	private final List<Statement> triples = new ArrayList<>();
	private String guid;
	private String URI;
	private Resource Resource;

	public RootEntity() {
		super();

	}

	public void addTriples(Set<Statement> current_triples) {
		triples.addAll(current_triples);
	}

	public List<Statement> getTriples() {
		return triples;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
			this.guid = guid;
	}

	
	public void setAdjustedGuid(String guid) {
			try {
				this.guid = URLEncoder.encode(guid, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}//GuidCompressor.uncompressGuidString(guid);
	}
	public String getURI() {
		return URI;
	}

	public void setURI(String uRI) {
		URI = uRI;
	}

	public Resource getResource() {
		return Resource;
	}

	public void setResource(Resource resource) {
		Resource = resource;
	}
	
	@Override
	public String toString() {
		return this.guid.toString();
	}

}
