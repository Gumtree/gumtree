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

import au.gov.ansto.bragg.process.configuration.InConfiguration;
import au.gov.ansto.bragg.process.exception.ProcessFailedException;
import au.gov.ansto.bragg.process.exception.ProcessorChainException;
import au.gov.ansto.bragg.process.processor.Processor;

public class In_ extends Port_ implements In {
		
	public final static long serialVersionUID = 1L;
	
	protected boolean producerIsReady = false;
	
/*	public In_(final int number, final Class type, final int dimension, 
			final Processor parent, final String name, final Out<T> source){
		super(number, type, dimension, parent, name);
		connect(source);
	}
*/
	
	public In_(final InConfiguration configuration, final Processor parent) 
	throws ProcessorChainException {
		super(configuration, parent);
	}
	/*
	 * Connect to its producer.
	 * (non-Javadoc)
	 * @see au.gov.ansto.bragg.process.port.In#connect(au.gov.ansto.bragg.process.port.Out)
	 *
	protected void connect(Out<T> producer) {
		// TODO Auto-generated method stub
		this.producer = producer;
	}*/
	
	public String getPortType(){
		return "in";
	}
	
	/**Remove on Feb. 2nd.
	/*
	 * Configure method.
	 *
	public void configure(final InConfiguration configuration, final Processor parent){
		super.configure(configuration, parent);
	}
	*/

	/*
	 * Receive signal from the producer.
	 *
	public void producerIsReady(){
		producerIsReady = true;
		informParent(producerIsReady);
	}*/
	
	/*****************************(5)*******************************
	 * When ready, lock the port, and inform the consumer.
	 * (non-Javadoc)
	 * @throws ProcessorChainException 
	 * @throws ProcessFailedException 
	 * @see au.gov.ansto.bragg.process.port.Out#ready()
	 */
	protected void ready() throws ProcessorChainException, ProcessFailedException {
		captureToken();
		informParent();
		informConsumer();
//		unlock();
	}
	
	/*
	 * Inform the parent processor that signal is ready,
	 * by calling tokenSignalReady() of its parent processor.
	 * This action may active the processing of the processor.
	 */
	protected void informParent() throws ProcessFailedException, ProcessorChainException{
		parent().inTokenSignalReady();
	}
	
	/*
	 * Release the this IN signal port. 
	 * Called by its parent processor after a successful transfer.
	 * Will release the connected producer.
	 *
	public void release(){
		unlock();
		feedBack();
	}*/
	
	/*
	 * Feed back information to its producer.
	 * Inform the producer to release one token.
	 * Called when this IN port is released.
	 *
	protected void feedBack(){
//		producer.releaseToken();
	}*/

	/*
	 * toString() method.
	 * (non-Javadoc)
	 * @see au.gov.ansto.bragg.process.port.Port_#toString()
	 */
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String result = "<in>\n" + super.toString();
//		result = result + "<producer id=\"" + getProducer().getNumber() + "\" name=\"" + getProducer().getName() + "\" />";
		result += "</in>\n";
		return result; 
	}
	
	protected void setParentField() throws ProcessorChainException {
		parent.setField(getCoreName(), signal, type);
	}
}