package au.gov.ansto.bragg.echidna.dra.algolib.core;
/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */
import java.util.ArrayList;

import au.gov.ansto.bragg.common.dra.algolib.data.DataStore;
import au.gov.ansto.bragg.common.dra.algolib.math.*;
import au.gov.ansto.bragg.common.dra.algolib.processes.GenericMaskProcessImpl;

import com.thoughtworks.xstream.converters.reflection.ObjectAccessException;

import au.gov.ansto.bragg.echidna.dra.algolib.entity.HRPDDetector;
import au.gov.ansto.bragg.echidna.dra.algolib.processes.*;
/**
 * Public API for access Echidna  Algorithm library 
 * @author jgw
 *
 */
public class DRAStaticLibHRPD {
	/**
	 * DATA SET ARRAY INDEX DIFFINITION
	 * SINGLE AND MULTIPLE DIMENSIONAL ARRAY
	 * =============================================
	 * Input
	 * 2D array:   inDat[yCount][xTubes]
	 * 3D array:   inDat[nScan][yCount][xTubes]
	 * =============================================
	 *  Output
	 *  1D array:  outDat[nSlices]
	 *  2Darray:   outDat[yCount][xTubes]
	 *  3D arry:  outDat[nScan][yCount][xTubes]
	 *  =============================================
	 *  Vectors
	 *  Pseudo  2D array output and input
	 *  1D data array plus 1D error array  pDat[2][nSlices]
	 *  								PDat[0]  1D data array,
	 *  								PDat[1]  1D error array.
	 *  Pseudo  3D array output and input
	 *  2D data array plus 2D error array  pDat[2][yCount][xTubes]
	 *  								PDat[0]  2D data array, pDat[0][yCount][xTubes]
	 *  								PDat[1]  2D error array.  pDat[1][yCount][xTubes]
	 *  After multiple set data stitching,
	 *   2D data array plus 2D error array  pDat[2][yCount][xTubes*nScan]
	 *  								PDat[0]  2D data array,   pDat[0][yCount][xTubes]
	 *  								PDat[1]  2D error array.  pDat[1][yCount][xTubes]
	 */
	

	/**
	 *  Constructor for Echidna data normalization. The design is actually quite general. This algorithm can be applied to
	 *   any other data normalization. Be ware the first dimension  length of multiDats should be same as the length of 
	 *   scanNorm 
	 * @param multiDats  three D data set to be normalized. This is three D array of multiple  scan with data 
	 *              format  (nScans * nCount * nTubes)
	 * @param scanNorm Normalization data array. For echidna instrument, this  data array should be read from 
	 *   monitor -- data entry in NeXus  (hdf) file.
	 * @return normDat   Normalized 3D data set with format   (nScans * nCount * nTubes)
	 */
	public static double[][][] EchidnaMultiDataNormalization(double[][][] multiDats, double[] scanNorm) {
		
		EchidnaDataNormalization edn = new EchidnaDataNormalization(multiDats,  scanNorm);

		return edn.getNormDat();

	}
	
/**
 * 
 * @param inData    Raw exp data float 2D set for Input  with format  (nDetCount * nTubes) 
 * @param effMap    Presetting 2D efficiency table from instrument scientist or anywhere else
 * @param flag        control para for doing correction (flag=1) or not (flag=0)
 * @param flag contrl parameter flag =1 do correction; flag=0 do nothing
 * @param Control threshold parameter to reject very small efficiency block
 * @param inverse   boolean control parameter 
 *                                if detection efficience less than 100%  set it false
 *                                if detection efficience greater than 100%  set it true
 * @return The filtered array.
 * @return  foo        corrected  2D float data set
 */
	
	public static double[][] efficiencyCorrector(double[][] inData, double[][] effMap, int flag, double thresh, boolean inverse ){
		
		HRPDCorrectionImpl corr = new HRPDCorrectionImpl();
		
		return corr.doSensitivity(inData, effMap,flag, thresh, inverse);
	}
	
