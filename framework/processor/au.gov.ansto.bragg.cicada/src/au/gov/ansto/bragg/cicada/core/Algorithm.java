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

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.cicada.core.AlgorithmConfiguration.AlgorithmType;
import au.gov.ansto.bragg.cicada.core.exception.ConfigurationException;
import au.gov.ansto.bragg.cicada.core.exception.ExportException;
import au.gov.ansto.bragg.cicada.core.exception.LoadAlgorithmFileFailedException;
import au.gov.ansto.bragg.cicada.core.exception.SetTunerException;
import au.gov.ansto.bragg.cicada.core.exception.TransferFailedException;
import au.gov.ansto.bragg.process.agent.Agent;
import au.gov.ansto.bragg.process.agent.ProcessorAgent;
import au.gov.ansto.bragg.process.common.Common;
import au.gov.ansto.bragg.process.port.Port;
import au.gov.ansto.bragg.process.port.Tuner;
import au.gov.ansto.bragg.process.processor.Sink;
/**
 * Algorithm is the interface for cicada algorithm. A cicada algorithm is a java 
 * Class that are built upon a recipe xml file. It create and manage a framework
 * that contains multiple processors. When initialized, it builds concrete processors
 * to process input data. The algorithm object will expose tuners, agents and sinks
 * to upper level applications.
 * <p>  
 * @see au.gov.ansto.bragg.process.port.Tuner
 * @see au.gov.ansto.bragg.process.agent.Agent
 * @see au.gov.ansto.bragg.process.processor.Sink
 * <p>  
 * 
 * Created on 20/02/2007, 9:58:24 AM
 * Last modified 20/12/2007, 9:58:24 AM
 * @author nxi
 * @version V2.6
 * @since V1.0
 */

public interface Algorithm extends Common {

	public final static String DEFAULT_NAME = "algorithm";
	public enum AlgorithmStatus{Idle, Running, Interrupt, Error, End};

	/**
	 * This method get the agent list of this algorithm.
	 * @return  list of agents
	 * @since V1.0
	 */
	public List<Agent> getAgentList();
	
	/**
	 * Get the current loaded signal of this algorithm.
	 * @return  generic signal type as Object
	 * @since V1.0
	 */
	public Object getCurrentSignal();
	
	/**
	 * Return the algorithm id specified in the recipe file. 
	 * @return Class id in String type.
	 */
	public String getClassID();
	
	/**
	 * This method retrieve the URL of the help file location.
	 * @return help file location in URL type
	 */
	public String getHelpURL();

	/**
	 * This method get the version of the algorithm recipe file.
	 * @return version number in String type
	 */
	public String getVersion();

	/**
	 * Get the input signal list of this algorithm
	 * @return the generic signal list 
	 * @since V1.0
	 */
//	public List<Object> getCurrentSignalList();
	
	/**
	 * This method returns the full path and name of the algorithm recipe file.
	 * @return full path in String type
	 * @since V1.0
	 */
	public String getFilename();
	
	/*
	 * (non-Javadoc)
	 * @see au.gov.ansto.bragg.process.common.Common#getID()
	 * Override get id;
	 *
	public int getReceipeID();
	*/
	
	/**
	 * Get icon path of the algorithm.
	 * @return the path of the icon used by the algorithm
	 * @since V1.0
	 */
	public String getIcon();
	
	/**
	 * Get short description attribute of the algorithm. The short description reflects 
	 * the function of the algorithm.
	 * @return  short description as String
	 * @since V1.0
	 */
	public String getShortDescription();
	
	/**
	 * This method gets the sink list of this algorithm. A sink is a special processor
	 * of the algorithm framework. A sink will notify its subscriber if its signal
	 * is changed.
	 * @return  list of sinks
	 * @since V1.0
	 */
	public List<Sink> getSinkList();
	
	/**
	 * This method gets the tuner list of the algorithm. A tuner is a proxy of a VAR
	 * port. It can view and change the VAR port signal and status. A tuner is both 
	 * observable and controllable.
	 * @return  list of tuners
	 * @since V1.0
	 */
	public List<Tuner> getTunerArray();
	
	/**
	 * This method returns a list of tuner list. Every processor of the algorithm 
	 * has a list of tuners. Put all these list handles to a new list and return
	 * it.
	 * @return list of tuner list
	 * @since V1.0
	 */
	public List<List<Tuner>> getTunerGroupList();
	
	/**
	 * Check if the algorithm is hidden algorithm. 
	 * @return hidden property in boolean type
	 * @since V2.4
	 */
	public boolean isHidden();
	
	/**
	 * This method builds an algorithm from a given recipe xml filename. The algorithm 
	 * built will have empty processor framework and is ready to take data.  
	 * @param filename  as instance of File class
	 * @throws LoadAlgorithmFileFailedException  failed to load the algorithm recipe file
	 * @since V1.0
	 */
	public void loadAlgorithm(File filename) throws LoadAlgorithmFileFailedException;
	
	/**
	 * Process the algorithm framework with multiple source signal. This method will 
	 * feed every input signal into the IN ports of the processor framework sequencially.
	 * Which will finally activate the processing of the processor framework.
	 * @throws NullPointerException
	 * @throws NullMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 * @since V1.0
	 *
	public void multiSignalTransfer() throws NullPointerException, NullMethodException,
	IllegalAccessException, InvocationTargetException, InstantiationException;
	*/
	
	/**
	 * This method builds an algorithm from a given recipe file path.
	 * @see Algorithm#loadAlgorithm(File)
	 * @param fileLocation  as String
	 * @throws LoadAlgorithmFileFailedException failed to load the algorithm recipe file
	 * @since V1.0
	 */
	public void loadAlgorithm(String fileLocation) throws LoadAlgorithmFileFailedException;
	
