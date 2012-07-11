/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package au.gov.ansto.bragg.echidna.dra.algolib.processes;

/**
 * @author jgw
 *
 */
public interface HRPDDataSetStitch {
	/**
	 * Converts a 3D multiscan data sets into a flat 2d based on the instrument metadata.
	 * @param  twoTheta0   the initial position of first tube
	 * @param  deltaTheta   distance between two tubes
	 * @param stepsize TODO
	 * @param 3D exp data set  to be stitchedwith format  nDataSet * nDetCount * nTubes. 
	 * 					DataSet[nDataSet ][nDetCount ][ nTubes]
	 * @return 2D array data double object with nCounts* nScan elements
	 *                  Special return array retData[nCounts] (normally retData[512]) present new theta vector.
	 */
	 
	public double[][] multiDataSetStich(double[][][] stds, double  twoTheta0, double deltaTheta, double[] stepsize);
	 /**
	  * 
	  * @param inDat      Input 3D file with nScan * nCounts * nTubes
	  * @param array2theta  Multiple scan 2D 2theta array nScan * nTubes
	  * @param stepsize  Multiple scan step size 1D array with nScan elements
	  * @param binsize   Optional user control parameter for number of bins stitching to
	  * @param twotheta0 The two theta for first tube in the first scan.
	  * @return 2D array data double object with nCounts*(nScan*nTubes) elements
	  */
	public double [][] echidnaDataStitch( double[][][] inDat, double [][] array2theta, double[] stepsize, double binsize, double twotheta0);

	
	 /**
	  * This  algorithm is developed for overlap multiple scan data set stitching and use ideal two theta vector
	  *  for each detector tube. Algorithm will take 1.25 degree and separation of two tubes and average stepsize
	  *  as binsize. two theta value for each detector position will be calculated instead of data from meta data
	  *  table. Number of bins will take value as nScan * nTubes if there is no overlap of the tubes. Otherwise 
	  *  number of bins will be calculated use (detctorArc + nScan*stepsize)/stepsize.
	  *	Converts a 3D multiscan data sets into a flat 2d based on the instrument metadata. 
	  * @param inDat      Input 3D file with nScan * nCounts * nTubes
	  * @param array2theta  Multiple scan 2D 2theta array nScan * nTubes
	  * @param stepsize  Multiple scan step size 1D array with nScan elements
	  * @param binsize   Optional user control parameter for number of bins stitching to.
	  * @param twotheta0 The two theta for first tube in the first scan.
	  * @return 2D array data double object with nCounts*((detctorArc + nScan*stepsize)/stepsize) elements
	  *                  Special return array retData[nCounts] (normally retData[512]) present new theta vector.
	  */
	public double [][] echidnaIdealDataStitch( double[][][] inDat, double [][] array2theta, double[] stepsize, 
			                        double binsize, double twotheta0);
}
