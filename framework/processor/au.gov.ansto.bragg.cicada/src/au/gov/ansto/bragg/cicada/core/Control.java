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

import java.io.IOException;
import java.net.URI;
import java.util.List;

import au.gov.ansto.bragg.cicada.core.exception.ExportException;
import au.gov.ansto.bragg.cicada.core.exception.NoneAlgorithmException;
import au.gov.ansto.bragg.cicada.core.exception.SetTunerException;
import au.gov.ansto.bragg.cicada.core.exception.TransferFailedException;
import au.gov.ansto.bragg.process.agent.Agent;

/**
 * The control API for algorithm manager
 * <p>
 * Created on 20/02/2007, 9:58:24 AM
 * Last modified 17/04/2007, 9:58:24 AM
 * @see au.gov.ansto.bragg.cicada.core.AlgorithmManager
 *
 * @author jgw + nxi
 * @version V2.0
 * @since V1.0
 */
public interface Control {

	/**
	 * Garbage collection routine called when the cicada perspective is disposed
	 * @since V1.0
	 */
	public void dispose();
	
	/**
	 * This method do the routine when the input signal is disposed. It will 
	 * release the algorithm that was loaded on the signal.
	 */
	public void disposeData();

	/**
	 * Execute the current loaded algorithm. User call this method after load a specific 
	 * algorithm recipe file upon the current data signal, and setup the tuner.
	 * @since V1.0
	 * @throws NoneAlgorithmException  no loaded algorithm
	 */
	public void execute() throws NoneAlgorithmException;
	
	/** 
	 * Execute an give algorithm (call process() method of the concrete processor).
	 * @param algorithm  loaded algorithm upon a data signal
	 * @throws NoneAlgorithmException  the algorithm is not loaded, or no data signal to work with
	 * @since V1.0
	 * Undeprecated  since V2.9
	 */
	public void execute(Algorithm algorithm) throws NoneAlgorithmException;
	
	/**
	 * Get a tuner handle by its tuner ID from the current loaded algorithm
	 * @param tunerID  as int type
	 * @return  the tuner handle
	 * @throws IndexOutOfBoundException  the tuner id is invalid
	 * @throws NoneAlgorithmException  no loaded algorithm
	 * @deprecated since V1.0
	 * @removed since V2.0
	public Tuner getTuner(int tunerID) throws IndexOutOfBoundException, NoneAlgorithmException;
	*/
	
	/**
	 * This method makes the current loaded algorithm start processing from the
	 * specified processor in the processor chain. The starting processor is referred
	 * by a given agent. A prerequisite of this method is the starting processor 
	 * has the input ports set up before this method is called. 
	 * @param agent a processor agent, which gives a view of the referred processor.
	 * @throws TransferFailedException the processing of the algorithm failed
	 * @since V2.4
	 */
	public void executeFrom(Agent agent) throws TransferFailedException;
	
	/**
	 * Halt a running algorithm. 
	 * @param algorithm  loaded Algorithm
	 * @since V1.0
	 */
	public void halt(Algorithm algorithm);
	
	/**
	 * Pause all running algorithms.
	 * @deprecated for M1
	 * @since V1.0 
	 */
	public void haltAll();
	
	/**
	 * Reload an available algorithm to the current algorithm handle
	 * @param algorithm  loaded algorithm
	 * @since V1.0
	 * @deprecated for M1
	 */
	public void reload(Algorithm algorithm);
	
	/**
	 * Save the loaded algorithm to a certain file. This method will save all the information
	 * of a data signal, for example, the algorithm applied on it and the result
	 * @param algorithm loaded algorithm
	 * @param algorithmFilename  as String
	 * @since V1.0
	 * @deprecated for M1
	 * @removed since V2.0
	public void save(Algorithm algorithm, File algorithmFilename);
	*/
	
	/**
	 * Set the tuner with a given signal argument. Make sure the tuner can take the 
	 * correct signal type.
	 * @param signal  generic signal type as Object, but has to match tuner type
	 * @param tuner  tuner handle
	 * @throws NullMethodException  fail to initialize the tuner constructor
	 * @throws IllegalAccessException  
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 * @since V1.0
	 * @removed since V2.0
	public void setTunnerValue(Object signal, Tuner tuner) throws NullMethodException,
	IllegalAccessException, InvocationTargetException, InstantiationException;
	*/
	
	/**
	 * Reset a loaded algorithm, clear all tuner port and sink processor port
	 * @param algorithm  loaded algorithm
	 * @since V1.0
	 */
	public void reset(Algorithm algorithm);
	
	/**
	 * Reset all loaded algorithms. Release port information, set processors and ports 
	 * to empty status.
	 * @since V1.0 
	 */
	public void resetAll();
	
	/**
	 * Export the result signal of an instrument signal (type SingalType) to a certain file
	 * @param filename full path of the file as String
	 * @throws ExportException  failed to export data
	 * @since V1.0
	 */
	public void resultExport(URI fileURI) throws ExportException;

	/**
	 * Export any algorithm input/output signal to a certain file
	 * @param filename full path of the filename in String type
	 * @param signal  as Object, can be either a GroupData, Variable, Array or primary 
	 * array with at most 3 dimensions. 
	 * @throws IOException  illegal access to the file system
	 * @throws ExportException  failed to export data
	 * @since V1.0
	 */
	public void signalExport(URI fileURI, Object signal) 
	throws ExportException ;

	/**
	 * Export any algorithm input/output signal to a certain file
	 * @param filename full path of the filename in String type
	 * @param signal  as Object, can be either a GroupData, Variable, Array or primary 
	 * array with at most 3 dimensions. 
	 * 
	 * @param transpose  flag of transposing the matrix
	 * @throws ExportException  failed to export data
	 * @since V1.2
	 */
	public void signalExport(URI fileURI, Object signal, boolean transpose) 
	throws ExportException ;

	/**
	 * Export any double array signal (dimension from 0 to 3) to a certain file
	 * @param filename full path of the filename in String type
	 * @param signal  as Object, can be either a GroupData, Variable, Array or primary 
	 * array with at most 3 dimensions. 
	 * @param title name of the signal as String
	 * 
	 * @throws ExportException  failed to export data
	 * @since V2.0
	 */
	public void signalExport(URI fileURI, Object signal, String title) 
	throws ExportException ;
	
	/**
	 * Set a tuner that shared by a list of algorithms. The tuners should
	 * have the same name as specified by the parameter.  
	 * @param algorithmInputs list of AlgorithmInput object
	 * @param tunerName in String type
	 * @param value any type of object that can be accepted by the tuners
	 * @throws SetTunerException 
	 */
	public void setTunerOfAlgorithms(List<AlgorithmInput> algorithmInputs, 
			String tunerName, Object value) throws SetTunerException;

	public void exportAlgorithmConfiguration(Algorithm algorithm, URI fileURI, 
			String configurationName) throws ExportException;


}
