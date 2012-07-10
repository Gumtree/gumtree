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
package au.gov.ansto.bragg.process.processor;

import java.util.List;

import au.gov.ansto.bragg.process.agent.Agent;
import au.gov.ansto.bragg.process.agent.ProcessorAgent;
import au.gov.ansto.bragg.process.common.Common;
import au.gov.ansto.bragg.process.exception.ProcessFailedException;
import au.gov.ansto.bragg.process.exception.ProcessorChainException;
import au.gov.ansto.bragg.process.port.In;
import au.gov.ansto.bragg.process.port.Out;
import au.gov.ansto.bragg.process.port.Port;
import au.gov.ansto.bragg.process.port.TunerPortListener;
import au.gov.ansto.bragg.process.port.Var;
import au.gov.ansto.bragg.process.processor.Processor_.ProcessorStatus;
/**
 * Generic Processor class. Processor instance will do data processing with static library methods.  <p> A Processor instance is built from an algorithm recipe xml file. It will call a concrete processor to do a data processing. The In, Var and Out ports are built upon the processor to take and pass signals.  <p> Processor is the ancestor class for CompositeProcessor and Framework.  The processing procedure will be trigered if all the Var ports are set and all the In ports are ready for processing. After the processing, it will output the result signal to its Out ports and its Out ports are in charge of where the signal flows.   <p> Created on 20/02/2007, 9:58:24 AM <p> Last modified 17/04/2007, 9:58:24 AM
 * @author  nxi
 * @version  V1.0
 * @since  M1
 * @see CompositeProcessor
 * @see Framework
 * @see Source
 * @see  Sink
 */
public interface Processor extends Common {

	public static final String METHODNAME = "process";
	/**
	 * This method will give a handle of ports list to the processor. The list contains all the
	 * In, Out and Var ports of processor. 
	 * @param portArray  in List<Port> type
	 */
	public void addAllPorts(List<Port> portArray);

	/**
	 * This mehod will give a handle of processors list and a handle of ports list
	 * to the processor. The processors list
	 * contains all the sub processors that belongs to this processor. This method
	 * only applies on composite processors. 
	 * @param processorArray  as List<Processor> type
	 * @param portArray  
	 */
	public void addAllProcessors(List<Processor> processorArray, List<Port> portArray);

	/**
	 * Get the agent that represents this processor.
	 * @return agent object
	 */
	public Agent getAgent();

	/**
	 * Get the IN port list handle of the processor. The list contains all In ports
	 * that are built upon this processor.
	 * @return In port list in List<In> type 
	 */
	public List<In> getInList();

	/**
	 * Get the OUT port list handle of the processor. The list contains all Out ports
	 * that are built upon this processor.
	 * @return Out port list in List<Out> type 
	 */
	public List<Out> getOutList();

	/**
	 * Get the VAR port list handle of the processor. The list contains all Var ports
	 * that are built upon this processor.
	 * @return Var port list in List<Var> type 
	 */
	public Processor getParent();

	/**
	 * Get the recipe id of the processor. The recipe id is specified in the algorithm
	 * recipe file. Every processor has a unique recipe id in a recipe file.
	 * @return recipe id in int type
	 */
//	public int getRecipeID();

	/**
	 * Get the processor type of the processor. The processor type is the name of the 
	 * child processor that this processor is in the runtime.  
	 * For example, composite processor and source processor.
	 * @return a String object
	 */
	public String getProcessorType();

	/**
	 * Get the signal of the main Out port of the processor.
	 * The mail Out port is usually the first one in the Out port list of the processor.
	 * @return generic signal in Object type
	 * @throws NullPointerException
	 */
	public Object getSignal() throws NullPointerException;

	/**
	 * Get the list of sinks that catch the outputs of this processor. 
	 * @return a list of Sink objects
	 */
	public List<Sink> getSinkList();

