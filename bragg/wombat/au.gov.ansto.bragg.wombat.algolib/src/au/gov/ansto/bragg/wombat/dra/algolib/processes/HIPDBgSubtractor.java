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
public interface HIPDBgSubtractor {
	   /**
     * Applies experiment background subtraction to the data.
     * @param data The twoD  array data to be filtered. with format data[yCounts][xTubes]
     * @param bgData  The detector background data sets from electronics or other equipment.
     * @param ration1 The ratio background proportional to time following bgData.
     * @param elecBD  The detector background data sets from known background source.
     * @param ration The ratio2 background proportional to time following elecBG.
     * @param effDat   the Detector efficiency table fro BG correction
     * 
     * @return The corrected twoD  arraydata set  with format data[yCounts][xTubes]
     */
    public  double[][] removeBackground(double[][] data, double[][] bgData,  double ratio1,  
    		      double[][] elecBD, double ratio2, double[][] effDat, boolean remNag );

}
