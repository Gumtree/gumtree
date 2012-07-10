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
package au.gov.ansto.bragg.process.signal;

import au.gov.ansto.bragg.process.common.Common;

/**
 * An interface of the abstract class FrameSignal_. It must be inherited by children class. Its children classes are signal class for different instrument, for example, EchidnaSiganl. <p>   Created on 23/02/2007, 11:58:06 AM Last modified 23/02/2007, 11:58:06 AM
 * @author  nxi
 */
public interface FrameSignal extends Common {

	/**
	 * This method returns the dimension of the signal.
	 * @return dimension of signal as array of long type.
	 */
	public long[] getDimension();
	
	/**
	 * Get the main signal of the group.
	 * @return generic signal type as Object. 
	 */
	public Object getSignal();
	
	/**
	 * Get the type of the signal, which is described in a string object.  
	 * Usallly defined in the configuration file of the signal..
	 * @return type as String 
	 */
	public String getType();
	
	/**
	 * Set the dimension of the signal. 
	 * @param dimension as long value.
	 */
	public void setDimension(long[] dimension);
	
	/**
	 * Set signal of the object.
	 * @param generic signal as Object instance
	 */
	public void setSignal(Object signal);
	
	/**
	 * Set type for the signal. The type of the signal is read from the algorithm
	 * recipe file.                                
	 * @param type as String instance 
	 */
	public void setType(String type);
}
