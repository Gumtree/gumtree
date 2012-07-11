/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package au.gov.ansto.bragg.echidna.dra.algolib.processes;

import au.gov.ansto.bragg.common.dra.algolib.math.FPoint;

/**
 * @author jgw
 *
 */
public interface SquareRegionIntegration {
	/**
     * Finds the square integration about a mask.
     * @param data The data to be integrated.
     * Points where data = 0 are considered to be masked.
	 * @param numSlices How many slices should be considered.
	 * @param startpoint The atart point to consider.
	 * @param endpoint  the end point to consider.
	 * @param thetaVect TODO
	 * @return The integrated data set.
     */
    public  double[][] findSquareMask(double[][] data, int numSlices,
			FPoint startpoint,FPoint endpoint, double[] thetaVect  );

}
