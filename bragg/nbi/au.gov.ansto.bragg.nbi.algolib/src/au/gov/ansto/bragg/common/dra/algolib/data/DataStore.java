package au.gov.ansto.bragg.common.dra.algolib.data;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

import au.gov.ansto.bragg.common.dra.algolib.model.Experiment;
import au.gov.ansto.bragg.common.dra.algolib.processes.Iterable;


/**
 * @author  jgw  A public class for data output store. This class will provide to output   binary data files and ascii data file
 * @param ods    data flow object 
 * @param dir    data output location
 * @param file   data output fiel name
 */
public class DataStore extends Experiment implements Iterable {

	/**
	 * @uml.property  name="data"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
	DataSet[] data;
	int lastIndex;
	
	public DataStore(DataSet[] data)
	{
		this.data = data;
	}
	
	public DataStore() {
		// TODO Auto-generated constructor stub
	}

	public int length()
	{
		if(data == null)
			return 0;
		return data.length;
	}
	
	public void select(int index) {
		if(data == null)
			return;
		lastIndex = index;
	}
	
	/**
	 * @param data  the data to set
	 * @uml.property  name="data"
	 */
	public void setData(DataSet[] data)
	{
		this.data = data;
		select(lastIndex);
	}
	
	public DataSet getScan(int index)
	{
		return data[index];
	}
	
