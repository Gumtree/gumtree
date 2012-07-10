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
package au.gov.ansto.bragg.process.port;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import au.gov.ansto.bragg.process.common.Common_;
import au.gov.ansto.bragg.process.common.exception.IllegalNameSetException;
import au.gov.ansto.bragg.process.configuration.PortConfiguration;
import au.gov.ansto.bragg.process.exception.IndexOutOfBoundException;
import au.gov.ansto.bragg.process.exception.ProcessFailedException;
import au.gov.ansto.bragg.process.exception.ProcessorChainException;
import au.gov.ansto.bragg.process.processor.Processor;
import au.gov.ansto.bragg.process.processor.Sink_;
import au.gov.ansto.bragg.process.util.SortedArrayList;
/**
 * @author nxi
 * Created on 30/01/2007, 12:26:05 PM
 * Last modified 30/01/2007, 12:26:05 PM
 * 
 * @param <T>
 */

public class Port_ extends Common_ implements Port {

	public final static long serialVersionUID = 1L;

	protected int dimension;
	protected Processor parent;
	protected Object signal;
//	protected int recipeID; //named 'id' in the XML prescription file. 
	protected Class<?> type;
	protected boolean locked = false;
	protected Port producer = null;
	protected List<Port> consumerList;
	protected int numberOfToken = 0;
	protected boolean cached = false;
	protected Object cache = null;
	Object signalObject;
	//********************** trial data ***********************
	int[] dim = {1, 1};
	Double abc[] = {2D, 4D, 6D};
	Double x = 1.2;

	public Port_(){ super(); }

	public Port_(final String name){
		super(name);
	}

	public Port_(final Class<?> type, final int dimension, 
			final Processor parent, final String name){
		this(name);
//		this.recipeID = receipeID;
		this.type = type;
		this.dimension = dimension;
		this.parent = parent;
	}

	public Port_(final PortConfiguration portConfiguration, final Processor parent) 
	throws ProcessorChainException {
		super();
		configure(portConfiguration, parent);
	}

	public Port_(final PortConfiguration portConfiguration, final List<Processor> processorList) 
	throws ProcessorChainException {
//		super();
//		configure(portConfiguration, (Processor) processorList.get(portConfiguration.getParentName()));
		try {
			configure(portConfiguration, SortedArrayList.getProcessorFromName(processorList, portConfiguration.getParentName()));
		} catch (IndexOutOfBoundException e) {
			throw new ProcessorChainException("failed to bound port " + portConfiguration.getName() + 
					" with processor " + portConfiguration.getParentName());
		}
	}

	protected int dimension(){
		return dimension;
	}

	protected Processor parent(){
		return parent;
	}

	protected Class<?> type(){
		return type;
	}

	protected Object signal(){
		return signal;
	}

//	protected int receipeID(){
//	return recipeID;
//	}

//	public int getRecipeID(){
//	return receipeID();
//	}

//	public void setReceipeID(int id){
//	recipeID = id;
//	}

	public String getPortType(){
		return "port";
	}

	/*
	 * Connect to producer;
	 */
	public void setProducer(Port producer) throws IllegalArgumentException{
		if (producer == null){
			throw new IllegalArgumentException("null port.");
		}
		this.producer = producer; 
	}

	public void addConsumer(Port consumer) throws IllegalArgumentException{
		if (consumer == null){
			throw new IllegalArgumentException("null port.");
		}
		consumerList.add(consumer);
	}

	public List<Port> getConsumerList(){
		return consumerList;
	}

	/*
	 * Return this IN port's producer.
	 * (non-Javadoc)
	 * @see au.gov.ansto.bragg.process.port.In#getProducer()
	 */
	public Port getProducer() {
		return producer;
	}

