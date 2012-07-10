/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
*/
package au.gov.ansto.bragg.common.dra.algolib.calibration;

import java.util.ArrayList;

/**
 * @author jgw
 * Tel: +61 2 9717 7062  Fax: +61 2 9717 9799
 * Data Analysis Team, Bragg Institute,Bld.82
 * ANSTO PMB 1 Menai NSW 2234 AUSTRALIA

 */
public class UirReciProcalGeneration {
	
	
	/**
	 * 
	 * @param expDat            The two D array data set  to be processed for UIR.  the matrics should be 
	 *                                            expDat [yCounts] [xDetectors]. as Wombat is 928(2theta) * 128(yCounts)
	 * @param pixOKMap       input prepared pixel  OK Map which describe detector cell count information
	 * @param shhold             the sheshold for each detector pixel.
	 * @param calact               special calibration parameter
	 * @param norm                normalisation parameter for calibration process
	 * @return
	 */
	public double[][] createUIRReciProcalMap(double[][] expDat, int[][] pixOKMap, double shhold, 
			                           boolean calact, double norm) {

		int numpixels =0;
		int xPixels = expDat[0].length;
		int yPixels = expDat.length;
		double[][] reciprocal = new double[yPixels][xPixels];	
		
		for (int j = 0; j < yPixels; j++) {
			for ( int k = 0; k < xPixels; k++) {
				if (pixOKMap[j][k]== 1 && expDat[j][k] > shhold) {
					reciprocal[j][k] = expDat[j][k] / countAverageProcess(expDat, pixOKMap, shhold);
					
				}
				
			}
		}
		
		return reciprocal;
		
		
	}
	/**
	 * This method is designed to make average for input data array. It can be used for raw data average process or any 
	 * data set for such purpose.
	 * @param expDat      input raw data array to be processed
	 * @param pixOKMap    detector property map that provides the situation of each detector pixel.
	 * @param shhold         the shreshold for data analysis
	 * @return  a double average value with no weight process 
	 */
	private static  double countAverageProcess(double[][] expDat, int[][] pixOKMap, double shhold){
		
		double equalWeightAverage = 0.0;
		int numpixels =0;
		int xPixels = expDat[0].length;
		int yPixels = expDat.length;
		
		for (int j = 0; j < yPixels; j++) {
			for ( int k = 0; k < xPixels; k++) {
				if (pixOKMap[j][k]== 1 && expDat[j][k] > shhold) {
					numpixels += 1;
					equalWeightAverage += expDat[j][k];
				}
			}
		}
		
		return equalWeightAverage/numpixels;
	}
	/**
	 * 
	 * @param expDat      input raw data array to be processed
	 * @param pixOKMap    detector property map that provides the situation of each detector pixel.
	 * @param shhold            the shreshold for data analysis
	 * @param countavarage  the average values that are depend upon data array expDat
	 * @return  an integer  array which contain event information for designed number of bins
	 */
	private static int[] eventArrayCreation(double[][] expDat, int[][] pixOKMap, double shhold, double countaverage) {
		     int yPixels = expDat.length;
		     int xPixels = expDat[0].length;  
		     double maxCellValue = Double.MIN_VALUE;
		     double minCellValue = Double.MAX_VALUE;
		     double[] diffArray = new double[yPixels*xPixels];

		     ArrayList<Double> differArray = new ArrayList<Double>();
				for (int j = 0; j < yPixels; j++) {
					for ( int k = 0; k < xPixels; k++) {
						if(pixOKMap[j][k] == 1 && expDat [j][k] > shhold)
		                differArray.add(expDat[j][k] - countaverage);
					}
				}
				
				int numElem = differArray.size();
				for (int m = 0; m < numElem; m++) {
					if (differArray.get(m) > maxCellValue)  maxCellValue =differArray.get(m);
					if (differArray.get(m) < minCellValue)   minCellValue =differArray.get(m);	
				}
				int binSiz = 200;    //normally with 6600 binsin full range for echina 
          int numBins = (int) ( maxCellValue - minCellValue ) / binSiz;
		     int[] numEvent = new int[numBins];          
		for (int j = 0; j < yPixels; j++) {
			for ( int k = 0; k < xPixels; k++) {
		     for (int n = 0; n < numBins; n++) {
        	   if ( expDat[j][k] >= n*binSiz && expDat[j][k] < (n + 1) * binSiz )
        		   numEvent[n] +=1;
          }
			}
		}
          
		return numEvent;
	}
	/**
	 * 
	 * @param evtArray  a double array which contain event information for designed number of bins
	 * @param mean   the raw mean value for fitting peak
	 * @param sigma       the raw sigma value for fitting peak
	 * @param shhold       the shreshold for data analysis to decide each pixel  with/without  process.
	 * @return   a verctor that contain fitted information such as mean, sigma, peak value...
	 */
	private static double[][] eventFittingprocess(double[] evtArray, double mean, double sigma, double shhold){
		    
//		OneDnumericFit  odf = new OneDnumericFit();
//		BackgroundFunction bgf = new BackgroundFunction(0,1);
//		double[][] resFit = odf.oneDDataFitting(evtArray, "Gaussian", 1, null, bgf);
//		return resFit;
		return null;
		
	}
	/**
	 * 
	 * @param expDat      input raw data array to be processed
     * @param evtArray  a double array which contain event information for designed number of bins
	 * @param mean   the raw mean value for fitting peak
	 * @param sigma       the raw sigma value for fitting peak
	 * @param shhold       the shreshold for data analysis to decide each pixel  with/without  process.
	 */
	private static double[][] staticticalAnalyser(double[][] expDat,  double mean, double sigma, double shhold) {
		boolean legal = true;
		DetectorPixelOKProcess dpok = new DetectorPixelOKProcess();
		int[][] pixOKMap  = dpok.powderDetectorPixelOKmap(expDat, legal, shhold);
		double countaverage = countAverageProcess(expDat,pixOKMap,shhold);
		int[] EvtArray = eventArrayCreation(expDat, pixOKMap,  shhold,  countaverage);
		int nElem = EvtArray.length;
// convert integer array to double array
		double[]  evtArray  = new double[nElem];
		for (int n = 0; n < nElem; n++){
			evtArray[n] = EvtArray[n];
		}
		
		double[][]  fittingRes = eventFittingprocess(evtArray, mean,  sigma, shhold);
		       mean  = fittingRes[1][1];
		       sigma = fittingRes[1][2];
		       ArrayList<Double>  diffElem = differenceElement(expDat, pixOKMap,  shhold,  countaverage);
	     for (int m = 0; m < diffElem.size(); m++){
		       if (diffElem.get(m) < 3.0 * sigma) diffElem.remove(m);
	     }
	     
	     Double[] diffElem2 = (Double[]) diffElem.toArray();
	     double[] diffElem3 = new double[diffElem2.length];
	     for (int l = 0; l < diffElem2.length; l++) 
	    	 diffElem3[l]  = diffElem2[l];
	     
			double[][] resFit2 = eventFittingprocess(diffElem3, mean,  sigma, shhold);
			return  resFit2;
	}
	
	private static ArrayList<Double>  differenceElement(double[][] expDat, int[][] pixOKMap, double shhold, double countaverage) {

		int yPixels = expDat.length;
	     int xPixels = expDat[0].length;  
		ArrayList<Double> differArray = new ArrayList<Double>();
			for (int j = 0; j < yPixels; j++) {
				for ( int k = 0; k < xPixels; k++) {
					if(pixOKMap[j][k] == 1 && expDat [j][k] > shhold)
	                differArray.add(expDat[j][k] - countaverage);
				}
			}
			
			return differArray;
	}
	
	private static double calSigma4Data (double shhold, double countaverage) {
		
		double sigmav = Math.sqrt(countaverage);
		
		return sigmav;
		
	}
}
