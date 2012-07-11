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
public interface HIPDCorrection {
    /**
     * Eliminates the background data. Data is normalised by the monitor counts
     * if possible.
     * @param iSample The sample data.
     * @param monSample The monitor counts for the sample scan.
     * @param iEmpty The empty cell data.
     * @param monEmpty The monitor counts for the empty cell scan.
     * @param iBlocked The background data.
     * @param monBlocked The monitor counts for the background scan.
     * @param sensitivity The detector sensitivity.
     * @param tSample The sample transmission.
     * @param tEmpty The empty cell transmission.
     * @param removeNegatives Whether to remove negative values from the
     * data.
     * @param background The flat background reading.
     * @return The corrected data.
     * @throws Exception If the background and data are not the same size
     */
    public  float[][] BGcorrect(float[][] iSample, float monSample, float[][] iEmpty,
            float monEmpty, float[][] iBlocked,
            float monBlocked, float[][] sensitivity, float tSample,
            float tEmpty, boolean removeNegatives, float background) throws Exception;
    /**
     * Non graphic version! All are independent!
     * Applies to detector sensitivity correction to the data.
     * @param data The data array to be filtered.
     * @param sensitivity The detector sensitivity data table.
     * @return The filtered array.
     */
    public  float[][] doSensitivity( float[][] data, int flag, float thresh, float[][] sensitivity ); 
}
