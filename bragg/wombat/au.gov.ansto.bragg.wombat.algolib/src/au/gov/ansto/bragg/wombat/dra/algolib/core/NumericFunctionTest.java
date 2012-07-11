/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package au.gov.ansto.bragg.wombat.dra.algolib.core;

import javax.swing.JOptionPane;

import au.gov.ansto.bragg.common.dra.algolib.math.FPoint;
import au.gov.ansto.bragg.common.dra.algolib.math.OneDGaussianFunction;

import au.gov.ansto.bragg.wombat.dra.algolib.core.DRAStaticLibHIPD;
import au.gov.ansto.bragg.wombat.dra.algolib.entity.HIPDDataProvider;
import au.gov.ansto.bragg.wombat.dra.algolib.entity.HIPDDataSet;
import au.gov.ansto.bragg.wombat.dra.algolib.entity.HIPDDataStore;
import au.gov.ansto.bragg.wombat.dra.algolib.entity.HIPDDetector;
import au.gov.ansto.bragg.wombat.dra.algolib.processes.*;


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
			final HIPDDataSet[] hdata = null;
			HIPDDetector hdetect= new HIPDDetector();
			int nTubes   = hdetect.xPixels;
			int nycount = hdetect.yPixels;
			double detecArch =hdetect.horisonCurv;
			double mu1 = 38.0F;
			double mu2 = 75.0F;
			double mu3 = 98.0F;		
			double sigma1 = 5.0F;
			double sigma2 = 4.0F;
			double sigma3 = 3.0F;		
//			double [] thetaVect = new double[nTubes];
	        HIPDReductionTestImpl test =new HIPDReductionTestImpl();
//			HRPDDataProvider pdata =new  HRPDDataProvider();
//			HRPDCorrectionImpl hct = new HRPDCorrectionImpl();
//			HRPDGeometryCorrectImpl gcorrect = new HRPDGeometryCorrectImpl();
//			VerticalIntegrationImpl vig  = new VerticalIntegrationImpl();
	        HIPDDataFileSummeryImpl   dstch = new HIPDDataFileSummeryImpl ();
			HorizontalIntegrationImpl hig  = new HorizontalIntegrationImpl();

			 test.setNScan(1);
			 int nScan = test.getNScan();
//			 double twoTheta0=0;
//			 double dTheta =hdetect.seperation;
//			boolean corraction = false;
			HIPDDataStore store = new  HIPDDataStore();
//			double [][][] genDataSet = new double [nScan][nTubes][nycount];
			double [][] sds   = new double [nycount][nTubes];
//			double [][] geods = new double [nycount][nTubes];
			double [][] itds= new double [nycount][nScan*nTubes];
//			double [][] sendt = new double [nycount][nTubes];
			double [] calsum = new double [nTubes];		
			double stepsize;
			double[] twotheta = new double[nTubes];
			double twotheta0 = 0;
			double tspacer = hdetect.seperation;
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
//			for(int i = 0; i < nScan; i++) 
//			{
//				stepsize = 0.05;
				for (int j= 0; j < nycount; j++)
				{
					for (int k= 0; k < nTubes; k++)
					{
				//		sds[k][j] = genDataSet[iDataSet][k][j];
						sds[j][k] = (double) ((double)(15)*(double)(Math.random()+0.5)*(1.0 
								+ OneDGaussianFunction.OneDGaussian(j, mu1, sigma1)
								+ OneDGaussianFunction.OneDGaussian(j, mu2, sigma2)
								+ OneDGaussianFunction.OneDGaussian(j, mu3, sigma3)));
//						calsum[j] +=  sds[k][j];
		//				System.out.println("Data Sample:" + sds[k][j]);
					}
				}
//			}
			

				for(int m = 0; m < nTubes; m++)
				{
					twotheta[m] =  m*tspacer;

				}
				stepsize = tspacer;
	    double lengthCurve =  hdetect.horisonCurv ;
        int nBins = nTubes;
            thetaVect = new double[nBins];
			for (int k = 0; k < nBins; k++)
				thetaVect[k] = twotheta0 + k * stepsize;
			double dtwotheta =thetaVect[1] - thetaVect[0];
				System.out.println("MC data generated!");
