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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import au.gov.ansto.bragg.process.common.Common;
import au.gov.ansto.bragg.process.common.Common_;
import au.gov.ansto.bragg.process.configuration.AgentConfiguration;
import au.gov.ansto.bragg.process.exception.IndexOutOfBoundException;
import au.gov.ansto.bragg.process.exception.NullPrincipalException;
import au.gov.ansto.bragg.process.processor.Framework;

public class Agent_ extends Common_ implements Agent {

	public final static long serialVersionUID = 1L;
	
	private Long lastAccessedTime = 0L;
	protected Common principal = null;
//	protected int receipeID;
	protected String principalName = null ;
	protected String label = null ; 
	protected String pName;
	protected String status = null;
	protected List<AgentListener> listenerList = null;
	
	/*
	 * Default constructor, which will set a primary id.
	 */
	public Agent_() {
		super();
		listenerList = new LinkedList<AgentListener>();
	}

	/*
	 * Constructor with configuration information
	 */
	public Agent_(AgentConfiguration configuration){
		this(configuration.getName());
//		receipeID = configuration.getReceipeID();
		principalName = configuration.getPrincipal();
		label = configuration.getDescription();
		pName = configuration.getPName();
	}

	/*
	 * Constructor from principal
	 */
	public Agent_(Common principal) {
		this();
		this.principal = principal;
	}
	
	/*
	 * Constructor taking a name as the parameter
	 */
	public Agent_(String name) {
		super(name);
		listenerList = new LinkedList<AgentListener>();
	}

	protected String listenersToString(){
		String result = "<listeners>\n";
		if (listenerList != null){
			for (Iterator<?> iter = listenerList.iterator(); iter.hasNext();)
				result += "<listener>" + iter.next().toString() + "</listener>\n";
		}
		else result += "null";
		result += "</listeners>\n";
		return result;
	}
	
	public String getDescription(){
		this.setTimestamp();
		return label;
	}

	/**
	 * Get the UI label of the agent.
	 * @return object in String type
	 * @since V2.6
	 */
	public String getLabel(){
		this.setTimestamp();
		return label;
	}
	
	public Long getLastAccessedTime() {
		return lastAccessedTime;
	}

	public String getPName(){
		return pName;
	}
	
	public String getPrincipalName() {
		this.setTimestamp();
		return principalName;
	}

	public Object getSignal() throws NullPrincipalException{
		return null;
	}
	
	public String getStatus() {
		this.setTimestamp();	
		return status;
	}

//	public int getReceipeID(){
//		return receipeID;
//	}
	
	public void setPrincipal(Common principal) {
		this.principal = principal;
	}
	
	public void setPrincipal(Framework framework) throws IndexOutOfBoundException, NullPrincipalException{}
	
	public void subscribe(AgentListener listener) throws NullPointerException{
		if (listenerList == null) throw new NullPointerException("sink is not initialized");
		if (listener == null) throw new NullPointerException("thread does not exist");
		listenerList.add(listener);
//		try{
//		synchronized(listener){
//			if (!listener.isAlive())
//				listener.start();			
//		}
//		}catch (Exception ex){
//			throw new NullPointerException("failed to subscribe");
//		}
	}
	
	public void statusTransfer() {
		if (listenerList != null){
			for (Iterator<?> iter = listenerList.iterator(); iter.hasNext();){
				AgentListener listener = (AgentListener) iter.next();
				listener.onChange(this);
//				synchronized(listener){
//					listener.notify();
//				}
			}
		}
	}

	public void unsubscribe(AgentListener listener) throws NullPointerException{
		if (listenerList == null) throw new NullPointerException();
		listenerList.remove(listener);
		//listener.stop();
	}
	
	public void dispose(){
		principal = null;
		listenerList.clear();
	}
}
