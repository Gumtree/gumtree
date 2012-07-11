/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package au.gov.ansto.bragg.echidna.dra.algolib.processes;

import au.gov.ansto.bragg.common.dra.algolib.processes.Signal;

/**
 * @author jgw
 *
 */
public interface GlobalIntegration {
	/**
     * Finds the global integration about a point.
     * @param data The data to be integrated.
     * Points where data = 0 are considered to be masked.
	 * @param numSlices How many slices should be considered.
	 * @param thetaVect TODO
	 * @return two vector arrays totals[2][xPexels]
     *                 totals[0][xPexels]   present number of neutron in each integrated bin
     *                 totals[1][xPexels]   present value of error in each integrated bin
     */
    public  double[][] findGlobalIntegration(double[][] data, int numSlices, double[] thetaVect );

	Signal processNew(Signal in);

}
