package org.lbd;

import java.io.File;
import java.util.Scanner;

import org.apache.commons.math3.stat.descriptive.rank.Median;


public class ReadFileDirectoryLogsIntoWolfgramAlphaNotation {

	public ReadFileDirectoryLogsIntoWolfgramAlphaNotation(String directory) {

		File curDir = new File(directory);
		getAllFiles(curDir);
	}

	private void getAllFiles(File curDir) {

		File[] filesList = curDir.listFiles();
		for (File f : filesList) {
			if (f.isFile()) {
				handle(f);
			}
		}
	}

	private void handle(File file) {
		//System.out.print("linear fit ");
		if (file.getName().contains("Publishing")) {
			
			Scanner sc;
			try {
				sc = new Scanner(file);
				sc.useDelimiter("\n");

				//boolean first=true;
				while (sc.hasNext()) {
					String line = sc.next();
					if (line.startsWith("Round ")) {
						String[] s = line.split(" ");
						float size = Float.parseFloat(s[2]);
						double time = Float.parseFloat(s[6]);
						//if(!first)
						//	System.out.print(",");
						//System.out.print("{"+size+","+time+"}");
						System.out.println(""+size+"\t"+time+"");
						//first=false;
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println();
		}
	}

	public double getMedian(double[] values){
		 Median median = new Median();
		 double medianValue = median.evaluate(values);
		 return medianValue;
		}
	public static void main(String[] args) {

		new ReadFileDirectoryLogsIntoWolfgramAlphaNotation("C:\\Users\\kiori\\OneDrive - Aalto University\\Research\\Attribute-based encyption for role based authentication for IFC-IPFS\\measurements\\publishing_ifc_files one_autenticator");

	}

}
