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
public interface SpecialRegionIntegration {
	/**
     * Finds the cross section through a point.
     * @param data The data to be used.
     * Points where data = 0 are considered to be masked.
     * @param numSlices How many slices should be considered.
     * @param minDist The minimum distance to consider.
     * @param maxDist The maximum distance to consider.
     * @param xCenter The beam center X coordinate.
     * @param yCenter The beam center Y coordinate.
     * @param dir The angle from the vertical to be considered as X for the output.
    * @return two vector arrays totals[2][xPexels]
     *                 totals[0][xPexels]   present number of neutron in each integrated bin
     *                 totals[1][xPexels]   present value of error in each integrated bin
     */
    public  double[][] findMaskArea(double[][] data, 
            int numSlices, double minDist, double maxDist, double xCenter,
            double yCenter, double dir);
}
