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

import au.gov.ansto.bragg.process.exception.ProcessorChainException;


/**
 * Interface of Sink_, a Child class of Processor. Sink instances deliver result signal
 * of an algorithm to the receivers, for example, views and exporters. 
 * <p> 
 * A Sink is a processor that takes result from other processor. The sink has a subscriber
 * list. When the sink takes new signal, all the subscribers will be notified. So that 
 * the subscriber can update signal from the sink.
 * <p>
 * A Sink object usually has only one In port and no Out port. It can have Var ports for
 * controlling purpose, for example enabling or disabling.
 * <p>
 * Created on 20/02/2007, 9:58:24 AM
 * <p>
 * Last modified 19/04/2007, 9:58:24 AM
 *
 * @author nxi
 * @version V1.0
 * @see Processor
 */
public interface Sink extends Processor {

	/**
	 * Subscribe a thread listening to the signal changing event of the sink processor
	 * When the signal is changed, the waiting thread will be notified
	 * @param listener as customized Thread type
	 * @throws NullPointerException
	 * @since V1.0
	 */
	public void subscribe(Thread listener) throws NullPointerException;
	
	/**
	 * Unsubscribe the listening thread from the listener list.
	 * @param listener as customized Thread type
	 * @since V1.0
	 */
	public void unsubscribe(Thread listener) throws NullPointerException;
	
	/**
	 * Get the property of the processor which this sink is attached to. The name of
	 * the property is given in a String type.
	 * @param propertyName String object
	 * @return Object type
	 * Created on 17/03/2008
	 * @throws ProcessorChainException 
	 */
	public Object getProperty(String propertyName) throws ProcessorChainException;
	
	/**
	 * Return true if the sink has been set to be plotted automatically. 
	 * To set this in the recipe file, put a 'autoplot=true' attribute in the sink
	 * item. 
	 * @return true or false
	 * Created on 04/09/2008
	 */
	public boolean isAutoPlot();
	
	/**
	 * Add status listener to the sink. 
	 * @param listener object of SinkListener
	 * Created on 22/09/2008
	 */
	public void subscribe(SinkListener listener);

	/**
	 * Remove the listener from the sink. 
	 * @param listener object of SinkListener
	 * Created on 22/09/2008
	 */
	public void unsubscribe(SinkListener listener);

	/**
	 * Check if the sink is the default sink of an algorithm
	 * @return true or false
	 * Created on 27/10/2008
	 */
	public boolean isDefault();
}
