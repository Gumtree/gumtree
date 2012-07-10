/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
*/
package au.gov.ansto.bragg.common.dra.algolib.calibration;

/**
 * @author jgw
 * Tel: +61 2 9717 7062  Fax: +61 2 9717 9799
 * Data Analysis Team, Bragg Institute,Bld.82
 * ANSTO PMB 1 Menai NSW 2234 AUSTRALIA

 */
public class DetectorPixelOKProcess {
	

	/**
	 * This method is developed to generate Pixel OK map depending upon calibration scan data set and shreshold value
	 * @param calDat    Calibration scan data set with two D array input with matrics [yCount][xDetector]
	 *                                  xDetector should be  the number of detectors in two theta direction
	 * @param legal       special boolean control parameter
	 * @param shreshold   Shresholp value for detector pixels
	 * @return  A pixel OK map of twoD integer array 
	 */
	
	public int[][]  powderDetectorPixelOKmap(double[][] calDat, boolean legal, double shreshold) {
		
		int xPixels = calDat[0].length;
		int yPixels = calDat.length;
		int[][] pixMap = new int[yPixels][xPixels];
		
		for(int j = 0; j < yPixels; j++) {
			for( int k = 0; k <  xPixels; k++){
				if (calDat[j][k] < shreshold) pixMap[j][k] = 0;
				else pixMap[j][k] = 1;
			}
		}
		
		return pixMap;
	}

}