    /**
     * We do NOT make background correction at this moment for HRPD. This element of  liborary
     * set for future application in case any other BG correction inquiry 
     * Eliminates the background data. Data is normalised by the monitor counts
     * if possible. 
     * @param iSample The sample data  with format  (nDetCount * nTubes) .
     * @param monSample The monitor counts for the sample scan.
     * @param iEmpty The empty cell data.
     * @param monEmpty The monitor counts for the empty cell scan.
     * @param iBlocked The background data.
     * @param monBlocked The monitor counts for the background scan.
     * @param sensitivity The detector sensitivity.
     * @param tSample The sample transmission.
     * @param tEmpty The empty cell transmission.
     * @param removeNegatives Whether to remove negative values from the
     * data.
     * @param background The flat background reading.
     * @return The corrected data.
     * @throws Exception If the background and data are not the same size
     */
    public  static  double[][] BGcorrect(double[][] iSample, double monSample, double[][] iEmpty,
            double monEmpty, double[][] iBlocked,
            double monBlocked, double[][] sensitivity, double tSample,
            double tEmpty, boolean removeNegatives, double background) throws Exception{
	
    	return  iSample;
    
    }
	   /**
     * Applies experiment background subtraction to the data. This method is simplified for one background source only.
     * @param data The data array to be filtered.
     * @param bgData The detector background data sets.
     * @return The filtered array.
     */
    public   static double[][] removeBackground(double[][] data, double[][] bgData, double ratio ) {
 
    	HRPDCorrectionImpl rbs = new HRPDCorrectionImpl();
    	return rbs.removeBackground(data, bgData, ratio);
    }
   /**
     * Applies experiment background subtraction to the data. This method is designed  for 
     * multiple BG sources. Be careful about  "ArrayList".
     * @param data Two D data array to be filtered.
   * @param bgData The detector background ArrayList data sets. Since BG can contribute from different  sources, 
     *                                  we therefore design BG data as a array list to process different bg sources.
     * @param ratio       propotion of  Arraylist BGs against time or other facts.
     * @return The filtered data array.
     */
    public   static double[][] removeBackground(double[][] data, ArrayList<double[][]> bgData,  ArrayList<Double> ratio ) {
	
    	HRPDCorrectionImpl rbs = new HRPDCorrectionImpl();
    	return  rbs.removeBackground(data, bgData, ratio);
    
    }
    /**
     * Applies experiment background subtraction to the data.
     * @param data The twoD  array data to be filtered. with format data[yCounts][xTubes]
     * @param bgData  The detector background data sets from electronics or other equipment.
     * @param ration1 The ratio background proportional to time following bgData.
     * @param elecBD  The detector background data sets from known background source.
     * @param ration The ratio2 background proportional to time following elecBG.
     * @param effDat   the Detector efficiency table fro BG correction
     * 
     * @return The corrected twoD  arraydata set  with format data[yCounts][xTubes]
     */
    public  static  double[][] removeBackground(double[][] data, double[][] bgData,  double ratio1,  
    		      double[][] elecBD, double ratio2, double[][] effDat, boolean remNag ){
    	HRPDCorrectionImpl hbs = new HRPDCorrectionImpl();
	        
	        return hbs.removeBackground(data, bgData, ratio1, elecBD, ratio2, effDat, remNag);
    }



	/**
	 * Converts a 3D multiscan data sets into a flat 2d based on the instrument metadata.
	 * This  algorithm is developed for NO overlap multiple scan data set stitching
	 * @param 3D exp data set  to be stitchedwith format  nDataSet * nDetCount * nTubes. 
	 * 					DataSet[nDataSet ][nDetCount ][ nTubes]
	 * @param  twoTheta0   the initial position of first tube
	 * @param  deltaTheta   distance between two tubes
	 * @return 2D array data double object with nCounts* nScan elements
	 *                  Special return array retData[nCounts] (normally retData[512]) present new theta vector.
	 */

