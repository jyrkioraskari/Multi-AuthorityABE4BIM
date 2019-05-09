package org.rdfcontext.signing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * This object exctract a canonicalized representation of a RDF graph  
 * 
 * @author Fabio Panaioli
 * 
 */
@SuppressWarnings("rawtypes")

public class RDFC14Ner  {
		
	/**
	 * @author Fabio Panaioli
	 * This object is the same of a triple with two comments in a N-Triple serialization
	 */
	public class C14NTtriple extends Object implements Comparable{
		protected Node subject;
		protected Node predicate;
		protected Node object;
		protected Node subjectID;
		protected Node objectID;
			
		protected boolean flag;
		
		protected C14NTtriple(Node s,Node p,Node o){
			this.subject=s;
			this.predicate=p;
			this.object=o;
			if(subject.isBlank()){
				this.subjectID=this.subject;
				this.subject= RDFC14Ner.TILDE;
			} else {
				this.subjectID=Node.NULL;
			}
			if(object.isBlank()){
				this.objectID=this.object;
				this.object=RDFC14Ner.TILDE;
			} else {
				this.objectID=Node.NULL;
			}
		}
		protected C14NTtriple(C14NTtriple st,Hashtable ht,int symCount){
			if(st.object.equals(RDFC14Ner.TILDE)){
				if(st.setTildeObject(ht)){
					this.object=st.object;
					this.objectID=st.objectID;
				} else {     //build a new ID in the hash table 
					ht.put(st.objectID,Node_URI.createURI("_:g"+RDFC14Ner.sixDigitsFormat.format(symCount)));
					this.object=(Node) ht.get(st.objectID);
					this.objectID=st.objectID;
					this.flag=true;
				}
			} else {
				this.object=st.object;
				this.objectID=Node.NULL;
			}
			
			this.predicate=st.predicate;
			
			if(st.subject.equals(RDFC14Ner.TILDE)){
				if(st.setTildeSubject(ht)){
					this.subject=st.subject;
					this.subjectID=st.subjectID;
				} else { //build a new ID in the hash table 
					ht.put(st.subjectID,Node_URI.createURI("_:g"+RDFC14Ner.sixDigitsFormat.format(symCount)));
					this.subject=(Node) ht.get(st.subjectID);
					this.subjectID=st.subjectID;
					this.flag=true;
				}
			} else {
				this.subject=st.subject;
				this.subjectID=Node.NULL;
			}
		}
		/**
		 * Replace the TILDE in object position with an hash table value if possible.
		 * @param ht - hashtable of Node object
		 * @return true if there is a entry in the hash table, false otherwise
		 */
		protected boolean setTildeObject(Hashtable ht){
			boolean test=false;		
			if(ht.containsKey(objectID)){
					object=(Node) ht.get(objectID);
					test=true;
			}
		return test;
		}
		/**
		 * Replace the TILDE in subject position with an hash table value if possible.
		 * @param ht - hashtable of Node object
		 * @return true if there is a entry in the hash table, false otherwise
		 */
		protected boolean setTildeSubject(Hashtable ht){
			boolean test=false;
			if(ht.containsKey(subjectID)){
					subject=(Node) ht.get(subjectID);
					test=true;
				}
		return test;	
		}

		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Object arg) throws ClassCastException {
			String current=subject.toString()+predicate.toString()+object.toString();					
			C14NTtriple cs=(C14NTtriple)arg;
			String ext=cs.subject.toString()+cs.predicate.toString()+cs.object.toString(); 		
			return current.compareTo(ext);
		}
		/**
		 * Build a triple string without subjectID and objectID
		 * @return triple string
		 */
		public String createTripleString(){
			return subject.toString()+" "+predicate.toString()+" "+object.toString();						
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString(){
			return subject.toString()+" "+predicate.toString()+" "+object.toString()+"#"+subjectID.toString()
			+"#"+objectID.toString()+" .";
			
		}		
	}
	
	public static final Node TILDE=Node_URI.createURI("~");
	public static final Node C14N_TRUE=Node_URI.createURI("http://www-uk.hpl.hp.com/people/jjc/rdf/c14n#true");
	public static final Node X=Node_URI.createURI("x");
	public static final DecimalFormat sixDigitsFormat=new DecimalFormat("000000");
	
	private Model model=ModelFactory.createDefaultModel();
	private String base;
	private ArrayList<String> canonical_string;
	
	/**
	 * This function will return an array list with a sequence of strings forming 
	 * the canonical reppresentation of the RDF that was fed
	 * each string reppresent a triple and by joining them in the given order 
	 * the result can be used as needed for example in digital signatures.
	 * @return the canonical string list.
	 */
	
	public ArrayList<String> getCanonicalStringsArray() {
		return canonical_string;
	}
	
	/**
	 * Will create AND immediately canonize the graph
	 * @param filePath the RDF file in RDF/XML
	 * @param base see Jena model.read() documentation
	 * @throws FileNotFoundException
	 */
	public RDFC14Ner(String filePath,String base) throws FileNotFoundException {
		model.read(new FileInputStream(filePath),base);
		doit(model);
	}

