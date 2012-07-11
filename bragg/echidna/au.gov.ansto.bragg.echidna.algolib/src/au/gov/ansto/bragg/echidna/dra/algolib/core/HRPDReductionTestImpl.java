/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */
package au.gov.ansto.bragg.echidna.dra.algolib.core;

import java.io.*;
import java.lang.*;
import javax.swing.JOptionPane;

import au.gov.ansto.bragg.common.dra.algolib.math.OneDGaussianFunction;
import au.gov.ansto.bragg.common.dra.algolib.processes.Signal;

import au.gov.ansto.bragg.echidna.dra.algolib.entity.*;
import au.gov.ansto.bragg.echidna.dra.algolib.processes.*;

/**
 *OPAL Neutron Scttering software package designed to 
 *  make online data reduction.
 *
 * @author J.G.Wang
 *
 */
public class HRPDReductionTestImpl extends HRPDProcessor implements Serializable
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
		final HRPDDataSet[] hdata = null;
		HRPDDetector hdetect= new HRPDDetector();
		int nTubes   = hdetect.xpixels;
		int nCount = hdetect.ypixels;
		double detecArch =hdetect.horisonCurv;
		double mu1 = 38.0F;
		double mu2 = 75.0F;
		double mu3 = 98.0F;		
		double sigma1 = 5.0F;
		double sigma2 = 4.0F;
		double sigma3 = 3.0F;		
		double [] thetaVect;
		double[][] datStitched;
		double[][] datInted;
        HRPDReductionTestImpl test =new HRPDReductionTestImpl();
		HRPDDataProvider pdata =new  HRPDDataProvider();
		HRPDCorrectionImpl hct = new HRPDCorrectionImpl();
		HRPDGeometryCorrectImpl gcorrect = new HRPDGeometryCorrectImpl();
		VerticalIntegrationImpl vig  = new VerticalIntegrationImpl();
		HorizontalIntegrationImpl hig  = new HorizontalIntegrationImpl();
		GlobalIntegrationImpl gig  = new GlobalIntegrationImpl();
		 test.setNScan(50);
		 int nScan = test.getNScan();
		 double twoTheta0=0;
		 double dTheta =hdetect.seperation;
		boolean corraction = false;
		HRPDDataStore store = new  HRPDDataStore();
//		double [][][] genDataSet = new double [nScan][nTubes][nCount];
		double [][] sds   = new double [nCount][nTubes];
		double [][] geods = new double [nCount][nTubes];
		double [][][] itds= new double [nScan][nCount][nTubes];
		double [][] sendt = new double [nCount][nTubes];
		double [] calsum = new double [nTubes];		
		double[] stepsize =new double[nScan];
		double[][] twotheta = new double[nScan][nTubes];
		double twotheta0 = 0;
		double tspacer = 1.25;
		double binsize = 0;


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

        for (iDataSet= 0; iDataSet < nScan; iDataSet++)
		  {
//        	System.out.println("Check status!");
			for (int j= 0; j < nTubes; j++)
				for (int k= 0; k < nCount; k++)
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
		
/**
* After efficiency correction set by set, put all two D dataset  to three D 
* dataset to make stiching
*/
		        for (int j= 0; j < nTubes; j++)
					for (int k= 0; k < nCount; k++)
					{		        

						itds[iDataSet][k][j] = cfds[k][j];	
//		System.out.println("Data output for stiching [" + iDataSet +"]" + 
//						"[" + k + "]" + "["+  j + "]" + "= " + itds[iDataSet][k][j]);						
					}
				
//		        try
//		        {
////		        store.HRPDDataOutput(cfds, "d:\\opaldra\\xml\\", "HRPDDataSet" + iDataSet + ".dat");
//		        store.HRPDDataAsciiOutput2D(cfds, "D:\\dragui\\data", "HRPDDataSet" + iDataSet + ".txt");
//		        } catch (Exception e)
//		        {
//		        	e.printStackTrace();
//		        }
		  } //data sets generation and efficiency correction
        
    	System.out.println("Finished efficiency correction, Check status!");      
/**
 * Apply for data set stiching
 * 
 */	
    	/**
    	 * Apply for HRPD data sets stiching. nScan data sets with xPixels * yPixels elements
    	 * will be converted (xPixels * nScan) * yPixels
    	 */
