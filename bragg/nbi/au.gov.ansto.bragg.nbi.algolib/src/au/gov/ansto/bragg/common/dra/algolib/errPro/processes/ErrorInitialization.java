/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
*/
package au.gov.ansto.bragg.common.dra.algolib.errPro.processes;

import java.util.Iterator;

/**
 * @author jgw
 * Tel: +61 2 9717 7062  Fax: +61 2 9717 9799
 * Data Analysis Team, Bragg Institute,Bld.82
 * ANSTO PMB 1 Menai NSW 2234 AUSTRALIA

 */
public class ErrorInitialization {
	int nScan;
	int nCount;
	int nTubes;
	double[][][] errorArray;
	/**
	 * 
	 * @param inRawDat  Raw data from detector or anywhere else with [nScan][nCount][nTubes] format
	 * @param thetaVect   theta vector for each detector in each scan
	 * @return error array with same dimension as input data
	 */
	public double[][][]  rawErrorCreation(double[][][] inRawDat, double[][] thetaVect) {
		 nScan  = new Integer(inRawDat.length);
		 nCount = new Integer(inRawDat[0].length);
		 nTubes = new Integer(inRawDat[0][0].length);
		 errorArray = new double[nScan][nCount][nTubes];
		 int n, j, k;
		 for (n= 0; n < nScan; n++) {
			 for ( j = 0; j < nCount; j++) {
				 for (k = 0; k < nTubes; k++) {
					 errorArray[n][j][k] = Math.sqrt(inRawDat[n][j][k]);
				 }
				 
			 }
		 }
		
		return errorArray;
		
	}

}
