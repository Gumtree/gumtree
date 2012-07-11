/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package au.gov.ansto.bragg.wombat.dra.algolib.processes;

/**
 * @author jgw
 *
 */
public interface VerticalIntegration {
	/**
     * Finds the directional averages about a point.
     * @param data The data to be averaged.
     * Points where data = 0 are considered to be masked.
	 * @param numSlices How many slices should be considered.
	 * @param minDist The minimum distance to consider.
	 * @param maxDist The maximum distance to consider.
	 * @param xOrigin
	 * @param yOrigin
	 * @param thetaVect TODO
	 * @param dir The angle from the vertical to be considered as X for the output.
	 * @return The directional average
     */
    public double[][] findVerticalIntegration(double[][] data, int numSlices,
			double minDist,
            double maxDist, double xOrigin, double yOrigin, double pos, double[] thetaVect);
}
