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

import au.gov.ansto.bragg.process.processor.Sink;

/**
 * A child class of Port to output signal for processors. 
 * <p>
 * An Out port is build upon a Processor object to output signals for the processor.
 * After the processing of a processor, it will sent the output signal to its Out
 * ports. Then the Out port will pass it other In ports or Out 
 * ports in its consumer list.
 * <p> 
 * When an Out port object is created, it is configured to be able
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
 * @see In
 * @see Var 
 */

public interface Out extends Port {

	/*
	 * Set cache signal of this OUT port.
	 * Called by its parent processor.
	 *
	public void setCach(T signal);
	*/

	/*
	 * Connect to its consumer.
	 * Called by configuration method.
	 *
	public void connect(In<T> consumer);
	*/
	
	/* 
	 * Release a single consumer token hold by this OUT port.
	 * Called by all of its consumers.
	 *	
	public void releaseToken();
	*/
	
	/**
	 * Return the sink that catch the out put from this out port.
	 * @return sink object
	 */
	public Sink getSink();
}
