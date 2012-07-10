/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.core.api.cicada;

import java.util.List;

import au.gov.ansto.bragg.kakadu.core.data.DataItem;
import au.gov.ansto.bragg.kakadu.core.data.Operation;
import au.gov.ansto.bragg.kakadu.core.data.UIAlgorithm;

/**
 * The interface defines API for algorithm execution.
 * Method calls have to follow in the next order:
 * <li>load data set</li>
 * <li>load algorithm</li>
 * <li>rum algorithm</li>
 * 
 * @author Danil Klimontov (dak)
 */
public interface AlgorithmProcessor {
	/**
	 * Loads a DataItem as a source data. 
	 * @param dataItem source data set.
	 */
	void loadDataSet(DataItem dataItem); 
	
	/**
	 * Loads an Algorithm to be executed.
	 * @param algorithm algorithm for the processor.
	 */
	void loadAlgorithm(UIAlgorithm algorithm);
	
	/**
	 * Gets operation list of loaded algorithm.
	 * Method <@link #loadAlgorithm(UIAlgorithm)> must be used before
	 * calling of the method.
	 * @return list of operations or null if algorithm was not loaded.
	 */
	List<Operation> getOperations();
	
	/**
	 * Starts execution of loaded alogorithm.
	 */
	void runAlgorithm(); //execute
	
	/**
	 * Runs loaded algorithm form particular operation.
	 * If previous operation has not been executed before an exception will be throwed.
	 * @param operation an operation to start with.
	 */
	void runOperation(Operation operation);

}
