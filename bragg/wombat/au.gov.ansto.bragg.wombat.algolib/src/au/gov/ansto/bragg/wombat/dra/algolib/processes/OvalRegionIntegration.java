/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package au.gov.ansto.bragg.wombat.dra.algolib.processes;

import au.gov.ansto.bragg.common.dra.algolib.math.FPoint;

/**
 * @author jgw
 *
 */
public interface OvalRegionIntegration {
	
/*	*
    * Finds the cross section through a point.
    * @param data The data to be used.
    * Points where data = 0 are considered to be masked.
    * @param err An array to put the error values into.
    * @param stdDev An array to put the standard deviation values into.
    * @param numSlices How many slices should be considered.
    * @param startpoint The atart point to consider.
    * @param endpoint  the end point to consider.
    * @param xCenter The beam center X coordinate.
    * @param yCenter The beam center Y coordinate.
    * @param  pos   the distance from sample to detector
	 * @param thetaVect  two theta vector for wombat detectors.
    * @return The cross section
    */
   public  double[][] ovalMaskRegion (double[][] data, int numSlices,
			FPoint startpoint,
           FPoint endpoint, double xCenter,   double yCenter, double pos, double[] thetaVect); 

}
