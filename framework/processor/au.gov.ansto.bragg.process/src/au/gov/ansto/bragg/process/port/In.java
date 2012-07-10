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

/**
 * A child class of Port to take input of processors. 
 * <p>
 * An In port is build upon a Processor object to take input for the processor.
 * An In port will take signal from its producer port and pass it to another 
 * In port if necessary. 
 * <p> 
 * When a In port object is created, it is configured to be able
 * to take certain type of signal information, for example, an Integer, a Double
 * or a double array. 
 * <p> 
 * Created on 20/02/2007, 9:58:24 AM
 * <p>
 * Last modified 17/04/2007, 9:58:24 AM
 *
 * @author nxi
 * @version V1.0
 * @since M1
 * @see Out
 * @see Var 
 */
public interface In extends Port {
	
	
	/*
	 * Returen the connected producer.
	 *
	public Out<T> getProducer();
	*/
	
	/*
	 * Configure method.
	 */
//	public void configure(Out<T> producer);
	
	/*
	 * Called by connected producer.
	 * This action lock this IN port.
	 *
	public void producerIsReady();
	*/
	
	/*
	 * Release the this IN signal port. 
	 * Called by its parent processor after a successful transfer.
	 * Will release the connected producer.
	 *
	public void release();
	*/
	
}