	public void configure(final PortConfiguration portConfiguration, final Processor parent) 
	throws ProcessorChainException {
		try {
			setName(portConfiguration.getName());
		} catch (IllegalNameSetException e) {
			throw new ProcessorChainException("failed to load the processor " + portConfiguration.getName() +
					", check the recipe file", e);
		}
//		recipeID = portConfiguration.getReceipeID();
		try {
			type = Class.forName(portConfiguration.getType());
		} catch (ClassNotFoundException e) {
			throw new ProcessorChainException("failed to setup " + portConfiguration.getType()+ 
					" type for port " + portConfiguration.getName() + ": " + e.getMessage(), e);
		}
//		type = Class.forName(portConfiguration.getType());
		dimension = portConfiguration.getDimension();
//		setParent((Processor) processorList.get(portConfiguration.getParentID()));			
		setParent(parent);
		consumerList = new LinkedList<Port>();
		try {
			signal = Array.newInstance(type, SortedArrayList.getDimensionArgument(dimension));
		} catch (Exception e) {
			throw new ProcessorChainException("failed to create array of " + portConfiguration.getType() + 
					": " + e.getMessage(), e);
		} 
	}

	public int getDimension() {
//		Double x = 0.5;
		return dimension();
	}

	public Processor getParent() {
		return parent();
	}

	public Object getSignal() {
		return signal();
	}

	public Class<?> getType() {
		return type();
	}

	protected void lock(){
		locked = true;
	}

	protected void unlock(){
		locked = false;
	}

	public boolean getLockStatus(){
		return locked;
	}

	public void setType(final Class<?> type){
		this.type = type;
	}

	/*
	 * Implementation Modifier 
	 */

	public void setDimension(final int dimension){
		this.dimension = dimension;
	}

	public void setParent(final Processor parent){
		this.parent = parent;
	}

	/****************************(1)*******************************
	 * Set cache signal of this OUT port.
	 * Called by its parent processor.
	 * @throws ProcessorChainException 
	 * @throws ProcessFailedException 
	 */
	public void setCach(final Object signal) throws ProcessorChainException, ProcessFailedException {
		if (locked){
			if (signal == null){
//				throw new IllegalArgumentException("null signal.");
			}
			cache = signal;
			cached = true;
		} else{
			setSignal(signal);
			cached = false;
		}
	}	

	public void setOutput(final Object signal) throws ProcessorChainException, ProcessFailedException{
		if (signal == null) 
			throw new IllegalArgumentException("null signal");
		this.signal = signal;
		setParentField();
		if (parent instanceof Sink_)
			informParent();
	}

	protected void informParent() throws ProcessFailedException, ProcessorChainException{}

	public void setOutputToConsumer(final Object signal) 
	throws ProcessorChainException, ProcessFailedException{
		setOutput(signal);
		if (consumerList.size() > 0){
			Iterator<Port> iter = consumerList.iterator();
			while(iter.hasNext()){
				Port consumer = iter.next();
				System.out.println("Send signal from port_" + this.getPortType() + "_" + 
						this.getName() + " to port_" + consumer.getPortType() + "_" + 
						consumer.getName() + ", type=" + getSignal().getClass().toString());
				consumer.setOutput(signal);
			}
		}
	}

	/****************************(2)*******************************
	 * Move cache information to signal.
	 * Called when all consumer tokens are released and cache is full.
	 * @throws ProcessFailedException 
	 * @throws ProcessorChainException 
	 */
	protected void cacheToSignal() throws ProcessorChainException, ProcessFailedException{
		Object object = cache;
		clearCache();
		setSignal(object);
	}

	/****************************(3)*******************************
	 * @throws ProcessorChainException 
	 * @throws ProcessFailedException 
	 * 
	 */
	protected void setSignal(final Object signal) throws ProcessorChainException, ProcessFailedException {
		this.signal = signal;
		setParentField();
		ready();
	}

	/******************************(4)*******************************
	 * Clear the cache area.
	 * Called when cache information is passed to signal area.
	 */
	protected void clearCache(){
		cache = null;
		cached = false;
	}

	protected void setParentField() throws ProcessorChainException{}

	/*****************************(5)*******************************
	 * When ready, lock the port, and inform the consumer.
	 * (non-Javadoc)
	 * @throws ProcessorChainException 
	 * @throws ProcessFailedException 
	 * @see au.gov.ansto.bragg.process.port.Out#ready()
	 */
	protected void ready() throws ProcessorChainException, ProcessFailedException {
		informConsumer();
	}

	/*****************************()*******************************
	 * Inform the parent processor that signal is ready,
	 * by calling tokenSignalReady() of its parent processor.
	 * This action may active the processing of the processor.
	 *
	protected void informParent(boolean inPortStatus){
		this.parent().tokenSignalReady();
	}*/

