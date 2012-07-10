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
import java.util.Map;

import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.cicada.core.exception.LoadAlgorithmFileFailedException;
import au.gov.ansto.bragg.cicada.core.exception.NoneAlgorithmException;
import au.gov.ansto.bragg.cicada.core.exception.NoneAttributeException;
import au.gov.ansto.bragg.cicada.core.extension.AlgorithmSet;
import au.gov.ansto.bragg.process.agent.Agent;
import au.gov.ansto.bragg.process.exception.IndexOutOfBoundException;
import au.gov.ansto.bragg.process.exception.NullSignalException;
import au.gov.ansto.bragg.process.port.Tuner;

/**
 * The query API for algorithm manager
 * <p>
 * Created on 20/02/2007, 9:58:24 AM
 * Last modified 18/06/2007, 9:58:24 AM
 * @see au.gov.ansto.bragg.cicada.core.AlgorithmManager
 *
 * @author jgw + nxi
 * @version V2.4
 * @since V1.0
 */
public interface Query {
	
	/**
	 * This method find the algorithm that contains the given agent object. Use this method
	 * if the client of the algorithm manager has only the agent handle and look for the algorith
	 * handle.
	 * @param agent has to be a processor agent
	 * @return algorithm handle
	 * @since V2.4
	 */
	public Algorithm findAlgorithmWithAgent(Agent agent);
	
	/**
	 * Get the agent handle from an agent id in the agent list of the current algorithm.
	 * @param agentID  as int type
	 * @return  agent handle
	 * @throws IndexOutOfBoundException  can't locate the agent id in the agent list
	 * @throws NoneAlgorithmException  no current algorithm loaded
	 * @since V1.0
	 * @deprecated since M1
	 *
	public Agent getAgent(int agentID) throws IndexOutOfBoundException, NoneAlgorithmException;
	 */
	
	/**
	 * Get a agent handle by its id from the agent list of an algorithm.
	 * @param algorithm  algorithm handle
	 * @param agentID  as int type
	 * @return  agent handle
	 * @throws IndexOutOfBoundException  can't locate the agent id in the agent list of 
	 * the given algorithm 
	 * @throws NoneAlgorithmException  invalid algorithm handle
	 * @since V1.0
	 */
	public Agent getAgent(Algorithm algorithm, int agentID);
	
	/**
	 * Get the list of all agents of the current algorithm of the algorithm manager.
	 * Agents are proxies of ports and processors. User can use them to check status 
	 * of ports and processors. Not all the ports and processors have agents. The 
	 * agents are specified in the algorithm recipe xml files. Agents are observable
	 * but not controllable except for being set as the start point of the algorithm 
	 * chain. 
	 * @return  list of agents
	 * @since V1.0 
	 * @throws NoneAlgorithmException  no current algorithm found
	 */
	public List<Agent> getAgentList() throws NoneAlgorithmException;
	
	/**
	 * Get the agent list of a loaded algorithm.
	 * @param algorithm  algorithm handle
	 * @since V1.0 
	 * @see au.gov.ansto.bragg.cicada.core.Query#getAgentList()
	 */
	public List<Agent> getAgentList(Algorithm algorithm);
	
	/**
	 * This method returns the algorithm set path as a String object. 
	 * The algorithm set folder path is in algorithm plugin folder. The algorithm
	 * manager has to know this to load the algorithm recipe files. In stand alone
	 * cicada application, this is necessary since it is independent from the workbench.
	 *  
	 * @return algorithm set full path in String type
	 * @since V2.0
	 * @see au.gov.ansto.bragg.cicada.core.AlgorithmManager#loadDirectory(String directoryName)
	 */
	public String getAlgorithmSetPath();

	/**
	 * This method returns a list of algorithms that are not hidden from the algorithm
	 * set. 
	 * Available algorithm is an algorithm structure loaded from a recipe file in the file system.
	 * This method will return an array that contains all the exposed algorithm structures.
	 * 
	 * @return array of Algorithm instances
	 * @see au.gov.ansto.bragg.cicada.core.AlgorithmManager#loadDirectory(String directoryName)
	 * @since V1.0
	 */
	public Algorithm[] getAvailableAlgorithmList();
	
	/**
	 * This method returns the algorithm handle of the algorithm manager. 
	 * The current algorithm is loaded for a specific data signal. The algorithm
	 * manager can handle multiple data signal by putting them in a data signal 
	 * list. When a data signal is loaded in to currentSignal member, the algorithm 
	 * handle that applied to 
	 * the data signal is also loaded into current algorithm.
	 * If a new algorithm is loaded upon the current data, the current algorithm handle is
	 * also updated to pointing to this algorithm.
	 * 
	 * @return instance of Algorithm
	 * @see au.gov.ansto.bragg.cicada.core.AlgorithmManager#getAvailableAlgorithmList()
	 * @since V1.0
	 */
	public Algorithm getCurrentAlgorithm();

