/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package au.gov.ansto.bragg.process.signal;

/**
 * An abstract instance for instrument signal type.  <p> The input signal of each instrument algorithm is a child class of SignalType.  The instance of the input signal will be created when a data is loaded from the data importing component. 
 * @author  nxi
 * @version  V1.0
 * @since  M1
 */
public abstract interface SignalType {
	
	/**
	 * This method returns the main data signal of the instrument reading.
	 * For example, in echidna and wombat instrument reading, it returns
	 * the detector reading. 
	 * @return  three dimensional doulbe array
	 */
	public double[][][] getScanData();
	
	/**
	 * A method to get the theta vector from the data signal.
	 * Theta vector is a matrix that contains theta values of every detector
	 * in every scan point. 
	 * @return two dimensional double array
	 */
	public double[][] getThetaVector();
	
	/**
	 * A method to get the scan step from the data signal. A scan step is 
	 * the angel that the detector changes in each step. Scan steps for 
	 * all scan point groups an two dimensional array. 
	 * @return one dimensional double array
	 */
	public double[] getScanStep();
	
	/**
	 * A method to get monitor data. 
	 * @return one dimensional long array
	 */
	public long[] getMonitorData();
	
	/**
	 * A method to get the efficiency map. The efficiency map is about the
	 * efficiency parameter for each pixel in the detector. It is used to 
	 * correct the reading of the detector.
	 * @return two dimensional double array
	 */
	public double[][] getEfficiencyMap();
	
	/**
	 * A method to get the geometry map of the detector. The geometry map is
	 * about the relative location of each pixel of the detector to the sample.
	 * It is used to correct the reading of the detector.
	 * @return two dimensional double array
	 */
	public double[][] getGeometryMap();
	
	/**
	 * A method to get the name of the file that contains the geometry map.
	 * @return file name in String object
	 */
	public String getGeometryFilename();
	
	/**
	 * Get the file name that contains the data signal information.
	 * @return file name in String object
	 */
	public String getFilename();
	
	/**
	 * A method to get data rank information. For example, 0 for int, 1 for double.
	 * @return data rank in int type
	 */
	public int getDataRank();
	
	/**
	 * A method to get signal id.
	 * @return id in int type
	 */
	public int getID();
	
	/**
	 * A method to get result data from the data signal. Usually called after an
	 * algorithm is applied on the data.
	 * @return generic signal in Object type
	 */
	public Object getResultData();
	
	/**
	 * A method to set the result data to the signal pack. Usually called by a sink 
	 * to export the result to the signal pack. 
	 * @param signalResult
	 */
	public void setResultData(Object signalResult);
	
	/**
	 * A method to get the 2-theta0 value of the detector.
	 * @return 2-theta0 value in double type
	 */
	public double getTwoTheta0();
}
