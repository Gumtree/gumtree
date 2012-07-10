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
import au.gov.ansto.bragg.process.port.Port;

/**
 * Interface of Framework, a Child class of Processor and CompositeProcessor.
 * <p> 
 * A Framework is a processor that can nest other processors or composite processors inside.
 * It is a framework for all processors of an algorithm. 
 * The Framework instances are built from an algorithm recipe xml file. 
 * <p>
 * A framework process has its own In, Out and Var ports, which will passing
 * their signals to their consumers or taking signals from their producers 
 * respectively.
 * <p>
 * Created on 20/02/2007, 9:58:24 AM
 * <p>
 * Last modified 19/04/2007, 9:58:24 AM
 *
 * @author nxi
 * @version V1.0
 * @since M1
 * @see Processor
 * @see CompositeProcessor
 */
public interface Framework extends CompositeProcessor {

	/**
	 * Get the port list of the framework
	 * @return list of ports as List<Port> type
	 */
	public List<Port> getPortArray();
	
	/**
	 * Get the processor list of the framework, which contains all the first level of 
	 * processors nested in the framework.
	 * @return list of processors as List<Processor> type 
	 */
	public List<Processor> getProcessorArray();
	
	/**
	 * Get the agent list of the framework. The agents of the framework are specified
	 * in the algorithm recipe file.
	 * @return list of agents in List<Agent> type
	 */
	public List<Agent> getAgentList();
	
	/**
	 * Get the source processor list of the framework. Source is a child class
	 * of Processor.
	 * @return list of source processor as List<Source> type
	 * @see Source
	 */
	public List<Source> getSourceList();
	
	/**
	 * Get the sink processor list of the framework. Sink is a child class of 
	 * Processor.
	 * @return list of sink processor as List<Sink> type
	 */
	public List<Sink> getSinkList();
	
	/**
	 * Return the default sink defined in the recipe.
	 * @return Sink object
	 * Created on 27/10/2008
	 */
	public Sink getDefaultSink();
}
