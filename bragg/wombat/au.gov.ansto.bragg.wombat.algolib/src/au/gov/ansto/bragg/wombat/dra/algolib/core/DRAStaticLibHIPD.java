package au.gov.ansto.bragg.wombat.dra.algolib.core;
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
import au.gov.ansto.bragg.wombat.dra.algolib.processes.HIPDGeometryCorrectImpl;

import com.thoughtworks.xstream.converters.reflection.ObjectAccessException;
import au.gov.ansto.bragg.wombat.dra.algolib.entity.HIPDDetector;
import au.gov.ansto.bragg.wombat.dra.algolib.processes.*;
/**
 * @author jgw
 *
 */
public class DRAStaticLibHIPD {
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
	
	private static HIPDDetector hid = new HIPDDetector();
	
	public static double[][] efficiencyCorrector(double[][] inData, double[][] effMap, int flag, double thresh, boolean inverse ){
		
		HIPDCorrectEfficiencyImpl corr = new HIPDCorrectEfficiencyImpl();
		
		return corr.doSensitivity(inData, effMap,flag, thresh, inverse);
	}
	
	   /**
     * Applies experiment background subtraction to the data. This method is simplified for one background source only.
     * @param data The data array to be filtered.
     * @param bgData The detector background data sets.
     * @return The filtered array.
     */
    public  static   double[][] removeBackground(double[][] data, double[][] bgData, double ratio ) {
 
	        HIPDBgSubtractorImpl rbs = new HIPDBgSubtractorImpl();
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
    public  static  double[][] removeBackground(double[][] data, ArrayList<double[][]> bgData,  ArrayList<Double> ratio ) {
	
    	        HIPDBgSubtractorImpl rbs = new HIPDBgSubtractorImpl();
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
	        HIPDBgSubtractorImpl hbs = new HIPDBgSubtractorImpl();
	        
	        return hbs.removeBackground(data, bgData, ratio1, elecBD, ratio2, effDat, remNag);
    }

	/**
	 * Converts a 3D multiscan data sets into a flat 2d based on the instrument metadata.
	 * This  algorithm is developed for NO overlap multiple scan data set stitching
	 * @param 3D exp data set  to be stitchedwith format  nDataSet * nDetCount * nTubes. 
	 * 					DataSet[nDataSet ][nDetCount ][ nTubes]
	 * @param  detHpixels  Detector horisontal resolution
	 * @param  detVpixels  Detector vertical resolution
	 * 	@param  nScan      number of scan
	 * @param  twoTheta0   the initial position of first tube
	 * @param  deltaTheta   distance between two tubes
	 * @return The stiched 2D data set with detHpixels * nScan in horisontal direction.
	 */

	public static double[][] hipdDataStitch(double[][][] stds, int  detHpixels, int detVpixels,
			int nScan, double  twoTheta0,  double deltaTheta){
		HIPDDataFileSummeryImpl stitch = new HIPDDataFileSummeryImpl();
		
		return stitch.multiDataSetStich(stds,   twoTheta0,    deltaTheta);
		
	}
	
	/**
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
		HIPDGeometryCorrectImpl geocor =new HIPDGeometryCorrectImpl();	
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
            int cmod = nvPix%2;
		    double[] thV = new double [nhPix];
			double[][] inputDat;
			if(cmod !=0){
		    	thV = data[nvPix-1];		
				inputDat = new double[nvPix-1][nhPix];	
				for (int j = 0; j < nvPix -1; j++) 
					for(int n = 0; n < nhPix; n++)
						inputDat[j][n] = data[j][n];
			} else {
				thV = thetaVect;
				inputDat = new double[nvPix][nhPix];
				inputDat = data;
			}
			

		  return hint.findHorizontalIntegration(data, numSlices, minDist, maxDist, xOrigin, yOrigin, pos, thV);
		
	}
	public static double[][] horizontalIntegration(double[][] data,
            int numSlices, double minDist, double maxDist, double xOrigin,
            double yOrigin, double pos){
		    HorizontalIntegrationImpl hint = new HorizontalIntegrationImpl();		
		    int nvPix = data.length;
		    int nhPix = data[0].length;
	         int cmod = nvPix%2;
             double theta0 = 0.0;
             double delTheta = hid.seperation;
		    double[][] inputDat;
		    double[] thV = new double [nhPix];
		    
			if(cmod !=0){
				inputDat = new double[nvPix-1][nhPix];	
				for (int j = 0; j < nvPix -1; j++) 
					for(int n = 0; n < nhPix; n++)
						inputDat[j][n] = data[j][n];
			} else {
				inputDat = new double[nvPix][nhPix];
				inputDat = data;
			}
// Calculate theta verctor

		    if(cmod != 0){
		    	thV = data[nvPix-1];
		         } else {
				for (int nth = 0; nth < nvPix; nth++){
					thV[nth]= theta0 + nth * delTheta;
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
		    int cmod = nvPix%2;
		    double[] thV = new double [nhPix];
			double[][] inputDat;
			if(cmod !=0){
		    	thV = data[nvPix-1];		
				inputDat = new double[nvPix-1][nhPix];	
				for (int j = 0; j < nvPix -1; j++) 
					for(int n = 0; n < nhPix; n++)
						inputDat[j][n] = data[j][n];
			} else {
				thV = thetaVect;
				inputDat = new double[nvPix][nhPix];
				inputDat = data;
			}
	return vint.findVerticalIntegration(inputDat, numSlices, minDist, maxDist, xOrigin, yOrigin, pos, thV);
		
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
		    int cmod = nvPix%2;
		    double[] thV = new double [nhPix];
			double[][] inputDat;
			if(cmod !=0){
		    	thV = data[nvPix-1];		
				inputDat = new double[nvPix-1][nhPix];	
				for (int j = 0; j < nvPix -1; j++) 
					for(int n = 0; n < nhPix; n++)
						inputDat[j][n] = data[j][n];
			} else {
				thV = thetaVect;
				inputDat = new double[nvPix][nhPix];
				inputDat = data;
			}
		return sint.findSquareMask(inputDat, numSlices, startpoint, endpoint, thV);

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
     * 															flag = 2 for "AND"  calculation
     * * 															flag = 3 for "A - B"  calculation
     * 
	 * @throws ObjectAccessException
	 */
	public static int[][] genericMaskAddition(double[][] expData, int[][] maskMap1, int[][] maskMap2, int flag )
												throws ObjectAccessException{
			GenericMaskProcessImpl gint = new GenericMaskProcessImpl();
			
		return gint.generiMaskProcess(expData, maskMap1, maskMap2, flag );

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
