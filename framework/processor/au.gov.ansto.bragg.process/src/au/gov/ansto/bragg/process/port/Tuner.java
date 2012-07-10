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

import java.util.ArrayList;
import java.util.List;

import au.gov.ansto.bragg.process.common.Common;
import au.gov.ansto.bragg.process.exception.ProcessFailedException;
import au.gov.ansto.bragg.process.exception.ProcessorChainException;

/**
 * A Tuner port is build upon a processor framework object to tune the Var ports of  the processors in the framework. Since Var ports are not exposed to developer users. User can use a Tuner  object to access and tune the Var port.  <p>  When an Tuner port object is created, it is configured to be able to take certain type of signal information, for example, an Integer, a Double or a double array.  
 * <p>  Created on 20/02/2007, 9:58:24 AM <p> Last modified 17/04/2007, 9:58:24 AM
 * @author  nxi
 * @version  V2.4
 * @since  M1
 * @see  Var
 */

public interface Tuner extends Common{
	
	/**
	 * Get the maximum limitation of the the tuner signal, which is got from 
	 * its target Var port.
	 * @return generic maximum value in Object type 
	 * @throws NullPointerException
	 * @since V1.2
	 */
	public Object getMax() throws NullPointerException;

	/**
	 * Get the minimum limitation of the the tuner signal, which is got from 
	 * its target Var port.
	 * @return generic minimum value in Object type
	 * @throws NullPointerException
	 * @since V1.2
	 */
	public Object getMin() throws NullPointerException;
	
	/**
	 * Get the owner id of the tuner.
	 * The owner of the tuner is a processor that finally use the parameter
	 * provided in the signal.
	 * @return owner id in int type
	 * @throws NullPointerException
	 * @since V1.0
	 */
	public int getOwnerID() throws NullPointerException;
	
	/**
	 * Get the signal of the tuner.
	 * @return generic signal in Object type
	 * @throws NullPointerException
	 * @since V1.0
	 */
	public Object getSignal() throws NullPointerException;
	
	/**
	 * Get the signal type of the tuner
	 * @throws NullPointerException
	 * @since V1.0
	 */
	public String getType() throws NullPointerException;
	
	/**
	 * This method retrieve the usage of the tuner in the client side, which is specified
	 * in the recipe file. The cases of the usage are either option or parameter.
	 * @return usage information in String type
	 * @since V2.4
	 */
	public String getUsage();
	
	/**
	 * Return true if the tuner has been set before the processor chain is processed. Once
	 * the processing is done, the change flag will be set to false.
	 * @return true or false
	 */
	public boolean isChanged();
	
	/**
	 * Reset the change flag to false. When means the tuner has not been set before the next 
	 * processing.
	 */
	public void resetChangeFlag();
	
	/**
	 * Get the consumer of the tuner. 
	 * The consumer of a tuner is a VAR port.
	 * @param port as a Var port
	 * @throws NullPointerException
	 * @throws ProcessorChainException 
	 * @since V2.0
	 */
	public void setConsumer(Var port) throws NullPointerException, ProcessorChainException;
	
	/**
	 * Set the signal of the tuner.
	 * The tuner will pass the signal to its consumers, that are Var ports.
	 * @param signal  generic signal in Object type
	 * @throws ProcessFailedException 
	 * @throws ProcessorChainException 
	 * @since V1.0
	 */
	public void setSignal(Object signal) throws ProcessorChainException, ProcessFailedException;
	
	/**
	 * Get the label of the tuner. If the tuner doesn't have a label attribute in the recipe,
	 * it will return the tuner name instead. 
	 * @return object in String type
	 */
	public String getLabel();

	/**
	 * Get the valid options of the value that can be set to the tuner.
	 * @return object list
	 * Created on 15/04/2008
	 */
	public List<?> getOptions();

	/**
	 * Get the tuner signal in String type. 
	 * @param tunerValue a String value.
	 * Created on 26/11/2008
	 * @throws ProcessFailedException 
	 * @throws ProcessorChainException 
	 */
	public void setStringSignal(String tunerValue) throws ProcessorChainException, ProcessFailedException;

	/**
	 * Get the UI width property that specified in the recipe file.
	 * @return an integer
	 * Created on 26/11/2008
	 */
	public int getUIWidth();

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
	 * Update the var port value accordingly. 
	 * @param value Object value
	 * Created on 26/11/2008
	 */
	public void updateValue(Object value);

	/**
	 * Set the maximum value for the tuner.
	 * @param max Object instance
	 * Created on 08/04/2009
	 */
	public void setMax(Object max);
	
	/**
	 * Set the minimum value for the tuner.
	 * @param min Object instance
	 * Created on 08/04/2009
	 */
	public void setMin(Object min);

	/**
	 * Set the option list for the tuner.
	 * @param options in List
	 * Created on 08/04/2009
	 */
	public void setOptions(List<?> options);

	/**
	 * Get the core name of the tuner. The core name should be the save as the property in the 
	 * concrete processor.  
	 * @return String object
	 * Created on 08/04/2009
	 */
	public String getCoreName();

	/**
	 * Add listener listening to signal change.
	 * @param listener {@link TunerPortListener}
	 */
	public void addChangeListener(TunerPortListener listener);
	
	/**
	 * Remove listener that listening to signal change. 
	 * @param listener {@link TunerPortListener}
	 */
	public void removeChangeListener(TunerPortListener listener);
	
	/**
	 * Check if the tuner is visible for UI
	 * @return true or false
	 */
	public boolean isVisible();

	/**
	 * Get the property value from the name
	 * @param propertyName in String type
	 * @return a String object
	 */
	public String getProperty(String propertyName);
}