	public static double[][] hrpdDataStitch(double[][][] stds, double  twoTheta0,  double deltaTheta, double[] stepsize){
		HRPDDataSetStitchImpl stitch = new HRPDDataSetStitchImpl();
		
		return stitch.multiDataSetStich(stds, twoTheta0, deltaTheta,  stepsize);
		
	}
	 /**
	  * This  algorithm is developed for overlap multiple scan data set stitching
	  *	Converts a 3D multiscan data sets into a flat 2d based on the instrument metadata. 
	  * @param inDat      Input 3D file with nScan * nCounts * nTubes
	  * @param array2theta  Multiple scan 2D 2theta array nScan * nTubes
	  * @param stepsize  Multiple scan step size 1D array with nScan elements
	  * @param binsize   Optional user control parameter for number of bins stitching to.
	  * @param twotheta0 The two theta for first tube in the first scan.
	  * @return 2D array data double object with nCounts*((detctorArc + nScan*stepsize)/stepsize) elements
	  *  !!!!           Special return array retData[nCounts] (normally retData[512]) present new theta vector.
	  */
	public static double [][] echidnaDataStitchBak( double[][][] inDat,
				 							    double[] stepsize, double binsize, double twotheta0){
		                HRPDDataSetStitchImpl stitch = new HRPDDataSetStitchImpl();	
		                HRPDDetector hdd = new HRPDDetector();
		                double twoTheta0=0.0;
		        		double deltaTheta = 0.0;
		        		int nScan =inDat.length;
		        		int nTubes = inDat[0][0].length;
		        		double[][] thetaVect = new double[nScan][nTubes];
//		        		double[][][] inDat3D = new double[nScan][nCount][nTubes];
		        		double avstepsiz =0.05;
		        		double bsize;
		        		
		        	    int   nsBin = (int)( (hdd.horisonCurv + nScan * avstepsiz)/ avstepsiz + 0.5);

		        			  twoTheta0=0.0;
		        			  deltaTheta = hdd.seperation;
		        			for (int i = 0; i < nScan; i++){
		        				for (int j = 0; j < nTubes; j++){
		        					thetaVect[i][j] =twoTheta0 + avstepsiz * i + deltaTheta*j;
		        				}
		        			}
	
		        		if (stepsize == null || (stepsize != null && stepsize[1]!=0.0)) {			
		        			stepsize = new double[nScan];	
		        		     for (int i = 0; i < nScan; i++) 
		        		    	 stepsize[i] = avstepsiz;
		        		}	                
		return stitch.echidnaDataStitch(inDat, thetaVect, stepsize, binsize, twotheta0);
	}
	 /**
	  * This  algorithm is developed for overlap multiple scan data set stitching
	  *	Converts a 3D multiscan data sets into a flat 2d based on the instrument metadata. 
	  * @param inDat      Input 3D file with nScan * nCounts * nTubes
	  * @param array2theta  Multiple scan 2D 2theta array nScan * nTubes
	  * @param stepsize  Multiple scan step size 1D array with nScan elements
	  * @param binsize   Optional user control parameter for number of bins stitching to.
	  * @param twotheta0 The two theta for first tube in the first scan.
	  * @return 2D array data double object with nCounts*((detctorArc + nScan*stepsize)/stepsize) elements
	  *  !!!!           Special return array retData[nCounts] (normally retData[512]) present new theta vector.
	  */	
	
	public static double [][] echidnaDataStitch( double[][][] inDat, double [][] thetaVect,
			    double[] stepsize, double binsize, double twotheta0){

		double twoTheta0;
	double deltaTheta ;
	int nScan = inDat.length;
	int nCount = inDat[0].length;
	int nTubes = inDat[0][0].length;
		HRPDDetector hdd = new HRPDDetector();
	double[][] array2theta = new double[nScan][nTubes];
//	double[][][] inDat3D = new double[nScan][nCount][nTubes];
	double avstepsiz =0.05;
//	double binsiz = 0.0;
	
//	for (int n = 0; n < nScan; n++)
//		for(int k = 0; k < nCount; k ++)
//			for (int j = 0; j < nTubes; j++)
//				inDat3D[n][k][j] = dat3DSets[n][k][j];
	
 //   int   nsBin = (int)( (hdd.horisonCurv + nScan * avstepsiz)/ avstepsiz + 0.5);
	if(thetaVect != null && (thetaVect[0][20] != 0 && thetaVect[0][40] !=0)) {
		array2theta = thetaVect;
		twoTheta0 = thetaVect[0][0];
		deltaTheta = thetaVect[0][1] -thetaVect[0][0];
		}
	else {
		  twoTheta0=0.0;
		  deltaTheta = hdd.seperation;
		for (int i = 0; i < nScan; i++){
			for (int j = 0; j < nTubes; j++){
				array2theta[i][j] =twoTheta0 + avstepsiz * i + deltaTheta*j;
			}
		}
		thetaVect = new double[nScan][nTubes];
		thetaVect = array2theta;
	}
	if (stepsize == null || (stepsize != null && stepsize[1] == 0.0)) {			
		stepsize = new double[nScan];	
	     for (int i = 0; i < nScan -1; i++) 
	    	 stepsize[i] = Math.abs(array2theta[i+1][0] - array2theta[i][0]);
	}


//	   double[][] stitchedDat = new double[nCount+1][nsBin];	
    	    
        HRPDDataSetStitchImpl stitch = new HRPDDataSetStitchImpl();	

 //      stitchedDat = stitch.echidnaDataStitch(inDat, array2theta, stepsize, binsize, twoTheta0);      

       return stitch.echidnaIdealDataStitch(inDat, array2theta, stepsize, binsize, twoTheta0); 
		
//		return stitch.echidnaDataStitch(inDat, thetaVect, stepsize, binsize, twotheta0); 

	}
	
