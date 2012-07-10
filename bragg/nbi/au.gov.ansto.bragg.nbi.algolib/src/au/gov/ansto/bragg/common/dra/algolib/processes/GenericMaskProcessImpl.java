/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package au.gov.ansto.bragg.common.dra.algolib.processes;

import java.io.FileNotFoundException;

import com.thoughtworks.xstream.converters.reflection.ObjectAccessException;

/**
 * @author jgw
 *
 */
public class GenericMaskProcessImpl implements GenericMaskProcess {
	
	public GenericMaskProcessImpl() {
		super();
	}
	
	
/**
 * 
 * @param expData    Two D  float data set from upsteam or from experiement
 * @param maskMap   Two D integer (0 1) mask table which made in somewhere else
 * @param nSlices       The number of slices for masked data set integration
 * @param xOrigin       THe neutron beam center. normally, we always let xOrigin=0
 * @param yOrigin       THe neutron beam center. normally, we always let yOrigin=0
 * @return totOneD     The one demensional data set from 2D data set integration
 * @param pos            The detector position. it is optional parameter.
 * @throws ObjectAccessException
 */
	public double[][]  genericMaskIntegration(double[][] expData, int[][] maskMap, 
			int nSlices,   double xOrigin,	double yOrigin, double pos, double[] thetaVect) 
							throws ObjectAccessException {
// if input data object is empty one, just return as null. 		
		if (expData.length == 0 || expData[0].length == 0) 
			return null;
// if there are different array lengh in two objects, simply return null.
		
		if (expData.length != maskMap.length || expData[0].length !=  maskMap[0].length) 
		{
			return null;
		}
		
		int xWidth  = expData[0].length;
		int yHeight = expData.length;
		double binwidth = xWidth / nSlices;
		
		double[][]  totOneD = new double[3] [nSlices];
		for (int j = 0;  j <3;  j++)
		for (int l = 0; l < nSlices;l++) {
			totOneD[j] [l] =0;
		}
		
try {
		for (int  i =0; i<xWidth; i++) {
			for  (int  j =0; j<yHeight; j++) {
				
				for (int l = 0; l < nSlices; l++) {
					
				if ( i >= l*binwidth && i <  (l+1)*binwidth){
					
				if (maskMap[j][i] == 1) {
					totOneD[0][l] += expData[j][i];
					
				        }
				    }
				}
				
			}
		}
}
		catch (Exception e) {
			e.printStackTrace();
		}

	            for (int x = 0; x < nSlices; x++) {
	            	totOneD[1][x] =  Math.sqrt(Math.abs(totOneD[0][x]));
	            	totOneD[2][x] = thetaVect[x];
//	              	System.out.println("x and totals[0][x], Totals[1][x] = " + x + ",  " + totals[0][x] + ",  " + totals[1][x]);   
	            }
		
		return totOneD;

	}
/**
 * A method to process  mask amp addition
 * @param expData        Two D  double data set from upsteam or from experiement
 * @param maskMap      Two D integer (0 1) mask table which made in somewhere else
 * @param maskMap2    Two D integer (0 1) mask table which made in somewhere else
 * @param flag              control parameters flag = 1 for "OR"   calculation
 * 															flag = 2 for "AND"  calculation
 * 															flag = 3 for "A - B"  calculation
 * 														
 * @throws ObjectAccessException
 * 
 * @return New two D integer mask table with (0,1) map
 */	
	
	public int[][] generiMaskProcess(double[][] expData, int[][] maskMap1, int[][] maskMap2, int flag) 
							throws ObjectAccessException {

		int xwidth1 = maskMap1[0].length;
		int xwidth2 = maskMap2[0].length;
		int yheight1 = maskMap1.length;
		int yheight2 = maskMap2.length;
		int xwidth =0;
		int yheight = 0;

		
// define a new two D mask table arrays
try {
		if (xwidth1 != xwidth2 ) {
			xwidth = Math.max(xwidth1, xwidth2);
		} else {
			xwidth = xwidth1;
		}
		if (yheight1 != yheight2) {
			yheight = Math.max(yheight1, yheight2);
		} else {
			yheight = yheight1;
		}
} catch (Exception e) {
	e.printStackTrace();
}		
		
		int[][]  pmaskMap    = new int [yheight][xwidth];	
		int[][]  nmaskMap1 = new int [yheight][xwidth];
		int[][]  nmaskMap2 = new int [yheight][xwidth];	
	 for (int i = 0; i < xwidth; i++) {
	    	for ( int j = 0; j < yheight; j++ ) {
	    		if( i < xwidth1 && j < yheight1 ) { nmaskMap1[j][i] = maskMap1[i][j];
	    	}	else nmaskMap1[j][i] = 0;
	    		if( i < xwidth2 && j < yheight2 ) { nmaskMap2[j][i] = maskMap2[i][j];
		    	}	else nmaskMap2[j][i] = 0;	    		
	    	}		 
	 }
		 
try {		
	    for ( int i = 0; i < xwidth; i++ ) {
	    	for ( int j = 0; j < yheight; j++ ) {
	    		if(flag==1) {
	    		if( nmaskMap1[j][i] ==0 && nmaskMap2[j][i] ==0 )  pmaskMap[j][i] = 0;
	    		else pmaskMap[j][i] = 1;
	    		}
	    		if (flag == 2) {
		    	if( nmaskMap1[j][i] ==1 && nmaskMap2[j][i] ==1 )  pmaskMap[j][i] = 1;
		    		else pmaskMap[j][i] = 0;    			
	    		}
	    		if (flag == 3) {
			    	if( nmaskMap1[j][i] ==1 && nmaskMap2[j][i] ==0)  pmaskMap[j][i] = 1;
			    		else pmaskMap[j][i] = 0 ;    			
		    		}	    		
	    	}
	    }
} catch (Exception e) {
	e.printStackTrace();
}
				
		return pmaskMap;
		
	
	}
	
/**
 *  A generic method to produce mask boundry from input mask 2D tables
 * @param maskMap
 * @return a new 2D mask table
 */	
	public int[][] genericMaskBoundry(int[][]maskMap){
		
		return maskMap;
		
	}

}