	/**
	 * Will create AND immediately canonize the graph
	 * @param rdfStream the RDF inputstream in RDF/XML
	 * @param base see Jena model.read() documentation
	 */
	
	public RDFC14Ner(InputStream rdfStream,String base) {
		model.read(rdfStream,base);
		doit(model);
	}
	
	/**
	 * Will create AND immediately canonicalize the graph
	 * @param rdfXml the RDF File (must be RDF/XML)
	 * @param base see Jena model.read() documentation
	 */

	public RDFC14Ner(Reader rdfXml, String base) {
		model.read(rdfXml,base);
		doit(model);
	}
	
	public RDFC14Ner(File rdffile,String base) throws FileNotFoundException {
		model.read(new FileInputStream(rdffile),base);
		doit(model);
	}
	public RDFC14Ner(Model model){
		doit(model);
	}
	
	private void doit(Model model) {
		
		this.model=model;
		StmtIterator st=model.listStatements();
		ArrayList a=new ArrayList();//an ArrayList of Triple object
		while(st.hasNext()) {
			a.add(st.nextStatement().asTriple());//fill the ArrayList of Triple object
		}
		canonical_string=pre_canonicalization(a,model);
	}
		
	/**
	 * Replace BlankNodeId(in object and subject position)with TILDE.
	 * The BlankNodeId has wrote in the subjectID and objectID fields of the
	 * C14NTtriple object
	 * @param a - ArrayList of Triple
	 * @return am - ArrayList of C14NTtriple
	 */
		private ArrayList putTilde(ArrayList a){
			ArrayList am=new ArrayList();
			for(int i=0;i<a.size();i++){
				Triple tmp= (Triple) a.get(i);			
	       		am.add(i,new C14NTtriple(tmp.getSubject(),tmp.getPredicate(),tmp.getObject()));
			}
			return am;		
		}
	/**
	 * Labelled TILDE node using gensym as _gNNNNNN if possible(hard to label node).
	 * @param a ArrayList di C14NTtriple
	 */
	 		
		private void labelledNode(ArrayList a){
			Hashtable ht=new Hashtable();// a look up table
			int symCount=1;// a gensym counter
			ArrayList af=new ArrayList();

			for(int i=0;i<a.size();i++){
				C14NTtriple t=(C14NTtriple) a.get(i);
				if (a.size()==1){ //case: a triple alone
					C14NTtriple tmp=new C14NTtriple(t,ht,symCount);
					af.add(tmp);
					break;
				}		
				if (i==0){ //the first triple hasn't precedent
					if((t.compareTo((C14NTtriple)a.get(i+1)))==0){ //compare only the triple
						af.add(t);
						continue;//if this triple is equals to the next then go to the next triple
					}else {
						C14NTtriple tmp=new C14NTtriple(t,ht,symCount);
						if (tmp.flag) symCount++;					
						af.add(tmp);
						continue;
					}
				}
				if((i>0)&&(i<a.size()-1)){ //a central triple has either precedent and successor
					if(((t.compareTo(a.get(i-1)))==0) || ((t.compareTo(a.get(i+1)))==0)){
						af.add(t);
						continue;
	        		}else {
	        			C14NTtriple tmp=new C14NTtriple(t,ht,symCount);
	        			if (tmp.flag) symCount++;
						af.add(tmp);
						continue;
					}
				}
				if (i==a.size()-1){  //the last triple hasn't successor
	        		if((t.compareTo(a.get(i-1)))==0){
	        			af.add(t);
	        			continue;
	    			}else {
	    				C14NTtriple tmp=new C14NTtriple(t,ht,symCount);
	    				if (tmp.flag) symCount++;
						af.add(tmp);
						continue;
					}
				}			
			}
			for(int i=0;i<af.size();i++){
				C14NTtriple t=(C14NTtriple) a.get(i);
				t.setTildeObject(ht);
				t.setTildeSubject(ht);			
			}
			a.clear();
			a.addAll(af);
		}
		
		/**
		 * Run One-step Deterministic Labelling algorithm. 
		 * @param a - ArrayList di Ttriple
		 * @return al - ArrayList di C14NTtriple
		 */
		private ArrayList one_step_algorithm(ArrayList a){
			//System.out.println("Begin One-Step algorithm...");
			ArrayList al=new ArrayList();
			//System.out.println("putTilde...");
			al=putTilde(a);
			//System.out.println("sort...");
			Collections.sort(al);
			//System.out.println("labelledNode...");
			labelledNode(al);
			//System.out.println("sort...");
			Collections.sort(al);

			return al;		
		}
		/**
		 * Determine if there aren't hard to label node
		 * @param a ArrayList di oggetti C14NTtriple
		 * @return true if there aren't hard to label node
		 */
		private boolean isAllLabelled(ArrayList a){
			boolean  test=true;
			for(int i=0;i<a.size();i++){
				C14NTtriple t=(C14NTtriple) a.get(i);
				if ((t.subject.equals(TILDE))||(t.objectID.equals(TILDE))){
					test=false;
				}
			}
			return test;
		}
		/**
		 * Remove from the model all triple with predicate c14n:true 
		 * @param a - ArrayList of C14NTtriple 
		 * @param model 
		 * @return A ArrayList of Triple
		 */

