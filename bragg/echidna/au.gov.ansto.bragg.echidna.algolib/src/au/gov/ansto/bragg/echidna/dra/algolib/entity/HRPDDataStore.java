package au.gov.ansto.bragg.echidna.dra.algolib.entity;
//
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import au.gov.ansto.bragg.common.dra.algolib.model.Experiment;
import au.gov.ansto.bragg.common.dra.algolib.processes.Iterable;
/**
 * @author jgw
 *
 */
public class HRPDDataStore extends Experiment implements Iterable {

	HRPDDataSet[] data;

	int lastIndex;
	
	
	public int length()
	{
		if(data == null)
			return 0;
		return data.length;
	}
	
	
	public void setData(HRPDDataSet[] data)
	{
		this.data = data;
		select(lastIndex);
	}
	
	public HRPDDataSet getScan(int index)
	{
		return data[index];
	}
	
 	/**
 	 * 
	 * @param ods  one D dataset object for output
	 * @param dir  output file path
	 * @param file  output file name
	 * @throws Exception
 	 */
	 public void  HRPDDataOutput(double[][] ods, String dir, String file) throws Exception {		        
			try
			{
				//String wdirectory = "D:\\opaldra\\xml\\";
				//String filename = "HRPDDataset.dat";

				File datafile = new File (dir, file);
			FileOutputStream dfos = new FileOutputStream(datafile);
			ObjectOutputStream doos = new ObjectOutputStream(dfos);
			doos.writeObject(ods);
			doos.close();
			}catch (IOException e)
			{
				System.out.println("Error" + e.getMessage());
			}
			

		}
	 	/**
	 	 * 
		 * @param ods  one D dataset object for output
		 * @param dir  output file path
		 * @param file  output file name
		 * @throws Exception
	 	 */
	 public void  HRPDDataOutput(double[] ods, String dir, String file) throws Exception {		        
			try
			{
				//String wdirectory = "D:\\opaldra\\xml\\";
				//String filename = "HRPDDataset.dat";

				File datafile = new File (dir, file);
			FileOutputStream dfos = new FileOutputStream(datafile);
			ObjectOutputStream doos = new ObjectOutputStream(dfos);
			doos.writeObject(ods);
			doos.close();
			}catch (IOException e)
			{
				System.out.println("Error" + e.getMessage());
			}
			

		} 
	 	/**
	 	 * 
		 * @param ods  one D dataset object for output
		 * @param dir  output file path
		 * @param file  output file name
		 * @throws Exception
	 	 */
	  public double [][] HRPDDataInput(String dir, String file) throws Exception {
			double [][] ids = null;  
			//String wdirectory = "D:\\opaldra\\xml\\";
			File datafile = new File (dir, file);
			try
			{		
			FileInputStream dfis = new FileInputStream(datafile);
			ObjectInputStream dois = new ObjectInputStream(dfis);
			ids = (double[][]) dois.readObject();
			ids.clone();
			}catch (IOException e)
			{
				System.out.println("Error" + e.getMessage());
			}
		return ids;
		  
	  }
	  
	 	/**
	 	 * 
		 * @param ods  one D dataset object for output
		 * @param dir  output file path
		 * @param file  output file name
		 * @throws Exception
	 	 */
	  public void  HRPDDataAsciiOutput2D(double[][] ods, String dir, String file) throws Exception {		        
			try
			{
				//String wdirectory = "D:\\opaldra\\xml\\";
				//String filename = "HRPDDataset.dat";

			File datafile = new File (dir, file);
			FileWriter dfos = new FileWriter(datafile,false);
			BufferedWriter doos = new BufferedWriter(dfos);		
			int yBin = ods.length;
			int xBin = ods[0].length;
		   	//System.out.println("xBin= " + xBin + " yBin= "+yBin); 
			String [][] sods = new String [yBin][xBin];		
			for (int j=0; j<yBin; j++){
			   for (int i=0; i<xBin; i++)
			     {
				    sods[j][i] = String.valueOf(ods[j][i]);
			        doos.write(sods[j][i]);
			        doos.write(" ");
			        //doos.close();
			   }
			   doos.newLine();
			}
			doos.close();
			}catch (IOException e)
			{
				System.out.println("Error" + e.getMessage());
			}
		}
	  
	 	/**
	 	 * 
		 * @param ods  one D dataset object for output
		 * @param dir  output file path
		 * @param file  output file name
		 * @throws Exception
	 	 */
	  public void  HRPDDataAsciiOutput1D(double[] ods, String dir, String file) throws Exception {		        
			try
			{
				//String wdirectory = "D:\\opaldra\\xml\\";
				//String filename = "HRPDDataset.dat";

			File datafile = new File (dir, file);
			FileWriter dfos = new FileWriter(datafile,false);
			BufferedWriter doos = new BufferedWriter(dfos);		
			int xBin = ods.length;

			String []sods = new String [xBin];		
			for (int i=0; i<xBin; i++){
				    sods[i] = String.valueOf(ods[i]);
			        doos.write(sods[i]);
			        //doos.close();
			   doos.newLine();
			}
			doos.close();
			}catch (IOException e)
			{
				System.out.println("Error" + e.getMessage());
			}
		}


		public void select(int index) {
			// TODO Auto-generated method stub
			
		} 
}