	/**
	 * 
	 * @param iSample       input 2D array raw data set after stitching  yPixels* (nTubes*nScan) 
	 * @param geometry   Optional 2D geometry data table
	 * @param detectArch  Detector arch in degree
	 * @param thetaVect   OneD array theta vector which presents ditector tube position
	 * @param Geom         boolean control para. "true" for geo correction  and do nothing with "false:
	 * @param Zpvertic      the vertical distance to central point.
	 * @return  2D array  corrected  data set  rtndata[yPixeld][nTubes*nScan],  (nTubes*nScan)*yPixels 
	 */
	public static double[][] correctGeometry(double[][] iSample,  double[][] geometry,
			double detectArch, double[] thetaVect, boolean Geom, double[][] Zpvertic)
	{
		HRPDGeometryCorrectImpl geocor =new HRPDGeometryCorrectImpl();	
		return geocor.correctGeometry(iSample, geometry, detectArch, thetaVect,  Geom,  Zpvertic);
		
	}
	
	/**
     * Finds the horizontal integration about a mask.
     * @param data The two D double data with format  (nDetCount * nTubes)  to be integrated.
     * Points where data = 0 are considered to be masked.
     * @param err An array to put the error values into.
     * @param stdDev An array to put the standard deviation values into.
     * @param numSlices How many slices should be considered.
     * @param minDist The minimum value (down side) for integration.
     * @param maxDist The maximum value (up side) for integration.
      * @param xOrigin  The x coodinate of beam center,  currently set xOrigin= 0.0. If it is special, input your value!
     * @param yOrigin  The y coodinate of beam center,  currently set xOrigin= 0.0. If it is special, input your value!
     * @param pos The position from the ditector to sample, default value is obtained from meta data. 
     *                         If there is any special, you can input hear. Otherwise input 0.0.
     *@param   thetaVect  the detector tube 2 theta position (nScan * nTubes)                    
     * @return two vector arrays totals[2][numSlices]
     *                 totals[0][numSlices]   present number of neutron in each integrated bin
     *                 totals[1][numSlices]   present value of error in each integrated bin
     *                 totals[2][numSlices]   present value of thetaVect in each integrated bin
     */
	                  	
	public static double[][] horizontalIntegration(double[][] data,
            int numSlices, double minDist, double maxDist, double xOrigin,
            double yOrigin, double pos, double[] thetaVect){
		    HorizontalIntegrationImpl hint = new HorizontalIntegrationImpl();		
		    int nvPix = data.length;
		    int nhPix = data[0].length;
		    int nmod = nvPix%2;
//		    double[] thV = new double [nhPix];
			double[][] inputDat;
			if(nmod != 0){
//	    	    thV = data[nvPix];		
				inputDat = new double[nvPix-1][nhPix];	
				for (int j = 0; j < nvPix -1; j++) 
					for(int n = 0; n < nhPix; n++)
						inputDat[j][n] = data[j][n];
			} else {
//				thV = thetaVect;
				inputDat = new double[nvPix][nhPix];
				inputDat = data;
			}
			

		  return hint.findHorizontalIntegration(data, numSlices, minDist, maxDist, xOrigin, yOrigin, pos, thetaVect);
		
	}
	public static double[][] horizontalIntegration(double[][] data,
            int numSlices, double minDist, double maxDist, double xOrigin,
            double yOrigin, double pos){
		    HorizontalIntegrationImpl hint = new HorizontalIntegrationImpl();		
		    int nvPix = data.length;
		    int nhPix = data[0].length;
		    int nmod = nvPix%2;
//		    int nTube = 128;
		    double theta0 = 0.0;
		    double asize = 0.05;
		    double delTheta = 1.25;
		    double[][] inputDat;
		    double[] thV = new double [nhPix];
		    
			if(nmod !=0){
				inputDat = new double[nvPix-1][nhPix];	
				for (int j = 0; j < nvPix -1; j++) 
					for(int n = 0; n < nhPix; n++)
						inputDat[j][n] = data[j][n];
			} else {
				inputDat = new double[nvPix][nhPix];
				inputDat = data;
			}
// Calculate theta verctor

		    if(nmod != 0){
		    	thV = data[nvPix];
		         } else {
				for (int nth = 0; nth < nhPix; nth++){
					if(nvPix ==128) thV[nth]= theta0 + nth * delTheta;
					else thV[nth]= theta0 + nth * asize;
				     }
			} 

		  return hint.findHorizontalIntegration(data, numSlices, minDist, maxDist, xOrigin, yOrigin, pos, thV);
		
	}
	/**
     * Finds the horizontal integration about a mask.
     * @param data The two D double data array  with format  (nDetCount * nTubes)  to be integrated.
     * Points where data = 0 are considered  to be rejected (or "masked").
     * @param err An array to put the error values into.
     * @param stdDev An array to put the standard deviation values into.
     * @param numSlices How many slices should be considered.
     * @param minDist  The minimum y value (left side) for integration.
     * @param maxDist The maximum y value (right side) for integration.
     * @param xOrigin  The x coodinate of beam center,  currently set xOrigin= 0.0. If it is special, input your value!
     * @param yOrigin  The y coodinate of beam center,  currently set xOrigin= 0.0. If it is special, input your value!
     * @param pos The position from the ditector to sample, default value is obtained from meta data. 
     *                         If there is any special, you can input hear. Otherwise input 0.0.
     *@param   thetaVect  the detector tube 2 theta position (nScan * nTubes)           
      * @return two vector arrays totals[2][numSlices]
     *                 totals[0][numSlices]   present number of neutron in each integrated bin
     *                 totals[1][numSlices]   present value of error in each integrated bin
     *                 totals[2][numSlices]   present value of thetaVect in each integrated bin
     */
	                      