		private ArrayList removeTripleWithC14N(ArrayList a,Model model){
			//System.out.println("Remove triple with c14n:true....");
			ArrayList statementList=new ArrayList();// ArrayList of Statement to delete
			ArrayList tripleList=new ArrayList();
			for(int i=0;i<a.size();i++){   //fill the ArrayList of Statement to delete
				C14NTtriple t=(C14NTtriple) a.get(i);
				if(t.predicate.equals(C14N_TRUE)){
					Statement st=model.createStatement(
									   model.createResource(t.subjectID.getBlankNodeId()),
			                           model.createProperty("http://www-uk.hpl.hp.com/people/jjc/rdf/c14n#","true"),
									   model.createLiteral(t.object.toString()));
					statementList.add(st);
				}
			}		
			model.remove(statementList);
			//model.write(System.out);
			model.removeNsPrefix("c14n");
			StmtIterator st=model.listStatements();
			
			while(st.hasNext()) {
				tripleList.add(st.nextStatement().asTriple());
			}			
			return tripleList;		
		}
	/**
	 * Add to the model all triple with predicate c14n:true 
	 * @param a - ArrayList of C14NTtriple
	 * @return A ArrayList of Triple
	 */
		private ArrayList addTripleWithC14N(ArrayList a,Model model){
			//System.out.println("Add triple with c14n:true....");
			ArrayList statementList=new ArrayList();// ArrayList of Statement to add		
			ArrayList tripleList=new ArrayList();
			Hashtable ht=new Hashtable();
			int symCount=1;
			
			for(int i=0;i<a.size();i++){   //fill the ArrayList of Statement to add
				C14NTtriple t=(C14NTtriple) a.get(i);
				if(t.object.equals(TILDE)){
					if(!ht.containsKey(t.objectID)){														                           
						ht.put(t.objectID,X);// build a new triple									
						Statement st=model.createStatement(
								model.createResource(t.objectID.getBlankNodeId()),
								model.createProperty("http://www-uk.hpl.hp.com/people/jjc/rdf/c14n#","true"),
								model.createLiteral(symCount+""));					
						statementList.add(st);
						t.objectID=Node.NULL;
						symCount++;					
					}				
				}
				if(t.subject.equals(TILDE)){
					if(!ht.containsKey(t.subjectID)){
						ht.put(t.subjectID,X);					
						Statement st=model.createStatement(
								model.createResource(t.subjectID.getBlankNodeId()),
								model.createProperty("http://www-uk.hpl.hp.com/people/jjc/rdf/c14n#","true"),
								model.createLiteral(symCount+""));					
						statementList.add(st);					
						t.subjectID=Node.NULL;
						symCount++;					
					}				
				}
			}
			model.setNsPrefix("c14n","http://www-uk.hpl.hp.com/people/jjc/rdf/c14n#");
			model.add(statementList);
			//model.write(System.out);
			StmtIterator st=model.listStatements();
			while(st.hasNext()) {
				tripleList.add(st.nextStatement().asTriple());//A new ArrayList of Triple object
			}			
			return tripleList;		
		}
	/**
	 * Run the one-step algorithm and if necessary execute the precanonicalization steps
	 * @param a - ArrayList of Triple object
	 * @return The canonicString ArrayList of triple string
	 */
		private ArrayList pre_canonicalization(ArrayList a,Model model){
			//System.out.println("Begin Precanonicalization...");
			ArrayList canonicString=new ArrayList(); //ArrayList of string
			ArrayList pre_canonic=one_step_algorithm(a); // 1)run alg one_step
			
			if (isAllLabelled(pre_canonic)){  // if it's just canonic,it builds the canonicString
				//System.out.println("THE BLANK NODES ARE ALL LABELLED");
				for(int i=0;i<pre_canonic.size();i++){
					C14NTtriple t=(C14NTtriple) pre_canonic.get(i);
				    canonicString.add(i,t.createTripleString());
				}
			}else{         // if it isn't just canonic
				//System.out.println("THE BLANK NODES AREN'T ALL LABELLED");
				a=removeTripleWithC14N(pre_canonic,model); // 2)delete triple with predicate c14n:true 
				pre_canonic=one_step_algorithm(a);// 3) run already alg one_step
				a=addTripleWithC14N(pre_canonic,model); // 4)add triple with predicate c14n:true 
				pre_canonic=one_step_algorithm(a); // 5)run already alg one_step
				for(int i=0;i<pre_canonic.size();i++){
					C14NTtriple t=(C14NTtriple) pre_canonic.get(i);
				    canonicString.add(i,t.createTripleString());
				}			
			}
			return canonicString;		
		}

	/**
	 * @return the canonical string
	 */
	public String getCanonicalString() {
		return canonical_string.toString();
	}
		

}