	/**
	 * Get the status of the processor, that is, running or stop.
	 * @return running status in String object
	 */
	public ProcessorStatus getStatus();

	/**
	 * Get the Var port list of the processor.
	 * @return Var port list in List<Var> type
	 */
	public List<Var> getVarList();

	/**
	 * Take one token from the processor's in token pool.
	 * If all in tokens are taken, the processor will start processing.
	 * The number of token in the token pool of the processor is decided 
	 * by how many In ports of the processor. 
	 * @throws ProcessorChainException 
	 * @throws ProcessFailedException 
	 */
	public void inTokenSignalReady() throws ProcessFailedException, ProcessorChainException;

	/**
	 * Get the running status of the processor.
	 * @return running status as boolean type
	 */
	public boolean isRunning();
//	Port getLocked();

	/**
	 * This method is called by In ports of this processor, when the In port
	 * is ready. 
	 * When this method is called, the processor will check if all In singals are ready,
	 * by checking if the token pool is empty.
	 * If true, the processor will do processing by calling transfer().
	 * @param recipeID in int type
	 */
//	public void setRecipeID(int recipeID);
	
	/**
	 * Get the lock status of the processor.
	 * @return lock status in boolean type
	 */
	public boolean lockStatus();

	/**
	 * Set the agent filed of the processor.
	 * @param agent an Agent object, which has to be a processor agent.
	 */
	public void setAgent(ProcessorAgent agent); 

	/**
	 * Set a specific filed of the concrete processor. This will call the setter of 
	 * the concrete processor to set the according field. 
	 * @param fieldName in String type
	 * @param signal signal object in generic type
	 * @throws ProcessorChainException 
	 */
	public void setField(String fieldName, Object signal, Class<?> type) 
	throws ProcessorChainException;
	
	/**
	 * Lock/Unlock the processor by set the lock property of the processor.
	 * @param lockStatus in boolean type
	 */
	public void setLock(boolean lockStatus);

	/**
	 * A toString() method taking an integer parameter. The method only shows 
	 * the information till the level specified by the level parameter.
	 * @param level is the depth of the processor nest, in int type
	 * @return a String object
	 */
	public String toString(int level);
		
	/**
	 * Trigger to run the process of the chain from a certain agent. 
	 * @throws ProcessorChainException 
	 * @throws ProcessFailedException 
	 */
	public void triggerFromAgent() throws ProcessFailedException, ProcessorChainException;
	
	public String getVersion();
	
	public void setVersion(String version);
	
	/**
	 * Return the concrete processor class type of which this processor represented for. 
	 * @return Class type
	 * Created on 17/03/2008
	 */
	public Class<?> getClassType();
	
	/**
	 * Get the field of the concreted processor, by providing the field name. 
	 * If the field does not exist, return null.
	 * @param fieldName in String type
	 * @return Object type
	 * @throws ProcessorChainException 
	 * Created on 17/03/2008
	 */
	public Object getField(String fieldName) throws ProcessorChainException;
	
	/**
	 * Set the processor status to interrupted
	 * 
	 * Created on 18/09/2008
	 */
	public void setInterruptStatus();
	
	/**
	 * Add a var port listener to the concrete processor.
	 * @param listener in VarPortListener type
	 * Created on 26/11/2008
	 */
	public void addTunerPortListener(String varName, TunerPortListener listener);
	
	/**
	 * Remove a var port listener from the concrete processor.
	 * @param listener in VarPortListener type
	 * Created on 26/11/2008
	 */
	public void removeTunerPortListener(String varName, TunerPortListener listener);

	/**
	 * A processor is reprocessable if the concrete processor implementation does not overwrite the 
	 * intermediate result that passed to the processor. 
	 * @return true or false
	 * Created on 01/04/2009
	 */
	public boolean isReprocessable();
	
	/**
	 * Dispose the processor. 
	 * 
	 * Created on 29/06/2009
	 */
	public void dispose();
}
