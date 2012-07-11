/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package au.gov.ansto.bragg.echidna.dra.algolib.core;

import javax.swing.JOptionPane;

import au.gov.ansto.bragg.common.dra.algolib.data.DataStore;
import au.gov.ansto.bragg.common.dra.algolib.math.OneDGaussianFunction;
import au.gov.ansto.bragg.common.dra.algolib.math.FPoint;
import au.gov.ansto.bragg.echidna.dra.algolib.entity.HRPDDataProvider;
import au.gov.ansto.bragg.echidna.dra.algolib.entity.HRPDDataSet;
import au.gov.ansto.bragg.echidna.dra.algolib.entity.HRPDDataStore;
import au.gov.ansto.bragg.echidna.dra.algolib.entity.HRPDDetector;
import au.gov.ansto.bragg.echidna.dra.algolib.processes.EchidnafileProcess;
import au.gov.ansto.bragg.echidna.dra.algolib.processes.GlobalIntegrationImpl;
import au.gov.ansto.bragg.echidna.dra.algolib.processes.HRPDCorrectionImpl;
import au.gov.ansto.bragg.echidna.dra.algolib.processes.HRPDDataSetStitchImpl;
import au.gov.ansto.bragg.echidna.dra.algolib.processes.HRPDGeometryCorrectImpl;
import au.gov.ansto.bragg.echidna.dra.algolib.processes.HorizontalIntegrationImpl;
import au.gov.ansto.bragg.echidna.dra.algolib.processes.VerticalIntegrationImpl;

/**
 * @author jgw
 *
 */
public class NumericFunctionTest {