	/*****************************(6)*******************************
	 * Informa consumer that the signal is ready.
	 * Called when the signal is set.
	 * @throws ProcessorChainException 
	 * @throws ProcessFailedException 
	 */
	protected void informConsumer() throws ProcessorChainException, ProcessFailedException{
		if (consumerList.size() > 0){
			Iterator<Port> iter = consumerList.iterator();
			while(iter.hasNext()){
				Port consumer = iter.next();
				System.out.println("Send signal from port_" + this.getPortType() + "_" + 
						this.getName() + " to port_" + consumer.getPortType() + "_" + 
						consumer.getName() + ", type=" + (getSignal() == null ? "null" : getSignal().getClass().toString()));
				consumer.setCach(signal);
			}
		}
	}

	/*****************************(7)*******************************
	 * Capture one token of the VAR port.
	 * Called by its consumer,
	 * when the consumer's processing is trigered.
	 */
	public void captureToken(){
		numberOfToken++;
		lock();
	}


	/****************************(8)*******************************
	 * After all consumers paid, unlock the port.
	 * @throws ProcessFailedException 
	 * @throws ProcessorChainException 
	 */
	protected void allTokenReleased() throws ProcessorChainException, ProcessFailedException{
		unlock();
//		informProducer();
		if (cached) 
			cacheToSignal();
	}

	protected void informProducer() throws ProcessorChainException, ProcessFailedException{
		producer.releaseToken();
	}

	/****************************(9)******************************* 
	 * When one consumer finished transfer, it releases one token
	 * of the producer. 
	 * When all tokens of the producer have been released, call
	 * allTokenReleased() method. 
	 * 
	 * (non-Javadoc)
	 * @throws ProcessFailedException 
	 * @throws ProcessorChainException 
	 * @see au.gov.ansto.bragg.process.port.Out#releaseToken()
	 */	
	public void releaseToken() throws ProcessorChainException, ProcessFailedException {
		if (numberOfToken == 0)
			return;
		if(--numberOfToken == 0) 
			allTokenReleased();		
	}

	public String getStatus(){
		return locked ? "locked" : "unlocked";
	}
	/*
	public void setSignal(T signal){
		try{
			super.setSignal(signal);
			informConsumer();
		}catch(IllegalArgumentException ex){
			throw new IllegalArgumentException(ex.getMessage() + "Set signal failed.");
		}		
	}*/

	/*
	 * toString() method.
	 */
	public String toString(){
		String result = super.toString();
//		result += "<receipe_id>" + getRecipeID() + "</receipe_id>\n";
		result = result + "<name>" + getName() + "</name>\n";
		result = result + "<dimension>" + dimension() + "</dimension>\n";
		result = result + "<type>" +  type().getName() + "</type>\n";
		result = result + "<parent>" + parent().getName() + "</parent>\n";
		result = result + "<lock_status>" + getLockStatus() + "</lock_status>\n";
		result = result + "<producer>";
		if (getProducer() == null) result += "none";
		else result += getProducer().getPortType() + "_" + getProducer().getName();
		result += "</producer>\n";
		result = result + "<consumers>";
		if (getConsumerList().size() == 0) result += "none";
		else{
			result += "\n";
			for (Iterator<Port> iter = getConsumerList().iterator(); iter.hasNext();){
				Port port = iter.next();
				result += "<consumer>" + port.getPortType() + "_" + port.getName();
				result += "</consumer>\n";
			}
		}
		result = result + "</consumers>\n";

//		result = result + SortedArrayList.preAdd(signal, cache) + "</consumers>\n";
		/*		try{
			result += "<signal information>\n" + ((SimpleBeanInfo) signal).getBeanDescriptor().toString() + "</signal information>\n";
			}catch (Exception ex){
				result += "<exception>" + ex.getMessage() + "</exception>\n";
			}
		 */			
		return result;
	}	

	public String getCoreName(){
		String name = getName();
		if (name.contains(".")){
			try{
				name = name.substring(name.lastIndexOf(".") + 1);
			}catch (Exception e) {
				// TODO: handle exception
			}
		}
		return name;
	}
}
