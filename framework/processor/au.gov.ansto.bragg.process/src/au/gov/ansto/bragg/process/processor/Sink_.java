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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import au.gov.ansto.bragg.process.common.exception.IllegalNameSetException;
import au.gov.ansto.bragg.process.configuration.SinkConfiguration;
import au.gov.ansto.bragg.process.exception.ProcessorChainException;
import au.gov.ansto.bragg.process.port.In;
import au.gov.ansto.bragg.process.port.Port;

public class Sink_ extends Processor_ implements Sink {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected List<Thread> listenerList = null;
	protected List<SinkListener> sinkListeners;
	protected Object field = null;
	private boolean isAutoPlot = false;
	private boolean isDefault = false;
	
	public Sink_(){
		super();
	}
	
	public Sink_(final SinkConfiguration configuration, Processor_ parent) 
	throws ProcessorChainException {
		createIns(configuration.getInConfigurationList());
		createOuts(configuration.getOutConfigurationList());
		createVars(configuration.getVarConfigurationList());
		try {
			setName(configuration.getName());
		} catch (IllegalNameSetException e) {
			throw new ProcessorChainException("failed to load the processor " + configuration.getName() +
					", check the recipe file", e);
		}
//		setRecipeID(configuration.getReceipeID());
		resetInToken();
//		createMethod(configuration.getMethodNameList());
//		setMethodNameList(configuration.getMethodNameList());
		setParent(parent);
		listenerList = new LinkedList<Thread>();
		setAutoPlot(configuration.getAutoPlot());
	}
	
	private void setAutoPlot(String autoPlot) {
		if (autoPlot != null)
			try{
				isAutoPlot = Boolean.valueOf(autoPlot);
			}catch (Exception e) {
			}
	}

	public void setDefault(boolean isDefault){
		this.isDefault = isDefault;
	}
	
	@Override
	public Object getSignal(){
		return this.getInList().get(0).getSignal();
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
	
	public void subscribe(Thread listener) throws NullPointerException{
		if (listenerList == null) throw new NullPointerException("sink is not initialized");
		if (listener == null) throw new NullPointerException("thread does not exist");
		listenerList.add(listener);
		try{
		synchronized(listener){
			if (!listener.isAlive())
				listener.start();			
		}
		}catch (Exception ex){
			throw new NullPointerException("failed to subscribe");
		}
	}
	
	public void subscribe(SinkListener listener) {
		if (sinkListeners == null) 
			sinkListeners = new ArrayList<SinkListener>();
		if (listener == null) 
			return;
		sinkListeners.add(listener);
	}

	public void unsubscribe(SinkListener listener) {
		if (sinkListeners == null) 
			return;
		sinkListeners.remove(listener);
	}
	
	public String toString(){
		String result = "<sink id=\"" + getID() + "\" name=\"" + getName() +
		"\">\n";
//		result += "<method>" + this.getMethodName() + "</method>\n";
//		result += "<receipe_id>" + getRecipeID() + "</receipe_id>\n";
		result += portsToString();
		result += listenersToString();
		result += "</sink>\n";
		return result;
	}
	
	public String toString(int level){
		String result = "<sink id=\"" + getID() + "\" name=\"" + getName() +
		"\">\n";
//		result += "<receipe_id>" + getRecipeID() + "</receipe_id>\n";
		result += portsToString();
		result += listenersToString();
		result += "</sink>\n";
		return result;
	}
	
	protected synchronized void transfer() throws ProcessorChainException {
		if (sinkListeners != null){
			for (SinkListener listener : sinkListeners)
				listener.onChange();
		}
		resetInToken();
	}

	public void unsubscribe(Thread listener) throws NullPointerException{
		if (listenerList == null) throw new NullPointerException();
		listenerList.remove(listener);
		//listener.stop();
	}
	
	@Override
	public void setField(String fieldName, Object signal, Class<?> type) {
		field = signal;
	}
	
	public Object getField(String fieldName){
		return field;
	};
	
	public Object getProperty(String propertyName) throws ProcessorChainException{
		Processor processor = findPrincipal();
		Object property = null;
		if (processor != null){
			property = processor.getField(propertyName);
		}
		return property;
	}
	
	public Processor findPrincipal(){
		In in = inList.get(0);
		Port producer = in.getProducer();
		Processor processor = null; 
		if (producer != null) 
			processor = producer.getParent();
		return processor;
	}
	
	public boolean isAutoPlot(){
		return isAutoPlot;
	}

	public boolean isDefault() {
		return isDefault;
	}
	
	public void dispose(){
		super.dispose();
		if (listenerList != null){
			listenerList.clear();
		}
		if (sinkListeners != null)
			sinkListeners.clear();
		field = null;
	}
}
