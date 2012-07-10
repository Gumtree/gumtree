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

import au.gov.ansto.bragg.cicada.core.exception.NoneAlgorithmException;
import au.gov.ansto.bragg.cicada.core.exception.SubscribeFailException;
import au.gov.ansto.bragg.process.agent.Agent;
import au.gov.ansto.bragg.process.agent.AgentListener;
import au.gov.ansto.bragg.process.processor.Sink;
import au.gov.ansto.bragg.process.processor.SinkListener;

/**
 * The subscribe API for algorithm manager
 * <p>
 * Created on 20/02/2007, 9:58:24 AM
 * Last modified 17/04/2007, 9:58:24 AM
 * @see au.gov.ansto.bragg.cicada.core.AlgorithmManager
 *
 * @author jgw + nxi
 * @version 2.0
 * @since V1.0
 */
public interface Subscribe {

	/**
	 * Get sink list of the current algorithm of algorithm manager. (List of all sinks)
	 * @return  list of sinks
	 * @throws NoneAlgorithmException  no current algorithm handle
	 * @since V1.0 
	 */
	public List<Sink> getSinkList() throws NoneAlgorithmException;
	
	/**
	 * Get the sink list of a loaded algorithm of the algorithm manager.
	 * @param algorithm  algorithm handle
	 * @see Subscribe#getSinkList()
	 * @since V1.0 
	 */
	public List<Sink> getSinkList(Algorithm algorithm);
	
	/*
	 * Get signal of a sink agent; 
	 *
	public Object getSinkSignal(Sink sink);
	*/
	
	/**
	 * Subscribe a thread instance to a specific agent. If the agent updates its signal,
	 * it will notify all the subscribed threads and updating routines will be activated
	 * by the threads.
	 * This instance will be informed when the agent status changes.
	 * @param receiver  customized Thread instance
	 * @param agent  agent handle
	 * @throws SubscribeFailException failed to subscribe 
	 * @since V2.4 
	 */
	public void subscribeSink(AgentListener receiver, Agent agent);
	
	/**
	 * Subscribe a thread instance to a specific sink. If the sink updates its signal,
	 * it will notify all the subscribed threads and updating routines will be activated
	 * by the threads.
	 * This instance will be informed when the sink get new signal.
	 * @param receiver  customized Thread instance
	 * @param sink  sink processor handle
	 * @throws SubscribeFailException failed to subscribe 
	 * @since V1.0
	 * @deprecated since V3.2, use {@link #subscribeSink(SinkListener, Sink)} instead 
	 */
	public void subscribeSink(Thread receiver, Sink sink);
	
	/**
	 * Unsubscribe a thread instance from a specific agent. Remove it from the listener
	 * list of the agent.
	 * @param receiver  Thread instance
	 * @param agent  agent handle 
	 * @throws SubscribeFailException failed to unsubscribe 
	 * @since V2.4 
	 */
	public void unsubscribeSink(AgentListener receiver, Agent agent) ;
	
	/**
	 * Unsubscribe a thread instance from a specific sink. Remove it from the listener
	 * list of the sink processor.
	 * @param receiver  Thread instance
	 * @param sink  sink processor handle 
	 * @throws SubscribeFailException failed to unsubscribe 
	 * @since V1.0 
	 * @deprecated since V3.2, use {@link #unsubscribeSink(SinkListener, Sink)} instead
	 */
	public void unsubscribeSink(Thread receiver, Sink sink);

	/**
	 * Subscribe a listener to a sink.
	 * @param listener SinkListener object
	 * @param sink Sink object
	 * Created on 22/09/2008
	 */
	public void subscribeSink(SinkListener listener, Sink sink);

	/**
	 * Unsubscribe a thread instance from a specific sink. Remove it from the listener
	 * list of the sink processor.
	 * @param listener object of SinkListener
	 * @param sink  sink processor handle 
	 * @throws SubscribeFailException failed to unsubscribe 
	 * @since V3.2 
	 */	
	public void unsubscribeSink(SinkListener listener, Sink sink);
}