 	/**
 	 * 
	 * @param ods  two D dataset object for output
	 * @param dir  output file path
	 * @param file  output file name
	 * @throws Exception
 	 */
	 public void  DataOutput(Object[][] ods, String dir, String file) throws Exception {		        
			try
			{
				//String wdirectory = "D:\\opaldra\\xml\\";
				//String filename = "HIPDDataset.dat";

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
	 public void  DataOutput(Object[] ods, String dir, String file) throws Exception {		        
			try
			{
				//String wdirectory = "D:\\opaldra\\xml\\";
				//String filename = "HIPDDataset.dat";

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
	 	 * A program to read ascii data file from a file server.
		 * @param dir  iutput file path
		 * @param file  iutput file name
		 * @param nrow: number of row  for data file  (detector counts), I fyou don't know exactly number, you can set it is 0.
		 * @param ncolu: number of column  for data file  (detector number or nTube * nScan),
		 *                 If you don't know exactly number, you can set it is 0.
		 * @throws Exception
	 	 */
	  public double[]  ASCIIData1DInput(String dir, String file ,int nrow) throws Exception {
			ArrayList<Double>  ids = new ArrayList<Double>();
	
			//String wdirectory = "D:\\opaldra\\xml\\";


			try
			{		
			File datafile = new File (dir, file);
	  		FileReader dois = new FileReader(datafile);
			BufferedReader idf = new BufferedReader(dois);	

			if ( nrow != 0 ){
		    for (int j = 0;  j <  nrow; j++ )
				{
				String srline = idf.readLine();
//				System.out.println("Input Data: " + srline);
				ids.add(Double.parseDouble(srline));    
			       }
				
			} else {
				int nl = 0; 			
				String srline = idf.readLine();				
				while (srline != null)
				{		 
					ids.add(Double.parseDouble(srline));    
					nl++;			 
				    srline = idf.readLine();	
				}			
			}

			}catch (IOException e)
			{
				System.out.println("Error" + e.getMessage());
			}
			
//			double[] dids = new double [ ids.lastIndexOf(ids)];	
//			System.out.println("Number of IndexLinst: " +  ids.lastIndexOf(ids));
//			int nob = 0;
//			for (double xinten:ids) {
//				dids[nob] = xinten;
//				nob++;
//			}
			double[] dids = new double [ ids.size()];
			for( int j = 0; j < ids.size(); j++){
//				System.out.println("Data Input:  " +  ids.get(j));
				dids[j] = ids.get(j);
			}
			return dids;
		  
	  }
	 
	 	/**
	 	 * A program to read ascii data file from a file server.
		 * @param dir  iutput file path
		 * @param file  iutput file name
		 * @param nrow: number of row  for data file  (detector counts), I fyou don't know exactly number, you can set it is 0.
		 * @param ncolu: number of column  for data file  (detector number or nTube * nScan),
		 *                 If you don't know exactly number, you can set it is 0.
		 * @throws Exception
	 	 */
	  public double [][] ASCIIData2DInput(String dir, String file ,int ncolu, int nrow) throws Exception {
			double [][] ids = new double[nrow][ncolu];  
			//String wdirectory = "D:\\opaldra\\xml\\";
			int nl = 0;
			ArrayList<Double>  dids = new ArrayList<Double>();
			try
			{		
			File datafile = new File (dir, file);
	  		FileReader dois = new FileReader(datafile);
			BufferedReader idf = new BufferedReader(dois);	

			if (ncolu != 0 &&  nrow != 0 ){
		    for (int j = 0;  j <  nrow; j++ )
				{
				String srline = idf.readLine();
				StringTokenizer stz = new StringTokenizer( srline,  " ");			
				    for(int i= 0; i< ncolu; i++) {				
					    ids[j][i] = Double.parseDouble(stz.nextToken());
//						 String pas=stz.nextToken();
//						 dids.add( Double.parseDouble(pas));	
			       }
				}

			} else {
				 nl = 0; 			
				String srline = idf.readLine();				
				while (srline != null)
				{
					 nl++;
					 int ie = 0;
				StringTokenizer stz = new StringTokenizer( srline,  " ");
				
					 while (stz.hasMoreTokens())
					 {
						 ie++;
						 String pas=stz.nextToken();
						 dids.add( Double.parseDouble(pas));	
			       }
				srline = idf.readLine();	
				}
			}
			}catch (IOException e)
			{
				System.out.println("Error" + e.getMessage());
			}	
			
				int nelem = 0;
				int nhelm =0;
				if(ncolu != 0 &&  nrow != 0){
					nhelm = ncolu;
					nl = nrow;
				}
				else nhelm = dids.size() / nl;
				double[][] ditds = new double [nl][ nhelm];		
				for( int j = 0; j < nl; j++){
					for( int i = 0; i <  nhelm; i++){
					if(ncolu != 0 &&  nrow != 0 ) {
						ditds[j][i] = ids[j][i];
					}
					else  	ditds[j][i] = dids.get(nelem);
					nelem++;
				     }		
				}			


			return ditds;	    		  
	  }
	 
	 	/**
	 	 * 
		 * @param ids  two D dataset object for output
		 * @param dir  output file path
		 * @param file  output file name
		 * @throws Exception
	 	 */
	  public Object [][] DataInput(String dir, String file) throws Exception {
			Object [][] ids = null;  
			//String wdirectory = "D:\\opaldra\\xml\\";
			File datafile = new File (dir, file);
			try
			{		
			FileInputStream dfis = new FileInputStream(datafile);
			ObjectInputStream dois = new ObjectInputStream(dfis);
			ids = (Object[][]) dois.readObject();
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
		 * @param dir  output file path,
		 * @param file  output file name
		 * @throws Exception
	 	 */
	  public void  DataAsciiOutput2D(double[][] ods, String dir, String file) throws Exception {		        
			try
			{
				//String wdirectory = "D:\\opaldra\\xml\\";
				//String filename = "HIPDDataset.dat";

			File datafile = new File (dir, file);
// true for append data, false for creating new file.
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
		 * @param dir  output file path,
		 * @param file  output file name
		 * @throws Exception
	 	 */
	  public void  dataTxtOutput2DCollumn(double[][] ods, String dir, String file) throws Exception {		        
			try
			{
				//String wdirectory = "D:\\opaldra\\xml\\";
				//String filename = "HIPDDataset.dat";

			File datafile = new File (dir, file);
//true for append data, false for creating new file.
			FileWriter dfos = new FileWriter(datafile,false);
			BufferedWriter doos = new BufferedWriter(dfos);		
			int yBin = ods.length;
			int xBin = ods[0].length;
		   	//System.out.println("xBin= " + xBin + " yBin= "+yBin); 
			String [][] sods = new String [yBin][xBin];		
			   for (int i=0; i<xBin; i++)
			     {
			     for (int j=0; j<yBin; j++){
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
	  public void  DataAsciiOutput1D(double[] ods, String dir, String file) throws Exception {		        
			try
			{
				//String wdirectory = "D:\\opaldra\\xml\\";
				//String filename = "HIPDDataset.dat";

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
}
