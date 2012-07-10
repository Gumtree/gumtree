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

import java.util.Iterator;

import au.gov.ansto.bragg.process.configuration.OutConfiguration;
import au.gov.ansto.bragg.process.exception.ProcessorChainException;
import au.gov.ansto.bragg.process.processor.Processor;
import au.gov.ansto.bragg.process.processor.Sink;
import au.gov.ansto.bragg.process.processor.Sink_;

public class Out_ extends Port_ implements Out {

	public final static long serialVersionUID = 1L;
	
	public Out_(final OutConfiguration configuration, final Processor parent) 
	throws ProcessorChainException {
		super(configuration, parent);
	}
	/*
	 * When the port is locked, reset the number of token. to
	 * the number of consumers.
	 *
	protected void resetNumberOfToken(){
		numberOfToken = getConsumerList().size();
	}*/
	
	/*
	 * Send ready signal to consumers.
	 * Called by this OUT port when signal is ready.
	 *
	protected void informConsumer(){
		Iterator<In<T>> iter = consumerList.iterator();
		while (iter.hasNext()){
			In<T> consumer = (In<T>) iter.next();
			consumer.producerIsReady();
		}
	}*/

	/*
	 * During configuration of the OUT port, connect it to its 
	 * consumer IN port one by one.
	 * Call List:add method to add a consumer link.
	 * Number of the consumers ++.
	 *
	public void connect(In<T> consumer){
		
		consumerList.add(consumer);
		numberOfConsumer++;
	}*/
	
	/*
	 * When ready, lock the port, and inform the consumer.
	 * (non-Javadoc)
	 * @see au.gov.ansto.bragg.process.port.Out#ready()
	 *
	protected void ready(){
		lock();
		informConsumer();
	}*/
	
	public String getPortType(){
		return "out";
	}

	/*
	 * If the port is not locked, write signal directly to this.signal.
	 * Otherwise, write signal to cache.
	 * Set cached flag.
	 *
	public void setCach(T signal){
		if (locked){
			try{
				if (signal == null){
					throw new IllegalArgumentException("no signal.");
				}
				cache = signal;
				cached = true;
			}catch(IllegalArgumentException ex){
				throw new IllegalArgumentException("set cache failed.");
			}
		}else{
			setSignal(signal);
			ready();
		}
	}*/
	
	/*
	 * Clear the cache area.
	 * Called when cache information is passed to signal area.
	 *
	protected void clearCache(){
		cache = null;
		cached = false;
	}*/
	
	/*
	 * Move cache information to signal.
	 * Called when all consumcer tokens are released and cache is full.
	 *
	protected void cacheToSignal(){
		try{
			setSignal(cache);
			clearCache();
			ready();
		}catch(IllegalArgumentException ex){
			throw new IllegalArgumentException("cache to signal failed.");
		}
	}*/
	
	/* When one consumer finished transfer, it releases one token
	 * of the producer. 
	 * When all tokens of the producer have been released, call
	 * done() method. 
	 * 
	 * (non-Javadoc)
	 * @see au.gov.ansto.bragg.process.port.Out#releaseToken()
	 *	
	public void releaseToken(){
		if(--numberOfToken == 0) allTokenReleased();		
	}*/
	
	/*
	 * After all consumers paid, unlock the port.
	 *
	protected void allTokenReleased(){
		unlock();
		if (cached) cacheToSignal();
	}*/
	
	/*
	 * toString() method.
	 */
	@Override
	public String toString(){
		String result = "<out>\n" + super.toString();
//		String result = "<out>\n" + super.toString() + "<consumer list>";
/*		Iterator<In<T>> iter = consumerList.iterator();
		while (iter.hasNext()){
			In<T> consumer = (In<T>) iter.next();
			result = result + "<consumer id=\"" + consumer.getNumber() + "\" name=\"" + consumer.getName() + "\" />\n";
		}
		result = result + "</consumer list>\n";
*/		
		result += "</out>\n";
		return result;
	}

	public Sink getSink() {
		// TODO Auto-generated method stub
		for (Iterator<Port> iter = consumerList.iterator(); iter.hasNext();){
			Processor processor = iter.next().getParent();
			if (processor instanceof Sink_) return (Sink) processor;
		}
		return null;
	}
}
