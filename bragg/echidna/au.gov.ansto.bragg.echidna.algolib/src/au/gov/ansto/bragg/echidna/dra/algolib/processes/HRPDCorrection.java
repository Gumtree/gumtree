/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package au.gov.ansto.bragg.echidna.dra.algolib.processes;

import java.util.ArrayList;

/**
 * @author jgw
 *
 */
public interface HRPDCorrection {
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
    public  double[][] BGcorrect(double[][] iSample, double monSample, double[][] iEmpty,
            double monEmpty, double[][] iBlocked,
            double monBlocked, double[][] sensitivity, double tSample,
            double tEmpty, boolean removeNegatives, double background) throws Exception;
    /**
     * Applies experiment background subtraction to the data.
     * @param data The twoD  array data to be filtered. with format data[yCounts][xTubes]
     * @param bgData  The detector background data sets from electronics or other equipment.
     * @param ration1 The ratio background proportional to time following bgData.
     * @param elecBD  The detector background data sets from known background source.
     * @param ration The ratio2 background proportional to time following elecBG.
     * @param effDat   the Detector efficiency table for BG correction
     * 
     * @return The corrected twoD  arraydata set  with format data[yCounts][xTubes]
     */
    public  double[][] removeBackground(double[][] data, double[][] bgData,  double ratio1,  
    		      double[][] elecBD, double ratio2, double[][] effDat, boolean remNag );
    
	   /**
     * Applies experiment background subtraction to the data. This method is simplified for one background source only.
     * @param data The data array to be filtered.
     * @param bgData The detector background data sets.
     * @return The filtered array.
     */
    public  double[][] removeBackground(double[][] data, double[][] bgData, double ratio );
    
    /**
     * Applies experiment background subtraction to the data. This method is designed  for 
     * multiple BG sources. Be careful about  "ArrayList".
     * @param data The data array to be filtered.
     * @param bgData The detector background ArrayList data sets. Since BG can contribute from different  sources, 
     *                                  we therefore design BG data as a array list to process different bg sources.
     * @param ratio       propotion of  Arraylist BGs against time or other facts.
     * @return The filtered data array.
     */
    public  double[][] removeBackground(double[][] data, ArrayList<double[][]> bgData,  ArrayList<Double> ratio );
    
    /**
     * Applies detector sensitivity to the data.
     * @param data The data array to be filtered.
     * @param sensitivity The detector sensitivity data.
     * @param flag contrl parameter flag =1 do correction; flag=0 do nothing
     * @param Control threshold parameter to reject very small efficiency block
     * @param inverse   boolean control parameter 
     *                                if detection efficience less than 100%  set it false
     *                                if detection efficience greater than 100%  set it true
     * @return The filtered array.
     */
   
    public  double[][] doSensitivity(double[][] data, double[][] sensitivity,int flag, double thresh, boolean inverse ) ; 
}