	public static double[][] verticalIntegration(double[][] data, 
            int numSlices, double minDist, double maxDist, double xOrigin,
            double yOrigin, double pos, double[] thetaVect){
		    VerticalIntegrationImpl vint = new VerticalIntegrationImpl();		
		    int nvPix = data.length;
		    int nhPix = data[0].length;
		    int nmod = nvPix%2;
//		    double[] thV = new double [nhPix];
			double[][] inputDat;
			if(nmod != 0){
//		    	thV = data[nCount];		
				inputDat = new double[nvPix-1][nhPix];	
				for (int j = 0; j < nvPix -1; j++) 
					for(int n = 0; n < nhPix; n++)
						inputDat[j][n] = data[j][n];
			} else {
//				thV = thetaVect;
				inputDat = new double[nvPix][nhPix];
				inputDat = data;
			}
	return vint.findVerticalIntegration(inputDat, numSlices, minDist, maxDist, xOrigin, yOrigin, pos, thetaVect);
		
	}
	/**
     * Finds the square integration about a mask.
     * @param data, The data to be integrated  with format  (nDetCount * nTubes) .
     * Points where data = 0 are considered to be rejected (or "masked").
     * @param err An array to put the error values into.
     * @param stdDev An array to put the standard deviation values into.
     * @param numSlices How many slices should be considered.
     * @param startpoint The atart point to consider with two d point (x1,y1).
     * @param endpoint  the end point to consider  with two d point (x2,y2).
     * @param xOrigin  The x coodinate of beam center,  currently set xOrigin= 0.0. If it is special, input your value!
     * @param yOrigin  The y coodinate of beam center,  currently set xOrigin= 0.0. If it is special, input your value!
     * @param pos The position from the ditector to sample, default value is obtained from meta data. 
     *                         If there is any special, you can input hear. Otherwise input 0.0.
     *@param   thetaVect  the detector tube 2 theta position (nScan * nTubes)                                    
     * @return two vector arrays totals[2][numSlices]
     *                 totals[0][numSlices]   present number of neutron in each integrated bin
     *                 totals[1][numSlices]   present value of error in each integrated bin
     *                 totals[2][numSlices]   present value of thetaVect in each integrated bin
     */
	public static double[][] squareIntegration(double[][] data,
			int numSlices,  FPoint startpoint, FPoint endpoint, double xOrigin,
			double yOrigin, double pos, double[] thetaVect){
		    SquareRegionIntegrationImpl sint = new SquareRegionIntegrationImpl();	
		    int nvPix = data.length;
		    int nhPix = data[0].length;
		    int nmod = nvPix%2;
//		    double[] thV = new double [nhPix];
			double[][] inputDat;
			if(nmod != 0){
//		    	thV = data[nvPix];		
//				thV = thetaVect;
				inputDat = new double[nvPix-1][nhPix];	
				for (int j = 0; j < nvPix -1; j++) 
					for(int n = 0; n < nhPix; n++)
						inputDat[j][n] = data[j][n];
			} else {
//				thV = thetaVect;
				inputDat = new double[nvPix ][nhPix];
				inputDat = data;
			}
		return sint.findSquareMask(inputDat, numSlices, startpoint, endpoint, thetaVect);

}
	/**
     * Finds the global integration about a mask.
     * @param data, The data to be integrated  with format  (nDetCount * nTubes) .
     * Points where data = 0 are considered to be rejected (or "masked").
     * @param err An array to put the error values into.
     * @param stdDev An array to put the standard deviation values into.
     * @param numSlices How many slices should be considered.
     * @param startpoint The atart point to consider with two d point (x1,y1).
     * @param endpoint  the end point to consider  with two d point (x2,y2).
     * @param xOrigin  The x coodinate of beam center,  currently set xOrigin= 0.0. If it is special, input your value!
     * @param yOrigin  The y coodinate of beam center,  currently set xOrigin= 0.0. If it is special, input your value!
     * @param pos The position from the ditector to sample, default value is obtained from meta data. 
     *                         If there is any special, you can input hear. Otherwise input 0.0.
     *@param   thetaVect  the detector tube 2 theta position (nScan * nTubes)                                   
     * @return two vector arrays totals[2][numSlices]
     *                 totals[0][numSlices]   present number of neutron in each integrated bin
     *                 totals[1][numSlices]   present value of error in each integrated bin
     *                 totals[2][numSlices]   present value of thetaVect in each integrated bin
     */
	public static double[][] globalIntegration(double[][] data, 
			int numSlices,  FPoint startpoint, FPoint endpoint, double xOrigin,
			double yOrigin, double pos, double[] thetaVect){
		    GlobalIntegrationImpl sint = new GlobalIntegrationImpl();		
		return sint.findGlobalIntegration(data, numSlices, thetaVect);

}	
	/**
     * Finds the global integration about a mask.
     * @param data, The data to be integrated  with format  (nDetCount * nTubes) .
     * Points where data = 0 are considered to be rejected (or "masked").
     * @param err An array to put the error values into.
     * @param stdDev An array to put the standard deviation values into.
     * @param numSlices How many slices should be considered.
     * @param startpoint The atart point to consider with two d point (x1,y1).
     * @param endpoint  the end point to consider  with two d point (x2,y2).
     * @param xOrigin  The x coodinate of beam center,  currently set xOrigin= 0.0. If it is special, input your value!
     * @param yOrigin  The y coodinate of beam center,  currently set xOrigin= 0.0. If it is special, input your value!
     * @param pos The position from the ditector to sample, default value is obtained from meta data. 
     *                         If there is any special, you can input hear. Otherwise input 0.0.
     *@param   thetaVect  the detector tube 2 theta position (nScan * nTubes)           
     * @return two vector arrays totals[2][numSlices]
     *                 totals[0][numSlices]   present number of neutron in each integrated bin
     *                 totals[1][numSlices]   present value of error in each integrated bin
     *                 totals[2][numSlices]   present value of thetaVect in each integrated bin
     */

