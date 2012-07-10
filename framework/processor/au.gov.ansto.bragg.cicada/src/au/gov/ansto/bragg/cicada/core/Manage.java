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

import java.net.URI;
import java.util.List;

import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.cicada.core.exception.ConfigurationException;
import au.gov.ansto.bragg.cicada.core.exception.ExportException;
import au.gov.ansto.bragg.cicada.core.exception.IllegalFileFormatException;
import au.gov.ansto.bragg.cicada.core.exception.LoadAlgorithmFileFailedException;
import au.gov.ansto.bragg.cicada.core.extension.AlgorithmSet;

/**
 * The manager API for algorithm manager
 * <p>
 * Created on 20/02/2007, 9:58:24 AM
 * Last modified 17/04/2007, 9:58:24 AM
 * @see au.gov.ansto.bragg.cicada.core.AlgorithmManager
 *
 * @author jgw + nxi
 * @version V2.0
 * @since V1.0
 */
public interface Manage {

	/**
	 * Add an exporter instance with a given format name
	 * @param formatName  in String, for example, 'text', 'xml'
	 * @return extension name as String
	 * @throws IllegalArgumentException  can't generate enum from the format name parameter
	 * @since V1.0 
	 * @see au.gov.ansto.bragg.cicada.core.Format
	 */
	public String addExporter(String formatName) 
	throws ExportException;
	
	/**
	 * Add the thread to the thread list of the algorithm manager.
	 * @param thread algorithm thread instance
	 */
	public void addThread(Thread thread);
	
	/**
	 * Routine when TransferException of a running algorithm is caught
	 * @param algorithm in Algorithm type
	 * @param e Exception instance
	 * @since V2.0
	 */
	public void catchTransferException(Algorithm algorithm, Exception e);

	/**
	 * Get the array of all available algorithm structures. Algorithm structure is a
	 * dry algorithm without having any data, that is empty processors, empty ports. 
	 * @return  array of algorithm handles
	 * @since V1.0 
	 */
	public Algorithm[] getAvailableAlgorithmList();

	/**
	 * Switch the current algorithm handle to another loaded algorithm.
	 * @param algorithm  algorithm handle
	 * @return  algorithm handle
	 * @since V1.0 
	 * @deprecated for M1
	 * @removed since V2.0
	public Algorithm switchCurrentAlgorithm(Algorithm algorithm);
	*/

	/**
	 * Get a loaded algorithm handle from the loaded algorithm list.
	 * @param algorithmID  as int type
	 * @return  algorithm handle
	 * @since V1.0 
	 * @deprecated for M1
	 * @removed since V2.0
	public Algorithm getLoadedAlgorithm(int algorithmID) throws IndexOutOfBoundException;
	*/

	/**
	 * Get a running algorithm handle from the running algorithm list.
	 * @param algorithmID  as int type
	 * @return  algorithm handle
	 * @since V1.0 
	 * @deprecated for M1
	 * @removed since V2.0
	public Algorithm getRunningAlgorithm(int algorithmID) throws IndexOutOfBoundException ;
	*/

	/**
	 * This method will get the loaded algorithm list of the algorithm manager. If an 
	 * algorithm is loaded upon some data signal, it will be put in the loaded algorithm list.
	 * If this data signal changes algorithm (by loading a new algorithm upon it), this 
	 * algorithm will be removed from the loaded algorithm list and lose reference, so that 
	 * it is subject to garbage collection.
	 * @return  array of algorithm handles
	 * @since V1.0 
	 */
	public Algorithm[] getLoadedAlgorithmList();

	/**
	 * This method will get the running algorithm list of the algorithm manager. When an
	 * algorithm is executed, it will be put in the running algorithm list. After its 
	 * processing is done, it will be removed from the running algorithm list. 
	 * @return  List of algorithm handles
	 * @since V1.0 
	 */
	public List<Algorithm> getRunningAlgorithmList();

	/**
	 * Load an algorithm from a give available algorithm structure. 
	 * Algorithm structure is a dry algorithm without having any data, 
	 * that is empty processors, empty ports.
	 * @param availableAlgorithm in Algorithm type
	 * @return  handle of the algorithm just loaded
	 * @throws LoadAlgorithmFileFailedException  failed to load algorithm from recipe file
	 * @throws ConfigurationException 
	 * @since V1.0 
	 * @see au.gov.ansto.bragg.cicada.core.Manage#getAvailableAlgorithmList()
	 */
	public Algorithm loadAlgorithm(Algorithm availableAlgorithm) 
	throws LoadAlgorithmFileFailedException, ConfigurationException;
	
	/**
	 * Load an algorithm structure from a given recipe xml file. This algorithm will be
	 * set to current algorithm handle of the algorithm manager. 
	 * @param algorithmFilename  as String
	 * @throws LoadAlgorithmFileFailedException  failed to load algorithm from recipe file
	 * @since V1.0 
	 */
	public void loadAlgorithmFile(String algorithmFilename) 
	throws LoadAlgorithmFileFailedException;
	

	/**
	 * Load an algorithm as a protocol to apply on multiple inputs.
	 * @param availableAlgorithm algorithm structure from the available algorithm list.
	 * @return initialized algorithm instance
	 * @throws LoadAlgorithmFileFailedException failed to load the algorithm
	 */
	public Algorithm loadProtocol(Algorithm availableAlgorithm) 
	throws LoadAlgorithmFileFailedException;
	
	/**
	 * Remove the thread from the thread list of the algorithm manager.
	 * @param thread algorithm thread instance
	 */
	public void removeThread(Thread thread);
	
	/**
	 * This method load existing algorithm input into the current signal field of the algorithm
	 * manager.
	 * @param signal  databag signal in  type
	 * @since V2.0
	 * @see au.gov.ansto.bragg.cicada.core.AlgorithmManager#getCurrentInput()
	 */
	public void setCurrentInput(AlgorithmInput input);
	
	/**
	 * This method create a new algorithm input with given databag, and set it
	 * to the current input field of the algorithm manager.
	 * @param databag  databag signal in GroupData type
	 * @since V1.0
	 * @see au.gov.ansto.bragg.cicada.core.AlgorithmManager#getCurrentInput()
	 */
	public void setCurrentInputData(IGroup databag);
	
	/**
	 * Unload a loaded Algorithm from the loaded Algorithm list.
	 * @param algorithm algorithm handle
	 * @since V1.0 
	 */
	public void unloadAlgorithm(Algorithm algorithm);

	/**
	 * Load algorithms in an algorithm set. 
	 * @param algorithmSet
	 * @throws LoadAlgorithmFileFailedException
	 * @throws FileAccessException
	 */
	public void switchToAlgorithmSet(AlgorithmSet algorithmSet) 
	throws LoadAlgorithmFileFailedException;
	
	/**
	 * Get the exporter by format name. 
	 * @param format enum value
	 * @return Exporter object
	 * @throws ExportException
	 * Created on 08/04/2009
	 */
	public Exporter getExporter(Format format) throws ExportException;
	
	/**
	 * Create a DRA task in the memory. It exists as a GDM object.
	 * @param taskName in String type
	 * @param algorithm an Algorithm object
	 * @return DRATask object
	 * @throws ConfigurationException
	 * Created on 08/04/2009
	 */
	public DRATask createDRATask(String taskName, Algorithm algorithm) throws ConfigurationException;
	
	/**
	 * Load a pre-saved DRA task to the algorithm manager
	 * @param uri a URI object
	 * @return DRATask object
	 * @throws IllegalFileFormatException
	 * Created on 08/04/2009
	 */
	public DRATask loadDRATask(URI uri) throws IllegalFileFormatException ;
}