//             itds = dstch.multiDataSetStich (sds, twotheta0,  dtwotheta);
//     		for(int j=0;j<nycount;j++)
//     		System.out.println("Return Data: " + itds[j][100]);
	    	System.out.println("Finished data stitching, Check status!");      

				
				int minBox  = 0;
				int maxBox  =nBins;
				int numBins = nBins;



				double xOrigin = 0.0F;
				double yOrigin = 0.0F;
				double pos = hdetect.radialCurv;
				
				double [][] hInteds = new double[3][nBins];	
				double [] hint1d  = new double[nBins];	
				hInteds = hig.findHorizontalIntegration(sds, nBins, minBox, maxBox,
						xOrigin, yOrigin, pos, thetaVect);
			            hint1d =hInteds[0];
		        try
		        {
		        //store.HRPDDataOutput(cgds, "/home/jgw/opaldra/xml/", "HInteCDataSet.dat");
	//	        	store.HRPDDataAsciiOutput1D(calsum, "D:\\dragui\\data\\", "CalINTDataSet.txt");
	//	        	store.HIPDDataAsciiOutput2D(itds, "D:\\dragui\\data\\", "stitchDataSet.txt");	        	
	//	        	store.HIPDDataAsciiOutput1D(hint1d, "D:\\dragui\\data\\", "HInteCDataSet.txt");
		        	store.HIPDDataAsciiOutput2D(hInteds, "D:\\dragui\\data\\", "HInteDataErr.txt");
		        } catch (Exception e)
		        {
		        	e.printStackTrace();
		        }
				System.out.println("Horizontal integration done, Check status!"); 			

				int minX =0;
				int maxX =nBins;
				double [][] vInteds = new double[3][nBins];	
	
//				vInteds = vig.findVerticalIntegration(itds, nBins, minX, maxX,
//						xOrigin, yOrigin, pos, thetaVect);
				vInteds =  DRAStaticLibHIPD.verticalIntegration(sds, nBins, minX, maxX,
						xOrigin, yOrigin, pos, thetaVect);
			            hint1d =hInteds[0];
		        try
		        {
		        //store.HRPDDataOutput(cgds, "/home/jgw/opaldra/xml/", "HInteCDataSet.dat");
	//	        	store.HRPDDataAsciiOutput1D(calsum, "D:\\dragui\\data\\", "CalINTDataSet.txt");

		        	store.HIPDDataAsciiOutput2D(vInteds, "D:\\dragui\\data\\", "vInteDataErr.txt");
		        } catch (Exception e)
		        {
		        	e.printStackTrace();
		        }
				System.out.println("Verticall integration done, Check status!"); 	

	
		

		System.out.println("Start box region integration...");
		FPoint stp =  new  FPoint(400.0,412.0);
		FPoint edp = new  FPoint(nBins-800,100.);		

		double [][] bInteds = new double[3][nBins];	

//		vInteds = vig.findVerticalIntegration(itds, nBins, minX, maxX,
//				xOrigin, yOrigin, pos, thetaVect);
		     bInteds =  DRAStaticLibHIPD.squareIntegration(sds, nBins, stp, edp, xOrigin, yOrigin, pos, thetaVect);
	
        try
        {
        //store.HRPDDataOutput(cgds, "/home/jgw/opaldra/xml/", "HInteCDataSet.dat");
//	        	store.HRPDDataAsciiOutput1D(calsum, "D:\\dragui\\data\\", "CalINTDataSet.txt");

        	store.HIPDDataAsciiOutput2D(bInteds, "D:\\dragui\\data\\", "bInteDataErr.txt");
        } catch (Exception e)
        {
        	e.printStackTrace();
        }
		System.out.println("Box region integration done, Check status!"); 	

	}

	}