	/**
	 * Load signal to current signal pointer.
	 * Replaced with setCurrentSignal(signal).
	 * 
	public void loadSignal(Object signal);
	*/
	
	/**
	 * This method will load current signal handle to a generic signal object.
	 * @param signal  generic signal type in Object
	 * @since V1.0
	 *
	public void setCurrentSignal(Object signal);
	*/
	
	/**
	 * This method builds an algorithm from a given URL of recipe xml file.
	 * @see Algorithm#loadAlgorithm(File)
	 * @param fileLocation  as URL path
	 * @throws LoadAlgorithmFileFailedException failed to load the algorithm recipe file
	 * @since V1.0
	 */
	public void loadAlgorithm(URL fileLocation) throws LoadAlgorithmFileFailedException;
	
	/**
	 * Set recipe id for the algorithm.
	 *
	public void setReceipeID(int receipeID);
	*/
	
	/**
	 * Reset the running flag to false.
	 * @since V1.0
	 */
	public void resetRunningFlag();
	
	/*
	 * Pass values to a specific port.
	 *
	public void setVarPort(Port port, Object value);
	*/
	
	/**
	 * @Only used for old test program.
	 *
	public void setVarPort(int portID, Object value);
	*/
	
	/**
	 * Set the filename string to all the filename tuner.
	 * @param filename  as String
	 * @throws SetTunerException  failed to set value to the tuner
	 * @since V1.0
	 */
	public void setFilenameTuner(String filename) throws SetTunerException;

	/**
	 * This method set the running flag to true. This method can is set to be public so that it
	 * can be called from outside. When a processor is triggered to be running, call this method
	 *  
	 * @since V2.0
	 */
	public void setRunningFlag();
	
	/**
	 * Process the algorithm. This will process the concrete algorithm processor chain by triger it with 
	 * the input ports.
	 * @throws TransferFailedException failed to process the algorithm 
	 * @since V1.0
	 */
	public void transfer() throws TransferFailedException;
	
	/**
	 * Make a clone of this algorithm. This will return an new algorithm with identical 
	 * processor chains. Also, the tuners that parameterize the processor chains 
	 * have the identical value with the cloned tuners.
	 * @return cloned algorithm
	 * @since V2.6
	 */
	public Algorithm clone();
	
	/**
	 * Return true if the algorithm is only a structure algorithm, which does not
	 * have a processor framework loaded.
	 * @return true or false
	 * @since V2.6
	 */
	public boolean isEmpty();
	
	/**
	 * Set the algorithm input field.
	 * @param currentSignal in the type of AlgorithmInput.
	 * @since V2.0
	 */
	public void setCurrentSignal(AlgorithmInput currentSignal);
	
	/**
	 * Set all the tuners in the tuner list.
	 * @throws SetTunerException failed to set tuners
	 */
	public void setUnchangedTuners() throws SetTunerException;
	
	/**
	 * Subscribe a thread exception handler to the algorithm manager.  
	 * @param handler
	 * @since V2.8
	 */
	public void subscribeExceptionHandler(ThreadExceptionHandler handler);
	
	/**
	 * Get the thread exception handler list from the algorithm manager.
	 * @return list of exception handlers
	 * @since V2.8
	 */
	public List<ThreadExceptionHandler> getExceptionHandlerList() ;
	
	/**
	 * Unsubscribe a thread exception handler to the algorithm manager. 
	 * @param handler
	 * @since V2.8
	 */
	public void unSubscribeExceptionHandler(ThreadExceptionHandler handler);
	
	/**
	 * This method do the routine when a transfer exception is thrown. 
	 * @param algorithm object
	 * @param e Exception instance
	 * @since V2.0 
	 */
	public void catchTransferException(Exception e);
	
	/**
	 * Return the input port of the processor framework. The processing of 
	 * the processor chain is triggered by set a signal to this input port. 
	 * @return input port in Port type
	 * Created on 04/04/2008
	 */
	public Port getCurrentSourcePort();
//	public void resetTunerChangeFlag();
	
	/**
	 * Check if the algorithm needs any input to trigger the processing. 
	 * If there are input ports listed in the recipe, it needs input. Otherwise return false.
	 * @return boolean value
	 * Created on 07/04/2008
	 */
	public boolean hasInPort();
	
	public void exportConfiguration(URI fileURI, String configurationName) 
	throws ExportException, ConfigurationException;

	public void exportPartialConfiguration(URI fileURI, String configurationName, String... tunerNames) 
	throws ExportException, ConfigurationException;
	
	/**
	 * Get the default sink name
	 * @return String type
	 * Created on 28/04/2008
	 */
	public String getDefaultSinkName();
	
	public void setDefaultSinkName(String sinkName);
	
	public void loadConfiguration(URI fileURI) throws LoadAlgorithmFileFailedException, ConfigurationException;

	public void loadConfiguration(AlgorithmConfiguration configuration) throws ConfigurationException;

	public AlgorithmConfiguration getConfiguration() throws ConfigurationException;
	
	public boolean isRunning();

	public void setConfigurationGroup(IGroup configuration) throws ConfigurationException;

	public void interrupt();

	public void execute(AlgorithmManager manager);

	public void addStatusListener(AlgorithmStatusListener listener);
	
	public void removeStatusListener(AlgorithmStatusListener listener);
	
	public AlgorithmType getAlgorithmType();
	
	public Object getDefaultAlgorithmResult() throws SignalNotAvailableException;
	
	public Tuner findTuner(String tunerName);

	public void dispose();
	
	public List<ProcessorAgent> getProcessorAgentList();
	
	public Sink getSink(String sinkName);
	
	public Tuner getTuner(String tunerName);
	
	public AlgorithmStatus getAlgorithmStatus();
}
