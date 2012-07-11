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
public interface HorizontalIntegration {

	/**
     * Finds the horizontal integration about a point.
     * @param data The data to be integrated.
     * Points where data = 0 are considered to be masked.
	 * @param numSlices How many slices should be considered.
	 * @param xOrigin
	 * @param yOrigin
	 * @param thetaVect TODO
	 * @param minYi The minimum distance to consider.
	 * @param maxYi The maximum distance to consider.
	 * @param posi The position from the vertical to be considered as Y for the output.
	 * @return The position integration.
     */
    public  double[][] findHorizontalIntegration(double[][] data, int numSlices,
			double minDist,
            double maxDist, double xOrigin, double yOrigin, double pos, double[] thetaVect);
}
