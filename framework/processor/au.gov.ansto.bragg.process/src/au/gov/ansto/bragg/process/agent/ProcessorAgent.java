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
package au.gov.ansto.bragg.process.agent;

import java.util.List;

import au.gov.ansto.bragg.process.exception.ProcessFailedException;
import au.gov.ansto.bragg.process.exception.ProcessorChainException;
import au.gov.ansto.bragg.process.port.Tuner;
import au.gov.ansto.bragg.process.processor.Sink;
import au.gov.ansto.bragg.process.processor.Processor_.ProcessorStatus;


/**
 * A child of Agent. Which represents a Processor object. 
 * <p>
 * The principal of the agent is a Port instance.
 * <p>
 * Created on 20/02/2007, 4:58:43 PM
 * <p>
 * Last modified 20/02/2007, 4:58:43 PM
 * @author nxi
 * @version M1
 * @since V1.0
 * @see Agent
 */
public interface ProcessorAgent extends Agent {

	/**
	 * This method retrieves the option tuners owned by the processor referred by the 
	 * agent.
	 * @return list of option tuners
	 * @since V2.4
	 */
	public List<Tuner> getOptions();
	
	/**
	 * This method retrieves the parameter tuners owned by the processor referred by the 
	 * agent.
	 * @return list of parameter tuners
	 * @since V2.4
	 */
	public List<Tuner> getParameters();
	
	/**
	 * This method retrieves the region tuners owned by the processor referred by the 
	 * agent.
	 * @return list of region tuners
	 * @since V2.5
	 */
	public List<Tuner> getRegionTuners();
	
	/**
	 * Get the list of sinks that catch the outputs of this agent's principal processor. 
	 * @return a list of Sink objects
	 */
	public List<Sink> getSinkList();
	
	/**
	 * This method retrieves all the tuners that are owned by the processor referred by
	 * this agent. 
	 * @return list of tuners
	 * @since V2.4
	 */
	public List<Tuner> getTuners();

	/**
	 * This method sets the processor referred by this agent as the starting point of the 
	 * processor chain and execute the processing. 
	 * @throws ProcessorChainException 
	 * @throws ProcessFailedException 
	 */
	public void trigger() throws ProcessFailedException, ProcessorChainException ;
	
	/**
	 * Get the version of the processor referred by this agent.
	 * @return version number in String type.
	 */
	public String getVersion();

	/**
	 * Get the status of the processor.  
	 * @return enum type of value
	 * Created on 04/09/2008
	 */
	public ProcessorStatus getProcessorStatus();
	
	/**
	 * Return the list of sinks that are set to be automatically plotted by GUI.
	 * @return List of Sink object
	 * Created on 04/09/2008
	 */
	public List<Sink> getAutoPlotSinkList();
	
	/**
	 * Set the processor's status to be interrupted.
	 * 
	 * Created on 18/09/2008
	 */
	public void setInterruptStatus();
	
	/**
	 * Return if the processor represented by the agent is reprocessable. 
	 * A processor is reprocessable if the concrete processor implementation does not overwrite the 
	 * intermediate result that passed to the processor. 
	 * @return true or false
	 * Created on 01/04/2009
	 */
	public boolean isReprocessable();
	
	/**
	 * Get the tuner of the agent by its name. 
	 * @param name
	 * @return Tuner object
	 * Created on 03/07/2009
	 */
	public Tuner getTuner(String name);
}
