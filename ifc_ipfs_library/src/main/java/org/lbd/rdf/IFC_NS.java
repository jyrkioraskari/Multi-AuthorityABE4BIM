/*
 * Copyright 2016 Pieter Pauwels, Ghent University; Jyrki Oraskari, Aalto University; Lewis John McGibbney, Apache
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lbd.rdf;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class IFC_NS {
	public static final String IFC2BASE = "http://www.buildingsmart-tech.org/ifcOWL/";
	
	public static final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
	public static final String OWL = "http://www.w3.org/2002/07/owl#";
	public static final String XSD = "http://www.w3.org/2001/XMLSchema#";
	public static final String DCE = "http://purl.org/dc/elements/1.1/";
	public static final String VANN = "http://purl.org/vocab/vann/";
	public static final String CC = "http://creativecommons.org/ns#";
	public static final String LIST = "https://w3id.org/list#";
	public static final String EXPRESS = "https://w3id.org/express#";
	public static final String SIMPLEBIM = "http://ifcowl.openbimstandards.org/SimpleBIM";

	protected static final Property property(String base_uri, String tag) {
		return ResourceFactory.createProperty(base_uri, tag);
	}

	public static final Property type = property(RDF, "type");
	
}