	/**
	 * Get the current loaded data signal (children class of SignalType).
	 * All data signals handled by the algorithm manager is stored in a list. If a data signal
	 * is picked up, the current signal handle will be updated to its pointer.
	 * @return algorithm input object in AlgorithmInput type
	 * @throws NullSignalException failed to load current signal or signal is null 
	 * @see au.gov.ansto.bragg.cicada.core.AlgorithmManager
	 * @since V1.0
	 */
	public AlgorithmInput getCurrentInput();

	/**
	 * Return the databag of the current input signal.
	 * 
	 * @return databag in GroupData type
	 * @since V2.0
	 */
	public IGroup getCurrentInputData();
	
	/**
	 * This method retrieve the default attributes of given databag. If the databag is a
	 * root group, it will retrieve the attributes of NXroot. If the databag is an entry
	 * or a subgroup of an entry, it will retrieve the attributes of the entry.
	 * @param databag in the type of GroupData
	 * @return Map object with key as String and value as Object
	 * @throws NoneAttributeException 
	 * @since V2.4
	 */
	public Map<String, Object> getDefaultAttributes(IGroup databag) throws NoneAttributeException ;

	/**
	 * This method retrieve all the entries in the root group of a databag. The entries of
	 * the databag have to have an attribute of NX_class, which has a value of NXentry.  
	 * @param databag in the type of GroupData
	 * @return a list of entries in the type of GroupData
	 * @since V2.4
	 */
	public List<IGroup> getEntryList(IGroup rootGroup);
	
	/**
	 * Get the exporter list of cicada algorithm manager. The exporter list is created
	 * in cicada initialization procedure. 
	 * @return list of exporter
	 * @see Exporter
	 * @since V2.0
	 */
	public List<Exporter> getExporterList();

	/**
	 * Get all the available format names from the enum type: format. 
	 * @return  format names as array of String
	 * @since V1.0
	 * @see au.gov.ansto.bragg.cicada.core.Format
	 */
	public String[] getExportFormat();
	
	/**
	 * Get the list of all tuners in the current loaded algorithm
	 * @return  array of tuner handles
	 * @throws NoneAlgorithmException  no loaded algorithm
	 * @since V1.0
	 */
	public Tuner[] getTunerList() throws NoneAlgorithmException;
	
	/**
	 * Get the list of the tuners of the given loaded algorithm.
	 * @param algorithm  loaded algorithm in Algorithm object
	 * @return array of tuner handles
	 * @since V1.0
	 */
	public Tuner[] getTunerList(Algorithm algorithm);
	
	/**
	 * Get isDispose status of the algorithm. If an algorithm loses reference, it is subjected
	 * to garbage collection by java VM and is set to be disposed.
	 * @since V1.0 
	 */
	public boolean isDisposed();
	
	/**
	 * Get the available algorithm set list. 
	 * @return list of algorithm set
	 * @see AlgorithmSet
	 * @since V2.7.0
	 */
	public List<AlgorithmSet> getAlgorithmSetList();
	
	/**
	 * This method returns a list of all algorithms that are available in the recipe files folder.
	 *  
	 * @return array of algorithms
	 * @since V2.8
	 */
	public Algorithm[] getAllAvailableAlgorithms();

	/**
	 * Query for the current algorithm set.
	 * @return AlgorithmSet object
	 * Created on 17/09/2008
	 */
	public AlgorithmSet getCurrentAlgorithmSet();
	
	/**
	 * Find an algorithm with a specified name in the current algorithm set.  
	 * @param algorithmName in String type
	 * @return Algorithm in the given name
	 * Created on 17/09/2008
	 */
	public Algorithm findAlgorithm(String algorithmName);
	
	/**
	 * Find an algorithm with a specified name in a given algorithm set. 
	 * @param algorithmSetName in String type
	 * @param algorithmName in String type
	 * @return Algorithm object
	 * @throws LoadAlgorithmFileFailedException
	 * Created on 17/09/2008
	 */
	public Algorithm findAlgorithm(String algorithmSetName, String algorithmName) 
	throws LoadAlgorithmFileFailedException;
	
	/**
	 * Return the array of algorithms that are experiment algorithms.
	 * @return
	 * Created on 17/09/2008
	 */
	public Algorithm[] getExperimentAlgorithms();
	
	/**
	 * Return the array of algorithms that are analysis algorithms.
	 * @return java array of Algorithm. 
	 * Created on 17/09/2008
	 */
	public Algorithm[] getAnalysisAlgorithms();
}