	public static double[][] ovalIntegration(double[][] data, 
			int numSlices,  FPoint startpoint, FPoint endpoint, double xOrigin,
			double yOrigin, double pos, double[] thetaVect){
		OvalRegionIntegrationImpl ovint = new OvalRegionIntegrationImpl();		
		return ovint.ovalMaskRegion(data, numSlices, startpoint, endpoint, xOrigin, yOrigin, pos,  thetaVect);

}
	/**
     * Finds the global integration about a mask.
     * @param data, The data to be integrated  with format  (nDetCount * nTubes) .
     * Points where data = 0 are considered to be rejected (or "masked").
     * @param err An array to put the error values into.
     * @param stdDev An array to put the standard deviation values into.
     * @param numSlices How many slices should be considered.
     * @param startpoint The atart point to consider with two d point (x1,y1).
     * @param endpoint  the end point to consider  with two d point (x2,y2).
     * @param xOrigin  The x coodinate of beam center,  currently set xOrigin= 0.0. If it is special, input your value!
     * @param yOrigin  The y coodinate of beam center,  currently set xOrigin= 0.0. If it is special, input your value!
     * @param pos The position from the ditector to sample, default value is obtained from meta data. 
     *                         If there is any special, you can input hear. Otherwise input 0.0.
 	 * @param thetaVect: the detector tube 2 theta position (nScan * nTubes)
	 * @return two vector arrays totals[2][xPexels]
     *                 totals[0][xPexels]   present number of neutron in each integrated bin
     *                 totals[1][xPexels]   present value of error in each integrated bin
     *                 totals[2][numSlices]   present value of thetaVect in each integrated bin               
	 * @throws ObjectAccessException
	 */
	public static double[][] genericIntegration(double[][] expData, int[][] maskMap, 
			int numSlices,   double xOrigin,	double yOrigin, double pos, double[] thetaVect)  throws ObjectAccessException{
			GenericMaskProcessImpl gint = new GenericMaskProcessImpl();
			
		return gint.genericMaskIntegration (expData, maskMap, numSlices, xOrigin, yOrigin, pos, thetaVect);

}
	/**
	 * 
	 * @param expData    Two D  double data set from upsteam or from experiement with format  (nDetCount * nTubes) 
	 * @param maskMap1 The mask file to be added
	 * @param maskMap2 The mask file to be added
	 * @return New mask file after two old mask file addition.
	 * @param flag              control parameters flag = 1 for "OR"   calculation
     * 															  flag = 2 for "AND"  calculation
     * * 														  flag = 3 for "A - B"  calculation
     * 
	 * @throws ObjectAccessException
	 */
	public static int[][] genericMaskAddition(double[][] expData, int[][] maskMap1, int[][] maskMap2, int flag )
												throws ObjectAccessException{
			GenericMaskProcessImpl gint = new GenericMaskProcessImpl();
			
		return gint.generiMaskProcess(expData, maskMap1, maskMap2, flag );

}
	/**
	 * This is element wise file addition process. When user applies for mergering  files, one should consider about element weight.
	 *  This algorithm is designed to use as flexiable as possible 
	 * @param datSam1   Two D array (file) is going to merge.
	 * @param datSam2   Two D array (file) is going to merge.
	 * @param weight1     Element weight for array1. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 * @param weight2     Eelement weight for array2. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 * @return
	 */
	public static double[][] echidnafileAddition (double[][] datSam1, double[][] datSam2, 
														double[][] weight1, double[][] weight2 ){
		EchidnafileProcess efp = new EchidnafileProcess();
		return efp.echidnafileAddition(datSam1, datSam2, weight1, weight2);
	
	}
	
