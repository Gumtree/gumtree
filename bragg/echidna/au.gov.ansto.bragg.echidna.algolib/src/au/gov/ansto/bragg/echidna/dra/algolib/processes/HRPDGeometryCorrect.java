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
public interface HRPDGeometryCorrect {
	
	/**
	 * Apply for geometry correction for HRPD curvered detector
	 * @param iSample
	 * @param geometry  in case new detector setup with some new geometery parameters
	 *                   if there is no change, just input "null"
	 * @param thetaVect   OneD array theta vector which presents ditector tube position.
	 * @param Geom    Always true!
	 * @param Zpvertic TODO
	 * @param detectArc Curved detecter arch length with degree, ex. HRPD detector detectArch = 158.75;
	 * @return Two dimensional data table
	 */
	   public double[][] correctGeometry(double[][] iSample,  double[][] geometry, double detectArch, double[] thetaVect,  boolean Geom, double[][] Zpvertic);
}
