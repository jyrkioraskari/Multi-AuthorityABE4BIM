package org.lbd.rdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import be.ugent.IfcSpfReader;

public class CanonizedPattern {

	public String clean(String cline) {
		// Converts C14 into N3
		String output_format = cline;//.replaceAll("\\[", "").replaceAll("\\]", " \n");
		output_format=intoLines(output_format);
		String[] model_splitted = output_format.split("\n");
		StringBuilder sp = new StringBuilder();
		for (String s : model_splitted) {
			List<String> list = new ArrayList<String>();
			try {
				Matcher mx = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(s.trim());
				while (mx.find())
					list.add(mx.group(1));

				if (list.size() == 2)
					list.add("\"\"");

				String ls = list.get(0);
				if (ls.matches("^(http|https|ftp)://.*$"))
					ls = "<" + ls + ">";
				String lp = list.get(1);
				if (lp.matches("^(http|https|ftp)://.*$"))
					lp = "<" + lp + ">";
				String lo = list.get(2);
				if (lo.matches("^(http|https|ftp)://.*$"))
					lo = "<" + lo + ">";
				sp.append(ls + " " + lp + " " + lo + " .\n");
			} catch (Exception e) {
				System.err.println("Bad: pattern: " + s.trim());
				System.err.println("list was" + list);
				System.out.println("cline vas:\n"+cline);
				e.printStackTrace();
				System.exit(1);
			}

		}
		return sp.toString();
	}