	/**
	 * This is column or row wise file addition process. When user applies for mergering  files, one should consider about element weight.
	 *  This algorithm is designed to use as flexiable as possible 
	 * @param datSam1   Two D array (file) is going to merge.
	 * @param datSam2   Two D array (file) is going to merge.
	 * @param weight1     Element weight for array1. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 * @param weight2     Eelement weight for array2. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 *  @param nWise      Column wise or row wise controller   nWise = 0 is column wise and nWise = 1 is row wise merge.                         
	 *                              
	 * @return
	 */
	public static  double[][] echidnafileAdditioncrw (double[][] datSam1, double[][] datSam2, 
			                            double[] weight1, double[] weight2, int nWise ){
		EchidnafileProcess efp = new EchidnafileProcess();
		
		return efp.echidnafileAdditioncrw(datSam1, datSam2, weight1, weight2, nWise);
	}
		
	/**
	 * This is element wise file addition process. When user applies for mergering  files, one should consider about element weight.
	 *  This algorithm is designed to use as flexiable as possible 
	 * @param datSam1   Two D array (file) is going to merge.
	 * @param datSam2   Two D array (file) is going to merge.
	 * @param weight1     Element weight for array1. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 * @param weight2     Eelement weight for array2. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 * @return
	 */
	public static  double[][] echidnafileSubtraction (double[][] datSam1, double[][] datSam2, 
			                      double[][] weight1, double[][] weight2 ){
		EchidnafileProcess efp = new EchidnafileProcess();
		return efp.echidnafileSubtraction(datSam1, datSam2, weight1, weight2);
	}
	
	/**
	 * This is collumn or row wise file addition process. When user applies for mergering  files, one should consider about element weight.
	 *  This algorithm is designed to use as flexiable as possible 
	 * @param datSam1   Two D array (file) is going to merge.
	 * @param datSam2   Two D array (file) is going to merge.
	 * @param weight1     Element weight for array1. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 * @param weight2     Eelement weight for array2. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 * @param nWise      Column wise or row wise controller   nWise = 0 is column wise and nWise = 1 is row wise merge
	 * @return
	 */
	public  static double[][] echidnafileSubtractioncrw (double[][] datSam1, double[][] datSam2, double[] weight1,
			                                                   double[] weight2, int nWise ){
		EchidnafileProcess efp = new EchidnafileProcess();
		return efp.echidnafileSubtractioncrw(datSam1, datSam2, weight1, weight2, nWise);
		
	}
	
	/**
	 * This is element wise file Multiplication process. When user applies for mergering  files, one should consider about element weight.
	 *  This algorithm is designed to use as flexiable as possible 
	 * @param datSam1   Two D array (file) is going to merge.
	 * @param datSam2   Two D array (file) is going to merge.
	 * @param weight1     Element weight for array1. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 * @param weight2     Eelement weight for array2. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 * @return
	 */
	public  static  double[][] echidnafileMultiplication (double[][] datSam1, double[][] datSam2, 
			                     double[][] weight1, double[][] weight2 ){
		EchidnafileProcess efp = new EchidnafileProcess();
		return efp.echidnafileMultiplication(datSam1, datSam2, weight1, weight2);
	}
	
	/**
	 * This is collumn or row wise file Multiplication process. When user applies for mergering  files, one should consider about element weight.
	 *  This algorithm is designed to use as flexiable as possible 
	 * @param datSam1   Two D array (file) is going to merge.
	 * @param datSam2   Two D array (file) is going to merge.
	 * @param weight1     Element weight for array1. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 * @param weight2     Eelement weight for array2. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 *@param nWise      Column wise or row wise controller   nWise = 0 is column wise and nWise = 1 is row wise merge
	 * @return
	 */
	public   static double[][] echidnafileMultiplicationcrw (double[][] datSam1, double[][] datSam2, double[] weight1, 
			                                                    double[] weight2, int nWise ){
		EchidnafileProcess efp = new EchidnafileProcess();
		return efp.echidnafileMultiplicationcrw(datSam1, datSam2, weight1, weight2, nWise);
	}
	
