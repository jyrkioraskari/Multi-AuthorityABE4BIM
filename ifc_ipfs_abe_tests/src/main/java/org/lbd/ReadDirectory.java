package org.lbd;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.math3.stat.descriptive.rank.Median;


public class ReadDirectory {

	public ReadDirectory(String directory) {

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
		double min = Float.MAX_VALUE;
		double max = -Float.MAX_VALUE;
		double total = 0;
		int count = 0;
		List<Double> values=new ArrayList<>();

		if (file.getName().contains("Publishing")||file.getName().contains("Fetching_")) {
			float attr =-1;
			Scanner sc;
			try {
				sc = new Scanner(file);
				sc.useDelimiter("\n");

				while (sc.hasNext()) {
					String line = sc.next();
					if (line.startsWith("Round ")) {
						String[] s = line.split(" ");
						attr = Float.parseFloat(s[1]);
						double val = Float.parseFloat(s[5]);
						count++;
						total += val;
						if (val > max)
							max = val;
						if (val < min)
							min = val;
						values.add(val);
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			double avg = (total / count);
			Median median = new Median();
			double[] dvalues=new double[values.size()];
			for(int i=0;i<values.size();i++)
			{
				dvalues[i]=values.get(i);
			}
			double medianValue = median.evaluate(dvalues);			
			System.out.println(attr+"," + medianValue+ "," + (avg - min) + "," + (max - avg));
		}
	}

	public double getMedian(double[] values){
		 Median median = new Median();
		 double medianValue = median.evaluate(values);
		 return medianValue;
		}
	public static void main(String[] args) {

		new ReadDirectory("C:\\Users\\kiori\\OneDrive - Aalto University\\Research\\Attribute-based encyption for role based authentication for IFC-IPFS\\measurements\\4");

	}

}
