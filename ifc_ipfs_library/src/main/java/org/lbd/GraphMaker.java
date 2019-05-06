package org.lbd;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class GraphMaker {
	
	
	public String create(Model  model)
	{
		final Map<RDFNode,String> rs=new HashMap<>();
		final Set<RDFNode> rset=new HashSet<>();
		
		
		final StringBuffer sb=new StringBuffer();
		sb.append("digraph {\n");
		model.listStatements().forEachRemaining(x -> {
			Resource s=x.getSubject();
			Resource p=x.getPredicate();
			RDFNode o=x.getObject();
			if(rset.add(s))
			{
				String s_name=createNodeName();
				if(s.isAnon())
				   sb.append(" "+s_name+" [label=\"\"]\n");
				else
					sb.append(" "+s_name+" [label=\""+s.asResource()+"\"]\n");	
				rs.put(s, s_name);
			}
			
			if(rset.add(o))
			{
				String o_name=createNodeName();
				if(o.isLiteral())
	 			sb.append(" "+o_name+" [label=\"'"+o.asLiteral().getLexicalForm()+"'\"]\n");
				else
					if(o.isAnon())
					  sb.append(" "+o_name+" [label=\"\"]\n");
					else
					  sb.append(" "+o_name+" [label=\""+o.asResource()+"\"]\n");
				rs.put(o, o_name);
			}
			String s_name=rs.get(s);
			String o_name=rs.get(o);
			sb.append(" "+s_name+" -> "+o_name+" [label=\""+p.getLocalName()+"\"];\n");
		});
		sb.append("\n}\n");
		return sb.toString();
	}

	private long inx=0;
    private String createNodeName()
    {
    	return "n"+inx++;
    }
	
	
}
