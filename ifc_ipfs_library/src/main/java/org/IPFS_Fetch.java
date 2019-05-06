package org;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

public abstract class IPFS_Fetch {
	protected List<String> timelog=new LinkedList<>();  
	protected  final DateFormat df = new SimpleDateFormat("yyyyMMdd HHmmss");
	protected  String reportDate;
	protected  String node;


	synchronized protected void addLog(String txt)
	{
		timelog.add(txt);
	}


	protected void writeToFile(String txt,boolean jena) {

		try {
			PrintWriter pw = new PrintWriter(
					new FileOutputStream(new File(node+"_"+reportDate + ".jena_" + jena+".txt"), true /* append = true */));
			pw.write(txt);
			pw.write("\n");
			System.out.println(node+"_"+reportDate + ".jena_" + jena+".txt  content: "+txt);
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
