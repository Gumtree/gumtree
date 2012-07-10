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

import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.cicada.core.exception.ConfigurationException;
import au.gov.ansto.bragg.cicada.core.exception.FailedToExecuteException;
import au.gov.ansto.bragg.cicada.core.exception.IllegalFileFormatException;
import au.gov.ansto.bragg.cicada.core.exception.LoadAlgorithmFileFailedException;
import au.gov.ansto.bragg.cicada.core.exception.NoneAlgorithmException;
import au.gov.ansto.bragg.process.common.Common;

/**
 * AlgorithmManager is the interface for cicada algorithm manager application interface.
 * This interface extends the other four interface of the algorithm manager, that is, 
 * Control, Manage, Query and Subscribe.
 * <p>
 * This interface integrated all the other four interfaces and add more abstract methods. 
 * User can use this interface to refer an algorithm manager object.
 * Algorithm manager plays an key controller role in cicada project. It has the functions of
 * building an algorithm framework, loading source data and making the processors processing.
 * It also has access to the status and result of all the processors. 
 * The run time life cycle of an algorithm manager object is the same as the cicada application.
 * <p> 
 * This is the interface for developing user to create and access everything of cicada.
 * <p>  
 * 
 * Created on 20/02/2007, 9:58:24 AM
 * Last modified 17/04/2007, 9:58:24 AM
 * @see au.gov.ansto.bragg.cicada.core.Control
 * @see au.gov.ansto.bragg.cicada.core.Manage
 * @see au.gov.ansto.bragg.cicada.core.Query
 * @see au.gov.ansto.bragg.cicada.core.Subscribe
 * @author nxi
 * @version V2.0
 * @since V1.0
 * 
 */
public interface AlgorithmManager extends Common, Manage, Control, Query, Subscribe {

	/**
	 * SignalType is the a String object read from a configuration file of the instrument 
	 * algorithm recipe folder. The algorithm manager
	 * will dynamically load the class from this string with a classloader.
	 * This method returns the signal class name string.
	 * 
	 * @see au.gov.ansto.bragg.cicada.core.AlgorithmManager#loadDirectory(String)
	 * @since V1.0
	 * @removed since V2.0
	 */
//	public String getSingalType();
//	public void runEchidna(Shell shell);
//	public void runEchidna();
//	public Algorithm loadAlgorithm(int algorithmID) throws Exception;

	/**
	 * This method takes an workspace path string as argument, read the cicada configuration 
	 * file in the path, and parse it. The configuration file specifies the available instrument 
	 * plugins, and the default instrument algorithms to load.
	 *   
	 * @param workspacePath  the path of the workspace as String
	 * @throws DocumentException  if no document is created
	 * @throws IllegalNameSetException  if null name is set
	 * @throws NoneAttributeException  if the attribute is not found in the recipe file
	 * @throws IOException  i/o failure
	 * 
	 * @see au.gov.ansto.bragg.cicada.core.AlgorithmManager#loadDirectory(String directoryName)
	 * @since V1.0
	 * @removed since V2.0
	public void loadConfiguration(String workspacePath) throws DocumentException, IllegalNameSetException, NoneAttributeException, IOException;
	*/

	/**
	 * Create a new empty algorithm in the algorithm list by calling this method. 
	 * 
	 * @throws IllegalNameSetException  null name is set
	 */
//	public void newEmptyAlgorithm() throws IllegalNameSetException;
	
	/**
	 * This method take no argument to load a configuration file.
	 * The file must be in the cicada project folder. This method will make
	 * the algorithm manager know which instrument plugin to load, and its relative 
	 * path.
	 * @throws ConfigurationException failed to load the configuration file
	 * @throws LoadAlgorithmFileFailedException failed to load algorithm from recipe file
	 * @since V1.2
	 * @reduced visibility since V2.0
	 *
	public void loadConfiguration() 
	throws ConfigurationException, LoadAlgorithmFileFailedException;
	*/

	/**
	 * Return the file that is opened to get the current signal
	 * 
	 * @return filename in String type
	 * @since V1.0 
	 */
//	public String getSignalFilename();

	/**
	 * This method execute multiple algorithms in different threads. The algorithm manager will iterate 
	 * the algorithm input list and execute each algorithm in a separate thread.
	 * @param inputList a list of algorithm inputs
	 * @throws FailedToExecuteException process one algorithm failed 
	 * @since V2.6
	 */
	void executeAll(List<AlgorithmInput> inputList) throws NoneAlgorithmException, FailedToExecuteException;
	
	/**
	 * Load an algorithm to a list of GroupData signals. Return a list of algorithm inputs,
	 * each of which has a field of GroupData signal and a field of copy of the same algorithm.
	 * @param dataList a list of signals in GroupData type
	 * @param algorithm an available Algorithm instance to be applied to multiple inputs
	 * @return a list of algorithm inputs
	 * @throws LoadAlgorithmFileFailedException failed to load the available algorithm
	 * @throws ConfigurationException 
	 * @since V2.6
	 */
	List<AlgorithmInput> loadAlgorithmOnMultipleData(List<IGroup> dataList, Algorithm algorithm) 
	throws LoadAlgorithmFileFailedException, ConfigurationException;

	/**
	 * This method load the databag from a full path of a file. It returns an object in
	 * GroupData type, which contains groups and variables information of the file. Storage
	 * data will no be load until the read method of the groups or variables is invoked.
	 * @param uri path of the group data
	 * @return databag in GroupData type 
	 * @throws IllegalFileFormatException failed to read the file format
	 * @since V2.0
	 */
	IGroup loadDataFromFile(URI uri) throws IllegalFileFormatException;

	/**
	 * This method load the databag from a IGOR ASCII file. It returns an object in
	 * Group type, which contains groups and variables information of the file. Storage
	 * data will no be load until the read method of the groups or variables is invoked.
	 * @param uri path of the group data
	 * @return databag in Group type 
	 * @throws IllegalFileFormatException 
	 *  	parse the ASCII file failed
	 * @since V2.5
	 */
	IGroup importIgorData(URI uri) throws IllegalFileFormatException ;

	/**
	 * This method load all recipe files in a given path. The algorithm manager will
	 * parse these recipe files and build simple algorithm structures for them. And these
	 * algorithm structures are called available algorithms. 
	 * 
	 * @param directoryName  file system path, in String.
	 * @throws LoadAlgorithmFileFailedException failed to load recipe file from the directory
	 * @since V1.0
	 * @see au.gov.ansto.bragg.cicada.core.Manage#getAvailableAlgorithmList()
	 */
	void loadDirectory(String directoryName) throws LoadAlgorithmFileFailedException;
	
}
