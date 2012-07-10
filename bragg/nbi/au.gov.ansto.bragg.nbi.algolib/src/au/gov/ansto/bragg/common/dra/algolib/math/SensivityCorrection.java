/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */
/**
 * @author J. G. WANG
 */

package au.gov.ansto.bragg.common.dra.algolib.math;

/**
 *OPAL Neutron Scttering software package designed to 
 *  make online data reduction.
 *
 */
public class SensivityCorrection {
	/**
	 * 
	 * @param data   input 2D exp float data set
	 * @param sensitivity    Pre-mad eff 2D table
	 * @param flag    Correction flag = 1 for over measuring data
	 * 									  flag = 2 for less sensitive measuring data
	 * @return   corrected 2D data array
	 */
    public static float[][] doSensitivity(float[][] data, float[][] sensitivity, int flag) {
        if (sensitivity == null || data == null) {
            return data;
        }
        if (sensitivity.length != data.length
                || sensitivity[0].length != data[0].length) {
            return null;
        }
        float[][] out = new float[data.length][data[0].length];
        int i, j;
        for (i = 0; i < data.length; i++) {
            for (j = 0; j < data.length; j++) {
                if (sensitivity[i][j] == 0.0F )
                	return null;
              if(flag==1)  out[i][j] = data[i][j] / sensitivity[i][j];
              if(flag==2)  out[i][j] = data[i][j] * sensitivity[i][j];
            }
        }
        return out;
}
}
