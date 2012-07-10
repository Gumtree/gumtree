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
package au.gov.ansto.bragg.cicada.core;

import java.util.List;

import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.cicada.core.exception.ConfigurationException;
import au.gov.ansto.bragg.cicada.core.exception.LoadAlgorithmFileFailedException;
import au.gov.ansto.bragg.cicada.core.internal.Algorithm_;
import au.gov.ansto.bragg.process.common.Common_;

public class AlgorithmInput extends Common_ {
	
	/**
	 * AlgorithmInput object is a wrap of databag signal and the algorithm
	 * loaded on the signal. Load an AlgorithmInput object in the algorithm manager
	 * instead of a raw databag signal to keep a shallow history of algorithm applied 
	 * on the signal.  
	 * <p> 
	 * 
	 * Created on 13/05/2007, 9:58:24 AM
	 * Last modified 21/08/2007, 9:58:24 AM
	 * 
	 * @author nxi
	 * @since V2.0
	 */
	private static final long serialVersionUID = 1L;
	IGroup inputSignal = null;
//	String dataName = null;
	Algorithm algorithm = null;
	
	public AlgorithmInput(){
		
	}
	
	/**
	 * Constructor with input signal filed provided.
	 * @param inputSignal databag in the type of GroupData
	 */
	public AlgorithmInput(IGroup inputSignal) {
		this();
		this.inputSignal = inputSignal;
	}

	/**
	 * Constructor with input signal filed provided.
	 * @param inputSignal databag in the type of GroupData
	 * @param algorithm algorithm instance
	 */
	public AlgorithmInput(IGroup inputSignal, Algorithm algorithm) {
		this();
		this.inputSignal = inputSignal;
		this.algorithm = algorithm;
		algorithm.setCurrentSignal(this);
	}
	
	/**
	 * Get the databag property.
	 * @return the databag
	 */
	public IGroup getDatabag() {
		return inputSignal;
	}

	/**
	 * Set the databag property. 
	 * @param databag the databag to set
	 */
	public void setDatabag(IGroup databag) {
		this.inputSignal = databag;
	}

	/**
	 * @return the dataName
	 */
//	public String getDataName() {
//		return dataName;
//	}

	/**
	 * @param dataName the dataName to set
	 */
//	public void setDataName(String dataName) {
//		this.dataName = dataName;
//	}

	/**
	 * Get the algorithm property.
	 * @return the algorithm instance.
	 */
	public Algorithm getAlgorithm() {
		return algorithm;
	}

	/**
	 * Set the algorithm property.
	 * @param algorithm the algorithm to set
	 */
	public void setAlgorithm(Algorithm algorithm) {
		this.algorithm = algorithm;
	}
	
	/**
	 * Load an algorithm from a given path to an algorithm recipe file. The algorithm 
	 * input object can be processed if the input signal field and the algorithm filed are set.
	 * @param algorithmFilename
	 * @throws LoadAlgorithmFileFailedException
	 */
	public void loadAlgorithm(String algorithmFilename) throws LoadAlgorithmFileFailedException{ 
		Algorithm algorithm = new Algorithm_(this);
		algorithm.loadAlgorithm(algorithmFilename);
		this.algorithm = algorithm;
	}
	
	/**
	 * Load an algorithm from the available algorithm list for this algorithm input. The algorithm 
	 * input object can be processed if the input signal field and the algorithm filed are set.
	 *  
	 * @param availableAlgorithm an Algorithm object from the available algorithm list
	 * @throws LoadAlgorithmFileFailedException failed to load the algorithm
	 * @throws ConfigurationException 
	 */
	public void loadAlgorithm(Algorithm availableAlgorithm) 
	throws LoadAlgorithmFileFailedException, ConfigurationException{ 
		loadAlgorithm(availableAlgorithm.getFilename());
		AlgorithmConfiguration configuration = availableAlgorithm.getConfiguration();
		if (configuration != null)
			algorithm.loadConfiguration(configuration);
	}
	
	/**
	 * Call this method to dispose an Algorithm object. This method will release the algorithm object 
	 * handle. 
	 */
	public void unloadAlgorithm(){
		algorithm.dispose();
		algorithm = null;
	}
	
	/**
	 * Override toString() method of parent class.
	 */
	public String toString(){
		String result = "";
		result += "<algorithm_input signal=\"";
		if (inputSignal != null) result += inputSignal.getShortName();
		else result += "null";
		result += "\" algorithm=\"";
		if (algorithm != null) result += algorithm.getName();
		else result += "null";
		result += "\" />";
		return result;
	}
	
	/**
	 * Check if the algorithm input is ready for process. Which means the signal and algorithm 
	 * are loaded.
	 * @return true or false
	 */
	public boolean isReady(){
		if (inputSignal == null) return false;
		if (algorithm == null) return false;
		if (algorithm.isEmpty()) return false;
		return true;
	}
	
	public void subscribeExceptionHandler(ThreadExceptionHandler handler) {
//		this.exceptionCatcher = catcher;
		algorithm.subscribeExceptionHandler(handler);
	}

	public List<ThreadExceptionHandler> getExceptionHandlerList() {
		return algorithm.getExceptionHandlerList();
	}

	public void unSubscribeExceptionHandler(ThreadExceptionHandler handler) {
		algorithm.unSubscribeExceptionHandler(handler);
	}

	public void dispose() {
		inputSignal = null;
		if (algorithm != null){
			algorithm.dispose();
			algorithm = null;
		}
	}
}
