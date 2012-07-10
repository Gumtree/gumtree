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

import java.util.List;

/**
 * A child class of Port to change the parameters of processors.  <p> A Var port is build upon a Processor object to change parameter for the processor. Var ports are not exposed to developer users. However user can use a tuner  object to access and tune the Var port.  After the Var port is tuned, it will pass the signal to other Var ports in its  consumer list. <p>  When an Var port object is created, it is configured to be able to take certain type of signal information, for example, an Integer, a Double or a double array.  <p>  Created on 20/02/2007, 9:58:24 AM <p> Last modified 17/04/2007, 9:58:24 AM
 * @author  nxi
 * @version  V1.0
 * @since  M1
 * @see In
 * @see Out  
 * @see  Tuner
 */

public interface Var extends Port {

	/*
	 * Configure method.
	 */
//	public void configure();
	
	/*
	 * Set cache signal of this OUT port.
	 * Called by its parent processor.
	 *
	public void setCach(T signal);
	 */
	
	/**
	 * Capture one token of the VAR port. If a Var port has tokens held by others, 
	 * it is in the locked status.
	 */
	public void captureToken();
	
	/* Releases one token of the producer. 
	 * (non-Javadoc)
	 * @see au.gov.ansto.bragg.process.port.Out#releaseToken()
	 */	
//	public void releaseToken();
	
	/**
	 * Get the owner id of the var port.
	 * The owner of the port is the deepest processor that will take the parameter
	 * in the Var port signal. The owner should not be a composite processor or
	 * a processor framework. It should always be a simple processor.
	 * @return owner id in int type 
	 */
	public int getOwnerID();
	
	/**
	 * Get the maximum limitation of the signal. It is a value specified in the
	 * algorithm recipe file. Some Var port may not have a maximum limitation.
	 * @return generic maximum value in Object type 
	 */
	public Object getMax();

	/**
	 * Set a maximum limit to the var port. 
	 * @param max an Object which must be in the same type as the port can hold
	 * Created on 26/11/2008
	 */
	public void setMax(Object max);
	
	/**
	 * Get the minimum limitation of the signal. It is a value specified in the
	 * algorithm recipe file. Some Var port may not have a minimum limitation.
	 * @return generic minimum value in Objcet type
	 */
	public Object getMin();
	
	/**
	 * Set a minimum limit to the var port. 
	 * @param min an Object which must be in the same type as the port can hold
	 * Created on 26/11/2008
	 */
	public void setMin(Object min);

	/**
	 * Set the options for the var port. The options is a list of Object that can be pick by the
	 * var port. 
	 * @param options a list of Object
	 * Created on 26/11/2008
	 */
	public void setOptions(List<?> options);
	
	/**
	 * Get the tuner for this Var port. If the Var port does not have a tuner, return null.
	 * @return Tuner object
	 * @since V2.3
	 */
	public Tuner getTuner();

	/**
	 * Set a tuner for the Var port. 
	 * @param tuner in the type of Tuner.
	 * @since V2.3 
	 */
	public void setTuner(Tuner tuner);
	/*
	 * Get producer of the VAR port.
	 *
	public Var<T> getProducer();
	*/
	public String getUsage();
	/*
	 * Get the consumer list of the VAR port.
	 *
	public List<Var<T>> getConsumerList();
	*/
	
	/**
	 * Get UI Label of the var port.
	 * @return label as String type
	 */
	public String getLabel();
	
	/**
	 * Get the options set to the Var port.
	 * @return list of objects
	 * Created on 14/04/2008
	 */
	public List<?> getOptions();

	/**
	 * Get the UI width attribute of the Var port.
	 * @return integer value
	 * Created on 08/09/2008
	 */
	public int getUIWidth();
	
	/**
	 * Simply update the signal in the abstract processor. This will not trigger to update the
	 * concrete processor field. This function is called when the field of the concrete processor
	 * get updated first. 
	 * @param signal Object value
	 * Created on 26/11/2008
	 */
	public void updateValue(Object signal);
	
	/**
	 * Add a var port listener to the parent processor.
	 * @param listener in VarPortListener type
	 * Created on 26/11/2008
	 */
	public void addVarPortListener(TunerPortListener listener);
	
	/**
	 * Remove a var port listener from the parent processor.
	 * @param listener in VarPortListener type
	 * Created on 26/11/2008
	 */
	public void removeVarPortListener(TunerPortListener listener);

	/**
	 * Get the property value from a given name. 
	 * @param propertyName in String type
	 * @return a String object
	 */
	public String getProperty(String propertyName);

	/**
	 * Get the default value for the var port.
	 * @return Object
	 */
	public Object getDefaultValue();
}