	/**
	 * This is element wise file addition process. When user applies for mergering  files, one should consider about element weight.
	 *  This algorithm is designed to use as flexiable as possible 
	 * @param datSam1   Two D array (file) is going to merge.
	 * @param retio         Element weight for datSam. This is  optional input. If  this param is input with null, program will set 
	 *                              all element weight to 1
	 * @return
	 */
	public   static double[][] echidnDataReNorm (double[][] datSam, double ratio ){
		EchidnafileProcess efp = new EchidnafileProcess();
		return efp.echidnDataReNorm(datSam, ratio);
	}
	/**
	 * 
	 * @param data         One D array input data
	 * @param lambda    Wavelenght  related to input oneD array data[i]
	 * @param theta0      initial theta0 for first detector tube
	 * @param dtheta      delta theta between two detectors
	 * @return  neutq      A return varible which contain two verctors
	 *                                neutq[0][i]  give Q verctor
	 *                                neutq[1][i]  give  number of neutrons respect to  Q[i] value
	 */
	public static double[][]  theta2Qconvertion (double[] data, double  lambda, double theta0, double dtheta ) {
		
		QTwoThetaConvention t2q = new QTwoThetaConvention();
		
		return t2q.theta2QConvertion ( data,  lambda,  theta0,  dtheta );
	}
	
	 /**
	 * 
	 * @param data            One D array input data  with format  (nDetCount * nTubes) 
	 * @param lambda       Wavelenght  related to input oneD array data[i]
	 * @param thetavect    theta vector, input vector values. the dimension should be same as data.length
	 * @return  neutq         A return varible which contain two verctors
	 *                                    neutq[0][i]  give Q verctor
	 *                                    neutq[1][i]  give  number of neutrons respect to  Q[i] value
	 */ 
	public static double[][]  theta2Qconvertion (double[] data, double  lambda, double[] thetavect) {
		
		QTwoThetaConvention t2q = new QTwoThetaConvention();
		
		return t2q.theta2QConvertion ( data,  lambda,  thetavect );
	}
	
	 /**
	 * 
	 * @param lambda       Wavelenght  related to input oneD array data[i]
	 * @param thetavect    theta vector, input vector values. the dimension should be same as data.length
	 * @return  neutq         A return varible which contain two verctors
	 *                                    neutq[i]  give Q verctor
	 */ 
	public static double[]  theta2QCal (double  lambda, double[] thetavect) {
		
		QTwoThetaConvention t2q = new QTwoThetaConvention();
		
		return t2q.theta2QConvertion( lambda,  thetavect );
	}
	
 	/**
 	 * A program to read ascii data file from a file server.
	 * @param dir  iutput file path
	 * @param file  iutput file name
	 * @param nrow: number of row  for data file  (detector counts), I fyou don't know exactly number, you can set it is 0.
	 *                 If you don't know exactly number, you can set it is 0.
	 * @throws Exception
 	 */
  public  static  double[]  ASCIIData1DInput(String dir, String file ,int nrow) throws Exception {
	  
	  DataStore ds = new  DataStore(null);
	  
	  return ds.ASCIIData1DInput(dir, file, nrow);
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
  public static double[][]   DataAsciiIntput2D(String dir, String file, int ncolu, int nrow) throws Exception {
	  
	    DataStore ds = new  DataStore(null);
	  	
	
	  	return  ds.ASCIIData2DInput(dir, file, ncolu, nrow);
	}
	
 	/**
 	 * 
	 * @param ods  two  D dataset object for intput with format  (nDetCount * nTubes) 
	 * @param dir  output file path
	 * @param file  output file name
	 * @throws Exception
 	 */
  public static void   DataAsciiOutput2D(double[][] ods, String dir, String file) throws Exception {
	  
	    DataStore ds = new  DataStore(null);
	  	ds.DataAsciiOutput2D(ods, dir, file);
	
	}
  
 	/**
 	 * 
	 * @param ods  one D dataset object for intput .
	 * @param dir  output file path
	 * @param file  output file name
	 * @throws Exception
 	 */
  public static void  DataAsciiOutput1D(double[] ods, String dir, String file) throws Exception {		        

	  DataStore ds = new  DataStore(null);
	  	ds.DataAsciiOutput1D(ods, dir, file);
	  	
	} 
  
	/**
	 * 
	 * @param ods  two D dataset object for output
	 * @param dir  output file path
	 * @param file  output file name
	 * @return  null;
	 * @throws Exception
	 */
  public static void  DataOutput2D(Object[][] ods, String dir, String file) throws Exception {		        

	  DataStore ds = new  DataStore(null);
	  ds.DataOutput(ods, dir, file);
	  	
	} 
	
}
