/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package au.gov.ansto.bragg.common.dra.algolib.processes;

import com.thoughtworks.xstream.converters.reflection.ObjectAccessException;

/**
 * @author jgw
 *
 */
public interface GenericMaskProcess {
	
	/**
	 * 
	 * @param expData    Two D  float data set from upsteam or from experiement
	 * @param maskMap   Two D integer (0 1) mask table which made in somewhere else
	 * @param nSlices       The number of slices for masked data set integration
	 * @param xOrigin       THe neutron beam center. normally, we always let xOrigin=0
     * @param yOrigin       THe neutron beam center. normally, we always let yOrigin=0
	 * @return totOneD     The one demensional data set from 2D data set integration
	 * @throws ObjectAccessException
	 */
		public double[][]  genericMaskIntegration(double[][] expData,int[][] maskMap, 
				int nSlices,   double xOrigin,	double yOrigin, double pos, double[] thetaVect) 
								throws ObjectAccessException;
		
		/**
		 * A method to process  mask amp addition
		 * @param expData        Two D  float data set from upsteam or from experiement
		 * @param maskMap      Two D integer (0 1) mask table which made in somewhere else
		 * @param maskMap2    Two D integer (0 1) mask table which made in somewhere else
		 *  @param flag              control parameters flag = 1 for "OR"   calculation
		 * 															flag = 2 for "AND"  calculation
		 * 															flag = 3 for "A - B"  calculation
		 * @throws ObjectAccessException
		 * 
		 * @return New two D integer mask table with (0,1) map
		 */	
			
			public int[][] generiMaskProcess(double[][] expData, int[][] maskMap1, int[][] maskMap2, int flag) 
									throws ObjectAccessException;
			
			/**
			 *  A generic method to produce mask boundry from input mask 2D tables
			 * @param maskMap
			 * @return a new 2D mask table
			 */	
				public int[][] genericMaskBoundry(int[][]maskMap);
}
