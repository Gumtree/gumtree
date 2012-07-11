/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */
package au.gov.ansto.bragg.wombat.dra.algolib.core;

import java.io.*;
import java.lang.*;
import javax.swing.JOptionPane;

import au.gov.ansto.bragg.common.dra.algolib.math.OneDGaussianFunction;
import au.gov.ansto.bragg.common.dra.algolib.processes.Signal;

import au.gov.ansto.bragg.wombat.dra.algolib.entity.*;
import au.gov.ansto.bragg.wombat.dra.algolib.processes.*;

/**
 *OPAL Neutron Scttering software package designed to 
 *  make online data reduction.
 *
 * @author J.G.Wang
 *
 */
public class HIPDReductionTestImpl extends HIPDProcessor implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		final HIPDDataSet[] hdata = null;
		HIPDDetector hdetect= new HIPDDetector();
		int nTubes   = hdetect.xPixels;
		int nycount = hdetect.yPixels;
		double detecArch =hdetect.radialCurv;
		double mu1 = 38.0F;
		double mu2 = 75.0F;
		double mu3 = 98.0F;		
		double sigma1 = 5.0F;
		double sigma2 = 4.0F;
		double sigma3 = 3.0F;		
		double [] thetaVect = new double[nTubes];
        HIPDReductionTestImpl test =new HIPDReductionTestImpl();
		HIPDDataProvider pdata =new  HIPDDataProvider();
		HIPDCorrectEfficiencyImpl hct = new HIPDCorrectEfficiencyImpl();
		HIPDGeometryCorrectImpl gcorrect = new HIPDGeometryCorrectImpl();
		VerticalIntegrationImpl vig  = new VerticalIntegrationImpl();
		HorizontalIntegrationImpl hig  = new HorizontalIntegrationImpl();
		GlobalIntegrationImpl gig  = new GlobalIntegrationImpl();
		 test.setNScan(10);
		 int nScan = test.getNScan();
		 double twoTheta0=0;
		 double dTheta =hdetect.seperation;
		boolean corraction = false;
		HIPDDataStore store = new  HIPDDataStore();
//		double [][][] genDataSet = new double [nScan][nTubes][nycount];
		double [][] sds   = new double [nycount][nTubes];
		double [][] geods = new double [nycount][nTubes];
		double [][] sendt = new double [nycount][nTubes];
		

/**
 * Generete two D data sets with double Guassian distribution
 * 
 */
		System.out.println("Start to generate two D MC HRPD data sets");
		
		sendt = pdata.genEffDataSet();
		
//		genDataSet = pdata.genGaussianDataSet();

		
		System.out.println("MC data generated!");
		
		//int nDataSet = genDataSet.length;
		//int nDetector = genDataSet[0].length;
		//int nDetcount = genDataSet[0][0].length;
        int iDataSet;

  
//        	System.out.println("Check status!");
			for (int j= 0; j < nTubes; j++)
				for (int k= 0; k < nycount; k++)
				{
			//		sds[k][j] = genDataSet[iDataSet][k][j];
					sds[k][j] = (double) ((double)(15)*(double)(Math.random()+0.5)*(1.0 
							+ OneDGaussianFunction.OneDGaussian(j, mu1, sigma1)
							+ OneDGaussianFunction.OneDGaussian(j, mu2, sigma2)
							+ OneDGaussianFunction.OneDGaussian(j, mu3, sigma3)));
	//				System.out.println("Data Sample:" + sds[k][j]);
				}
/**
 * Apply for efficiency correction
 */
			int flag =1;
			double thresh= (double) 10E-10;
			boolean inverse = false;
			double[][] 	 cfds = hct.doSensitivity(sds, sendt, flag , thresh, inverse);
		

 
       	        
		double[][] cgds = gcorrect.correctGeometry(cfds, geods, detecArch, thetaVect,  corraction, null);    

		        try
		        {
//		        store.HIPDDataOutput(cgds, "D:\\echidnadra\\data", "HGeoCDataSet.dat");
		        store.HIPDDataAsciiOutput2D(cgds, "D:\\echidnadra\\data", "HGeoCDataSet.txt");
		        } catch (Exception e)
		        {
		        	e.printStackTrace();
		        }
       	
//       	double[][] cgds = stds;
		System.out.println("Geometry correction done, Check status!");     
