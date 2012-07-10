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

import au.gov.ansto.bragg.process.common.Common;
import au.gov.ansto.bragg.process.exception.IndexOutOfBoundException;
import au.gov.ansto.bragg.process.exception.NullMethodException;
import au.gov.ansto.bragg.process.exception.NullPrincipalException;
import au.gov.ansto.bragg.process.processor.Framework;

/**
 * Agents are proxies of ports and processors.  <p>User can use them to check status  of ports and processors. Not all the ports and processors have agents. The  agents are specified in the algorithm recipe xml files. Agents are observable but not controllable. <p> Created on 20/02/2007, 4:37:32 PM <p> Last modified 20/02/2007, 4:37:32 PM
 * @author  nxi
 * @version  V2.4
 */
public interface Agent extends Common {

	/**
	 * Get the time stamp at which the agent was last accessed.
	 * @return number in Long
	 * @since V1.0
	 */
	public Long getLastAccessedTime();
	
	/**
	 * Get principle ID of the agent. A principle is what the agent
	 * represents for. It can be either a port or a processor.
	 * @return  id in int type
	 * @since V1.0 
	 */
	public String getPName();
	
	/**
	 * Get principal's name of the agent. 
	 * @return  name in String
	 * @see Agent#getPID()
	 * @since V1.0
	 */
	public String getPrincipalName();
	
	/**
	 * Abstract method to get the signal information from the agent.
	 * @return generic signal in Object instance
	 * @see PortAgent
	 * @since V1.0
	 */
	public abstract Object getSignal() throws NullPrincipalException;
	
	/**
	 * Get the status of the principal.
	 * If the principal is a processor, return the running status.
	 * If the principal is a port, return the lock status.
	 * @return status information in String
	 * @throws NullPrincipalException  no principal found
	 * @since V1.0
	 */
	public String getStatus();
	
	/**
	 * Set a principal to this agent, a generic principal can be 
	 * either a port or processor
	 * @param principal  generic principal as Common instance
	 * @since V1.0
	 */
	public void setPrincipal(Common principal);
	
	/**
	 * An abstract method to set a principal to the agent.
	 * @throws NullPrincipalException 
	 * @see ProcessorAgent
	 * @since V1.0
	 */
	public abstract void setPrincipal(Framework framework) throws IndexOutOfBoundException, NullPrincipalException;
	
	/**
	 * Subscribe a thread listening to the status changing event of the processor 
	 * referred by the agent. When the status of the processor is changed, the 
	 * waiting thread will be notified.
	 * @param listener as customized Thread type
	 * @throws NullPointerException illegal listener
	 * @since V2.4
	 */
	public void subscribe(AgentListener listener) throws NullPointerException;
	
	/**
	 * This method will notify the listeners of the agent about a status change.
	 * This method is called by the referred processor when the processing 
	 * status changes. 
	 * @throws NullMethodException failed to notify the listeners
	 * @since V2.4
	 */
	public void statusTransfer();
	
	/**
	 * Unsubscribe the listening thread from the listener list of the agent.
	 * @param listener as customized Thread type
	 * @throws NullPointerException can not find the listener
	 * @since V2.4
	 */
	public void unsubscribe(AgentListener listener) throws NullPointerException;
	
	/**
	 * Get the UI label of the agent.
	 * @return object in String type
	 * @since V2.6
	 */
	public String getLabel();

	/**
	 * Dispose the agent;
	 * 
	 * Created on 29/06/2009
	 */
	public void dispose();

}