//    	double[][]  stds = new double [nCount][nScan*nTubes];
        HRPDDataSetStitchImpl hdss = new HRPDDataSetStitchImpl();
		for(int n = 0; n < nScan; n++)
			for(int m = 0; m < nTubes; m++)
			{
				twotheta[n][m] = stepsize[n]*n + m*tspacer;

			}
		
		double steps = 0.025;
    double lengthCurve =  hdetect.horisonCurv +nScan * stepsize[1];
    int nBins = (int) (lengthCurve / steps + 0.5);
        thetaVect = new double[nBins];
		for (int k = 0; k < nBins; k++)
			thetaVect[k] = twotheta0 + k * stepsize[1];

         datStitched = new double[nBins][nCount];
         
         for (int n = 0; n <nScan; n++)
        	 stepsize[n] = 0.025;
         
//         datStitched = hdss.echidnaDataStitch(itds, twotheta, stepsize, binsize, twotheta0);
         
         datStitched = hdss.echidnaIdealDataStitch(itds, twotheta, stepsize, binsize, twotheta0);
// 		for(int j=0;j<nCount;j++)
// 		System.out.println("Return Data: " + itds[j][100]);
    	System.out.println("Finished data stitching, Check status!");      
    
//    	stds = hdss.multiDataSetStich(itds,nTubes,nCount,nScan,twoTheta0,dTheta); 
		
        try
        {
 //       store.HRPDDataOutput(stds, "D:\\dragui\\data", "HStichDataSet.dat");
        	store.HRPDDataAsciiOutput2D(datStitched, "D:\\project\\data", "HStichDataSeta.txt");
        } catch (Exception e)
        {
        	e.printStackTrace();
        }
        System.out.println("paras for stitching: " + nCount +  ", " + nTubes +", "+ nScan + ", " +  twoTheta0  +  ", " + dTheta);
       	System.out.println("Finished data set stiching, Check status!");        
       	/**
//       	 * Apply for HRPD data Geometry correction. nScan data sets with  (xPixels * nScan) * yPixels
//       	 */
//       		thetaVect = new double[nBins];
//       	        for (int i=0; i<nBins;i++) {
//       	       	thetaVect[i] = i * steps;
     
 //      	        	thetaVect[i] = (double)i * Math.random();
 //      	        }
       	  int datlen = datStitched.length;
       	  int thlen   = datStitched[0].length;
       	         thetaVect = new double[thlen];
       	      thetaVect = datStitched[datlen-1];
				double dz = hdetect.pixelHeight;
				double[][] zpVect = new double[nCount][thlen];
				for ( int j = 0; j < nCount; j++) {
					for (int k = 0; k <thlen; k++) {
						zpVect[j][k] = j *dz -150.0;
					}
				}
 //      	        double [][] zcoord = new double[nBins][nCount];
       	        
		double[][] cgds = gcorrect.correctGeometry(datStitched, geods, detecArch, thetaVect,  corraction, zpVect);    

		        try
		        {
//		        store.HRPDDataOutput(cgds, "D:\\project\\data", "HGeoCDataSet.dat");
		        store.HRPDDataAsciiOutput2D(cgds, "D:\\project\\data", "HGeoCDataSet.txt");
		        } catch (Exception e)
		        {
		        	e.printStackTrace();
		        }
       	
//       	double[][] cgds = stds;
		System.out.println("Geometry correction done, Check status!");     

   /**
      * Following data set stiching, Data integration will be applied

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
			hInteds = hig.findHorizontalIntegration(datStitched, numSlices, minBox, maxBox,
					xOrigin, yOrigin, pos, null);
		            hint1d =hInteds[0];
	        try
	        {
	        //store.HRPDDataOutput(cgds, "D:\\project\\data", "HInteCDataSet.dat");
	        	store.HRPDDataAsciiOutput1D(hint1d, "D:\\project\\data", "HInteCDataSet.txt");
	        	store.HRPDDataAsciiOutput2D(hInteds, "D:\\project\\data", "HInteDataErr.txt");
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
		
		vInteds = vig.findVerticalIntegration(datStitched, numSlices, minBox, maxBox,
				xOrigin, yOrigin, pos, null);
        try
        {
        //store.HRPDDataOutput(cgds, "D:\\project\\data", "VInteCDataSet.dat");
        	store.HRPDDataAsciiOutput1D(vInteds[0], "D:\\project\\data", "VInteCDataSet.txt");
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
	
		gInteds = gig.findGlobalIntegration(datStitched, numSlices, null);

        try
        {
        //store.HRPDDataOutput(cgds, "D:\\project\\data", "GInteCDataSet.dat");
        	store.HRPDDataAsciiOutput1D(gInteds[0], "D:\\project\\data", "GInteCDataSet.txt");
        } catch (Exception e)
        {
        	e.printStackTrace();
        }
		System.out.println("Global integration done, Check status!"); 
	}
    */
}

	/**
	 * 
	 * @param ods  two D dataset object for output
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
public Signal processNew(Signal in) {
	// TODO Auto-generated method stub
	return null;
}
	
	
}
