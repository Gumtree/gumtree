/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
*/
package au.gov.ansto.bragg.common.dra.algolib.errPro.ui;

import au.gov.ansto.bragg.common.dra.algolib.errPro.processes.DataErrorShower;
import au.gov.ansto.bragg.common.dra.algolib.errPro.processes.ErrorInitialization;
import au.gov.ansto.bragg.common.dra.algolib.errPro.processes.LibErrorPropagation;

/**
 * @author jgw
 * Tel: +61 2 9717 7062  Fax: +61 2 9717 9799
 * Data Analysis Team, Bragg Institute,Bld.82
 * ANSTO PMB 1 Menai NSW 2234 AUSTRALIA

 */
public class DataErrorProcessAPI {
	
	
	/**
	 * 
	 * @param inRawDat  Raw data from detector or anywhere else with [nScan][nCount][nTubes] format
	 * @param thetaVect   theta vector for each detector in each scan
	 * @return error array with same dimension as input data
	 */
	public double[][][]  rawErrorCreation(double[][][] inRawDat, double[][] thetaVect) {
		
		ErrorInitialization eit = new ErrorInitialization();
		
		return eit.rawErrorCreation(inRawDat, thetaVect);
	}
	/**
	 * 
	 * @param inDat   input data set
	 * @param inErr    input data error set
	 * @return   propagated result
	 */
	
	public double[][] errorPropagProcess ( double[][] inDat, double[][]inErr){
		int vPix = inDat.length;
		int hPix = inDat.length;
		double[][] err2D =inDat;
		LibErrorPropagation  ep = new LibErrorPropagation();
//		for(int k = 0; k < vPix; k++)
//			for(int j = 0; j < hPix; j++)

		
		return err2D;
	}	
	/**
	 * 
	 * @param inDat1   input data set
	 * @param inErr1    input data error set
	 * @param inDat2   input data set   indat1 and indat2 should have same dimension.
	 * @param inErr2    input data error set
	 * @return
	 */
	
	public double[][] errorPropagation ( double[][] inDat1, double[][]inErr1, double[][] inDat2, double[][] inErr2){
		int vPix = inDat1.length;
		int hPix = inDat1.length;
		double[][] err2D =null;
		LibErrorPropagation  ep = new LibErrorPropagation();
		for(int k = 0; k < vPix; k++)
			for(int j = 0; j < hPix; j++)
		  err2D[k][j] = ep.errorEdditionPropagation(inDat1[k][j], inErr1[k][j], inDat2[k][j], inErr2[k][j]);
		
		return err2D;
	}
	
	/**
	 * @param inExpDat    integrated data and thetaVect  ( with error optional)
	 * @param  errArray,   optional parameter  in case inExpDat are corrected using other algorithms.
	 * @param emParam  emplify param 1:1 or 1:2 ....
	 * @param exprtForm  dat export format:
	 * 	                                  0:   with  data +- err
	 *                                    1:  with   dat + err  and data - err
	 * @return
	 */
	public double[][] dataErrorShower(double[][] IntedDat, 	double[] errArray, 
			                                                               double emParam, int exportForm) {
		DataErrorShower des = new DataErrorShower();
		
		return des.errorProIntedData(IntedDat, errArray, emParam, exportForm);
		
	}
}