/**
 * Following data set stiching, Data integration will be applied
 */
		System.out.println("Start HRPD data integration!");	


			System.out.println("Please select integration method:" 
						+ "Horizontal Integration "
						+ "Vertical Integration"
						+ "Global Integration from dialog box"
						);
			String intename = JOptionPane.showInputDialog(null,
						"Please select integration method: Horizontal  " +
						"or Vertical and Global Integration (Horizontal/Vertical/Global) ");
			JOptionPane.showMessageDialog(null, "Hi, your selcetion is: " + intename);

	if (intename.equals("Horizontal"))
		{	
			String minboxin = JOptionPane.showInputDialog(null,	"Please input your minimum value" +
							" of integration box:");
			JOptionPane.showMessageDialog(null, "The bottom value of box is: " + minboxin);

			String maxboxin = JOptionPane.showInputDialog(null,	"Please input your maximum value" +
				" of integration box:");
			JOptionPane.showMessageDialog(null, "The top value of box is: " + maxboxin);
										
			String numbinin = JOptionPane.showInputDialog(null,	"Please input number " +
							"of bins you like integrate dada set in the area you selected");
			JOptionPane.showMessageDialog(null, "The bottom value of box is:" + numbinin);
			
			int minBox  = Integer.parseInt(minboxin);
			int maxBox  = Integer.parseInt(maxboxin);
			int numBins = Integer.parseInt(numbinin);

			int numSlices = numBins;

			double[] err = new double[numSlices];
			double[] stdDev = new double[numSlices];
			double xOrigin = 0.0F;
			double yOrigin = 0.0F;
			double pos = hdetect.radialCurv;
			
			double [][] hInteds = new double[2][numSlices];	
			double [] hint1d  = new double[numSlices];	
			hInteds = hig.findHorizontalIntegration(cgds, numSlices, minBox, maxBox,
					xOrigin, yOrigin, pos, null);
		            hint1d =hInteds[0];
	        try
	        {
	        //store.HIPDDataOutput(cgds, "D:\\echidnadra\\data", "HInteCDataSet.dat");
	        	store.HIPDDataAsciiOutput1D(hint1d, "D:\\echidnadra\\data", "HInteCDataSet.txt");
	        	store.HIPDDataAsciiOutput2D(hInteds, "D:\\echidnadra\\data", "HInteDataErr.txt");
	        } catch (Exception e)
	        {
	        	e.printStackTrace();
	        }
			System.out.println("Horizontal integration done, Check status!"); 			
				}
	
	if (intename.equals("Vertical"))
	{	
		String minboxin = JOptionPane.showInputDialog(null,	"Please input your  value" +
						" for the leftside of integration box:");
		JOptionPane.showMessageDialog(null, "The left side value of box is:" + minboxin);

		String maxboxin = JOptionPane.showInputDialog(null,	"Please input your value" +
			" for the right side of integration box:");
		JOptionPane.showMessageDialog(null, "The right side value of box is:" + maxboxin);
									
		String numbinin = JOptionPane.showInputDialog(null,	"Please input number " +
						"of bins you like integrate dada set in the area you selected");
		JOptionPane.showMessageDialog(null, "The bottom value of box is:" + numbinin);
		
		int minBox  = Integer.parseInt(minboxin);
		int maxBox  = Integer.parseInt(maxboxin);
		int numBins = Integer.parseInt(numbinin);
		int numSlices = numBins;

		double[] err = new double[numSlices];
		double[] stdDev = new double[numSlices];
		double xOrigin = 0.0F;
		double yOrigin = 0.0F;
		double pos = hdetect.radialCurv;
		double [][] vInteds = new double[2][numSlices];	
		
		vInteds = vig.findVerticalIntegration(cgds, numSlices, minBox, maxBox,
				xOrigin, yOrigin, pos, null);
        try
        {
        //store.HIPDDataOutput(cgds, "D:\\echidnadra\\data", "VInteCDataSet.dat");
        	store.HIPDDataAsciiOutput1D(vInteds[0], "D:\\echidnadra\\data", "VInteCDataSet.txt");
        } catch (Exception e)
        {
        	e.printStackTrace();
        }	
		System.out.println("Vertical integration done, Check status!"); 
	}
	if (intename.equals("Global"))
	{			
		String numbinin = JOptionPane.showInputDialog(null,	"Please input number " +
						"of bins you like integrate dada set in the area you selected");
		JOptionPane.showMessageDialog(null, "The bottom value of box is:" + numbinin);

		int numBins = Integer.parseInt(numbinin);

		int numSlices = numBins;

		double [][] gInteds = new double[2][numSlices];
	
		gInteds = gig.findGlobalIntegration(cgds, numSlices, null);

        try
        {
        //store.HIPDDataOutput(cgds, "D:\\echidnadra\\data", "GInteCDataSet.dat");
        	store.HIPDDataAsciiOutput1D(gInteds[0], "D:\\echidnadra\\data", "GInteCDataSet.txt");
        } catch (Exception e)
        {
        	e.printStackTrace();
        }
	}    
		  }
//		System.out.println("Global integration done, Check status!"); 
	



	/**
	 * 
	 * @param ods  two D dataset object for output
	 * @param dir  output file path
	 * @param file  output file name
	 * @throws Exception
	 */
		        
 public void  HIPDDataOutput(double[][] ods, String dir, String file) throws Exception {		        
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
 public void  HIPDDataOutput(double[] ods, String dir, String file) throws Exception {		        
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
  public double [][] HIPDDataInput(String dir, String file) throws Exception {
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

  public void  HIPDDataAsciiOutput2D(double[][] ods, String dir, String file) throws Exception {		        
		try
		{
			//String wdirectory = "D:\\opaldra\\xml\\";
			//String filename = "HIPDDataset.dat";

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
  public void  HIPDDataAsciiOutput1D(double[] ods, String dir, String file) throws Exception {		        
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
  

@Override
public String getName() {
	// TODO Auto-generated method stub
	return null;
}


@Override
public String getDescription() {
	// TODO Auto-generated method stub
	return null;
}


@Override
protected Signal processNew(Signal in) {
	// TODO Auto-generated method stub
	return null;
}
	
	
}