		/**
		 * @param args
		 * @throws Exception 
		 */
		public static void main(String[] args) throws Exception {
			// TODO Auto-generated method stub
			final HRPDDataSet[] hdata = null;
			HRPDDetector hdetect= new HRPDDetector();
			int nTubes   = hdetect.xpixels;
			int nycount = hdetect.ypixels;
			double detecArch =hdetect.horisonCurv;
			double mu1 = 25.0;
			double mu2 = 40.0;
			double mu3 = 60.0;
			double mu4 = 85.0;
			double mu5 = 105.0;			
			double sigma1 = 0.8;
			double sigma2 = 1.6;
			double sigma3 = 1.5;	
			double sigma4 = 1.0;
			double sigma5 = 1.3;				
//			double [] thetaVect = new double[nTubes];
	        HRPDReductionTestImpl test =new HRPDReductionTestImpl();
//			HRPDDataProvider pdata =new  HRPDDataProvider();
//			HRPDCorrectionImpl hct = new HRPDCorrectionImpl();
//			HRPDGeometryCorrectImpl gcorrect = new HRPDGeometryCorrectImpl();
			VerticalIntegrationImpl vig  = new VerticalIntegrationImpl();
			HRPDDataSetStitchImpl  dstch = new HRPDDataSetStitchImpl();
			HorizontalIntegrationImpl hig  = new HorizontalIntegrationImpl();

			 test.setNScan(125);
			 int nScan = test.getNScan();
//			 double twoTheta0=0;
//			 double dTheta =hdetect.seperation;
//			boolean corraction = false;
			DataStore store = new  DataStore();
//			double [][][] genDataSet = new double [nScan][nTubes][nycount];
			double [][][] sds   = new double[nScan] [nycount][nTubes];
			double [][][] bgDat  = new double[nScan][nycount][nTubes];
			double [][] itds;
//			double [][] sendt = new double [nycount][nTubes];
			double [] calsum = new double [nTubes];		
			double[] stepsize =new double[nScan];
			double[][] twotheta = new double[nScan][nTubes];
			double twotheta0 = 0;
			double tspacer = 1.25;
			double binsize = 0;
			double[] thetaVect = new double[nScan*nTubes];
	/**
	 * Generete two D data sets with double Guassian distribution
	 * 
	 */
			System.out.println("Start to generate two D MC HRPD data sets");
			
//			sendt = pdata.genEffDataSet();
			
//			genDataSet = pdata.genGaussianDataSet();			
			//int nDataSet = genDataSet.length;
			//int nDetector = genDataSet[0].length;
			//int nDetcount = genDataSet[0][0].length;
			for (int i = 0; i < nTubes; i++) {
				calsum[i] =0;
			}
			for(int i = 0; i < nScan; i++) 
			{
				stepsize[i] = 0.05;
				for (int j= 0; j < nycount; j++)
				{
					for (int k= 0; k < nTubes; k++)
					{
				//		sds[k][j] = genDataSet[iDataSet][k][j];
					int	signal = (int) ((45)*(Math.random()+0.5)*(
							       OneDGaussianFunction.OneDGaussian(k, mu1, sigma1)
								+ OneDGaussianFunction.OneDGaussian(k, mu2, sigma2)
								+ OneDGaussianFunction.OneDGaussian(k, mu3, sigma3)
								+ OneDGaussianFunction.OneDGaussian(k, mu4, sigma4)
								+ OneDGaussianFunction.OneDGaussian(k, mu5, sigma5)  	));
				   int  noice	= 	(int) ((5.0)*(Math.random()+0.5)*(2.0 + Math.sin((double)k)));
				             sds[i][j][k] = signal + noice;
				             bgDat[i][j][k] = noice;
						
//						calsum[j] +=  sds[k][j];
		//				System.out.println("Data Sample:" + sds[k][j]);
					}
				}
//		        try
//		        {
//		        store.DataAsciiOutput2D(sds[i], "d:\\dragui\\simdat\\", "HRPDDataSet" + i + ".dat");
//		        store.DataAsciiOutput2D(bgDat[i], "D:\\dragui\\simdat\\", "HRPDbgSet" + i + ".dat");
//		        } catch (Exception e)
//		        {
//		        	e.printStackTrace();
//		        }
			}
			
			for(int n = 0; n < nScan; n++)
				for(int m = 0; m < nTubes; m++)
				{
					twotheta[n][m] = stepsize[n]*n + m*tspacer;

				}
	    double lengthCurve =  hdetect.horisonCurv +(nScan-1) * stepsize[1];
//	    int nBins = (int) (lengthCurve / stepsize[1] + 0.5);
	    int nBins = (int) (lengthCurve / stepsize[1]);
            thetaVect = new double[nBins];
			for (int k = 0; k < nBins; k++)
				thetaVect[k] = twotheta0 + k * stepsize[1];

				System.out.println("MC data generated!");
				System.out.println("Making background correction.");
				double[][][] signal_nobg = new double[125][128][128];
				for(int i = 0; i < nScan; i++) 
				{
	
					for (int j= 0; j < nycount; j++)
					{
						for (int k= 0; k < nTubes; k++)
						{
							signal_nobg[i][j][k] = sds[i][j][k] - bgDat[i][j][k];
						}
					}
				}
				
				itds =  new double [nycount +1][nBins];
				double[][] itds2 =  new double [nycount +1][nBins];			
             itds = dstch.echidnaDataStitch(signal_nobg, twotheta, stepsize, binsize, twotheta0);
             itds2 = dstch.echidnaDataStitch(sds, twotheta, stepsize, binsize, twotheta0);           
//     		for(int j=0;j<nycount;j++)
//     		System.out.println("Return Data: " + itds[j][100]);
	    	System.out.println("Finished data stitching, Check status!");   
	    	String nant = String.valueOf(Double.NaN);
	    	System.out.println(" The string of Double.NaN =" +  nant);
	    	if (nant.equals( "NaN"))
	    		System.out.println("String.NaN is  equal to NaN!"); 
	    	else System.out.println("String.NaN is  NOT equal to NaN!"); 
	    	
	    	double[][] reform  =  new double [nycount ][nBins];		
	    		for (int m = 0; m < nycount; m++ ) {
	    			for (int n = 0; n < nBins; n++ ){
	    				if(m < 5 || n < 55) reform[m][n] = Double.NaN;
	    				   else reform[m][n] = itds[m][n];
	    			}
	    		}
				int minBox  = 0;
				int maxBox  =128;
				int numBins = nBins;
				int xPix = itds[0].length;
				int yPix = itds.length;
				int nmod = yPix%2;

				System.out.println("Nmod = " + nmod);
				double xOrigin = 0.0;
				double yOrigin = 0.0;
				double pos = hdetect.radialCurv;
				
				double [][] hInteds = new double[3][nBins];	
				double [][] hInteds2 = new double[3][nBins];					
//				double [] hint1d  = new double[nBins];	
				hInteds = hig.findHorizontalIntegration(reform, nBins, minBox, maxBox,
						xOrigin, yOrigin, pos, thetaVect);
//			            hint1d =hInteds[0];
				hInteds2 = hig.findHorizontalIntegration(itds2, nBins, minBox, maxBox,
								xOrigin, yOrigin, pos, thetaVect);		            
				double[][] datfil1 = new double[nycount][nTubes];
				double[][] datfil2 = new double[nycount][nTubes];
				double[][] datres = new double[nycount][nTubes];
				double[][] datmulti = new double[nycount][nTubes];
				for (int j= 0; j < nycount; j++)
				{
					for (int k= 0; k < nTubes; k++)
					{
	
						datfil1[j][k]  = 5.00;
						datfil2[j][k]  = 2.00;
					}
				}
				EchidnafileProcess efp = new EchidnafileProcess();
				datres = efp.echidnafileAddition(datfil1, datfil2, null, null);
				datmulti = efp.echidnafileMultiplication(datfil1, datfil2, null, null);
				
				try
		        {
		        	store.DataAsciiOutput2D(datfil1, "D:\\project\\data\\", "DataSet1.txt");	
		        	store.DataAsciiOutput2D(datfil2, "D:\\project\\data\\", "DataSet2.txt");	        	
		        	store.DataAsciiOutput2D(datres, "D:\\project\\data\\", "Dataradd.txt");
		        	store.DataAsciiOutput2D(datmulti, "D:\\project\\data\\", "Datamulti.txt");		        	
//		        	store.DataAsciiOutput2D(reform, "D:\\dragui\\simdat\\", "stitchDataSet.txt");	
//		        	store.DataAsciiOutput2D(itds2, "D:\\dragui\\simdat\\", "stitchDataSet_bg.txt");	        	
//		        	store.dataTxtOutput2DCollumn(hInteds, "D:\\dragui\\simdat\\", "HInteDataErr.txt");
//		        	store.dataTxtOutput2DCollumn(hInteds2, "D:\\dragui\\simdat\\", "HInteDataErr_bg.txt");		        	
		        } catch (Exception e)
		        {
		        	e.printStackTrace();
		        }
				System.out.println("Horizontal integration done, Check status!"); 	
				
				
//				int minX =0;
//				int maxX =nBins;
//				double [][] vInteds = new double[3][nBins];	
//	
////				vInteds = vig.findVerticalIntegration(itds, nBins, minX, maxX,
////						xOrigin, yOrigin, pos, thetaVect);
//				vInteds =  DRAStaticLibHRPD.verticalIntegration(itds, nBins, minX, maxX,
//						xOrigin, yOrigin, pos, thetaVect);
//			            hint1d =hInteds[0];
//		        try
//		        {
//		        //store.HRPDDataOutput(cgds, "/home/jgw/opaldra/xml/", "HInteCDataSet.dat");
//	//	        	store.HRPDDataAsciiOutput1D(calsum, "D:\\dragui\\data\\", "CalINTDataSet.txt");
//
//		        	store.HRPDDataAsciiOutput2D(vInteds, "D:\\dragui\\data\\", "vInteDataErr.txt");
//		        } catch (Exception e)
//		        {
//		        	e.printStackTrace();
//		        }
//				System.out.println("Verticall integration done, Check status!"); 	
//
//	
//		
//
//		System.out.println("Start box region integration...");
//		FPoint stp =  new  FPoint(400.0,412.0);
//		FPoint edp = new  FPoint(nBins-800,100.);		
//
//		double [][] bInteds = new double[3][nBins];	
//
////		vInteds = vig.findVerticalIntegration(itds, nBins, minX, maxX,
////				xOrigin, yOrigin, pos, thetaVect);
//		     bInteds =  DRAStaticLibHRPD.squareIntegration(itds, nBins, stp, edp, xOrigin, yOrigin, pos, thetaVect);
//	
//        try
//        {
//        //store.HRPDDataOutput(cgds, "/home/jgw/opaldra/xml/", "HInteCDataSet.dat");
////	        	store.HRPDDataAsciiOutput1D(calsum, "D:\\dragui\\data\\", "CalINTDataSet.txt");
//
//        	store.HRPDDataAsciiOutput2D(bInteds, "D:\\dragui\\data\\", "bInteDataErr.txt");
//        } catch (Exception e)
//        {
//        	e.printStackTrace();
//        }
//		System.out.println("Box region integration done, Check status!"); 	

     }
}