	private String intoLines(String canonized_line) {
		int brackets_opening=0;
		int state = 0;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < canonized_line.length(); i++) {
			char c = canonized_line.charAt(i);
			switch (state) {
			case 0:
				if (c == '\"')
					state = 1;
				break;
			case 1:
				if (c == '\"')
					state = 0;
				break;
			}
			if(c=='[')
				brackets_opening++;
			if(c==']')
				brackets_opening--;
			if(c=='[' && state==0)
				c=' ';
			if(c==']' && state==0)
				c=' ';
			
			if(state==0 && c==',')
				sb.append('\n');
			else
				sb.append(c);
		}
		return sb.toString();
	}
	
	// Not guaranteed
	public String createTTL(String cleanedRDF) {
		try {
			try {
				Model m = ModelFactory.createDefaultModel();
				InputStream istream = new ByteArrayInputStream(
						cleanedRDF.getBytes(StandardCharsets.UTF_8.name()));
				m.read(istream, null, "TTL");
				
				OutputStream ostream = new ByteArrayOutputStream();
				m.write(ostream, "TTL");
				
				return ostream.toString();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();

		}
		return "";
	}


	public static void main(String[] args) {

		CanonizedPattern cp=new CanonizedPattern();
		//cp.clean(
		//		"[http://ipfs/bim/56844e4c-03ba-42f6-bcca-719f78d5bcff http://www.buildingsmart-tech.org/ifcOWL/IFC2X3_TC1#globalId_IfcRoot http://ipfs/bim/IfcGloballyUniqueId_269538, http://ipfs/bim/56844e4c-03ba-42f6-bcca-719f78d5bcff http://www.buildingsmart-tech.org/ifcOWL/IFC2X3_TC1#relatedObjects_IfcRelAssociates http://ipfs/bim/6cde67dd-8eb3-481d-a286-c3cb2c57a695, http://ipfs/bim/56844e4c-03ba-42f6-bcca-719f78d5bcff http://www.buildingsmart-tech.org/ifcOWL/IFC2X3_TC1#relatedObjects_IfcRelAssociates http://ipfs/bim/6cde67dd-8eb3-481d-a286-c3de2c57a646, http://ipfs/bim/56844e4c-03ba-42f6-bcca-719f78d5bcff http://www.buildingsmart-tech.org/ifcOWL/IFC2X3_TC1#relatedObjects_IfcRelAssociates http://ipfs/bim/6cde67dd-8eb3-481d-a286-c3de2c57a6a6, http://ipfs/bim/56844e4c-03ba-42f6-bcca-719f78d5bcff http://www.buildingsmart-tech.org/ifcOWL/IFC2X3_TC1#relatingMaterial_IfcRelAssociatesMaterial http://ipfs/bim/IfcMaterial_165716, http://ipfs/bim/56844e4c-03ba-42f6-bcca-719f78d5bcff http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.buildingsmart-tech.org/ifcOWL/IFC2X3_TC1#IfcRelAssociatesMaterial, http://ipfs/bim/IfcGloballyUniqueId_269538 http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.buildingsmart-tech.org/ifcOWL/IFC2X3_TC1#IfcGloballyUniqueId, http://ipfs/bim/IfcGloballyUniqueId_269538 https://w3id.org/express#hasString \"1MX4vC0xf2zhpASPzurRp$\", http://ipfs/bim/IfcLabel_254649 http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.buildingsmart-tech.org/ifcOWL/IFC2X3_TC1#IfcLabel, http://ipfs/bim/IfcLabel_254649 https://w3id.org/express#hasString \"Metal - Paint Finish - Ivory, Matte\", http://ipfs/bim/IfcMaterial_165716 http://www.buildingsmart-tech.org/ifcOWL/IFC2X3_TC1#name_IfcMaterial http://ipfs/bim/IfcLabel_254649, http://ipfs/bim/IfcMaterial_165716 http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.buildingsmart-tech.org/ifcOWL/IFC2X3_TC1#IfcMaterial]");
		
		cp.clean("[http://ipfs/bim/72c1a7c7-332d-f441-8a8c-42453b558004 http://www.buildingsmart-tech.org/ifcOWL/IFC2X3_TC1#compositionType_IfcBuildingElementProxy http://www.buildingsmart-tech.org/ifcOWL/IFC2X3_TC1#COMPLEX, http://ipfs/bim/72c1a7c7-332d-f441-8a8c-42453b558004 http://www.buildingsmart-tech.org/ifcOWL/IFC2X3_TC1#description_IfcRoot http://ipfs/bim/IfcText_8312, http://ipfs/bim/72c1a7c7-332d-f441-8a8c-42453b558004 http://www.buildingsmart-tech.org/ifcOWL/IFC2X3_TC1#globalId_IfcRoot http://ipfs/bim/IfcGloballyUniqueId_8311, http://ipfs/bim/72c1a7c7-332d-f441-8a8c-42453b558004 http://www.buildingsmart-tech.org/ifcOWL/IFC2X3_TC1#name_IfcRoot http://ipfs/bim/IfcLabel_8233, http://ipfs/bim/72c1a7c7-332d-f441-8a8c-42453b558004 http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.buildingsmart-tech.org/ifcOWL/IFC2X3_TC1#IfcBuildingElementProxy, http://ipfs/bim/IfcGloballyUniqueId_8311 http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.buildingsmart-tech.org/ifcOWL/IFC2X3_TC1#IfcGloballyUniqueId, http://ipfs/bim/IfcGloballyUniqueId_8311 https://w3id.org/express#hasString \"1omQV7CotqGOgCGaKxLO04\", http://ipfs/bim/IfcLabel_8233 http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.buildingsmart-tech.org/ifcOWL/IFC2X3_TC1#IfcLabel, http://ipfs/bim/IfcLabel_8233 https://w3id.org/express#hasString \"Bolt layout\", http://ipfs/bim/IfcText_8312 http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.buildingsmart-tech.org/ifcOWL/IFC2X3_TC1#IfcText, http://ipfs/bim/IfcText_8312 https://w3id.org/express#hasString \"bolts member[3] mtrl(2) to member[8] mtrl(7)\", http://www.buildingsmart-tech.org/ifcOWL/IFC2X3_TC1#COMPLEX http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.buildingsmart-tech.org/ifcOWL/IFC2X3_TC1#IfcElementCompositionEnum, http://www.buildingsmart-tech.org/ifcOWL/IFC2X3_TC1#COMPLEX http://www.w3.org/1999/02/22-rdf-syntax-ns#type http://www.w3.org/2002/07/owl#NamedIndividual, http://www.buildingsmart-tech.org/ifcOWL/IFC2X3_TC1#COMPLEX http://www.w3.org/2000/01/rdf-schema#label \"COMPLEX\"]");
		
	}

}
