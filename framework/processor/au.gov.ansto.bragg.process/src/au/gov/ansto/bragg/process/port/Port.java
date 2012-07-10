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

import au.gov.ansto.bragg.process.common.Common;
import au.gov.ansto.bragg.process.configuration.PortConfiguration;
import au.gov.ansto.bragg.process.exception.ProcessFailedException;
import au.gov.ansto.bragg.process.exception.ProcessorChainException;
import au.gov.ansto.bragg.process.processor.Processor;
/**
 * Generic port class.  <p> A Port can maintain and pass signal. The children classs of Port can be  In, Out and Var port class. Togather they grouped as the necessary accessment  to a Processor. When a Port object is created, it is configured to be able to take certain type of signal information, for example, an Integer, a Double or a double array.  <p>  Created on 20/02/2007, 9:58:24 AM <p> Last modified 17/04/2007, 9:58:24 AM
 * @author  nxi
 * @version  V1.0
 * @since  M1
 * @see In
 * @see Out
 * @see  Var
 */
public interface Port extends Common {

	/**
	 * Add a consumer to the consumer list. A consumer of this port is another port
	 * to which this port will pass the signal handle, if the signal is changed.  
	 * @param consumer  another Port instance
	 */
	public void addConsumer(Port consumer);
	
	/**
	 * A token will be created if this method is called.
	 * The port becomes ready if all token are released.
	 * @see #releaseToken()
	 */
	public void captureToken();
	
	/**
	 * This method configure the port with a port configuration object. It will update
	 * the port's name, type, dimension and so on.
	 * @param portConfiguration  a port configuration object
	 * @param parent  a processor in which this port is built, as Processor 
	 * @throws ProcessorChainException 
	 */
	public void configure(final PortConfiguration portConfiguration, final Processor parent) 
	throws ProcessorChainException;
	
	/**
	 * This method gets the consumer list of the port. The consumer list contains
	 * all consumers of this port. If the signal information is updated, the port
	 * will pass the signal to all its consumers one by one.
	 * @return list of ports as List<Port>
	 */
	public List<Port> getConsumerList();
	//	public int getNumber();
	
	/**
	 * Get the dimension information of the port. It is the dimension of the signal
	 * of the port.
	 * @return dimension as int type
	 */
	public int getDimension();
	
	/**
	 * Get the lock status of the port. If the port's information is used for a running
	 * processing of its parent processor, it is locked. After processing, the port is
	 * unlocked
	 * @return lock status as boolean type
	 */
	public boolean getLockStatus();
	
	/**
	 * Get the parent of the port. 
	 * The parent of the port is a processor, in which the port is physically built.
	 * @return a Processor instance
	 */
	public Processor getParent();
	
	/**
	 * Get the signal type of the port. The signal of the port is defined as generic 
	 * signal as Object. When the port is created upon a port configuration object,
	 * it will be initialized as a specific type that is defined in the recipe file.
	 * So the port type can be described as a String
	 * @return port type as String
	 */
	public String getPortType();
	
	/**
	 * Get the producer of the port.
	 * A producer of the port is another port which pass its signal to this port.
	 * A port can have only one producer.
	 * @return producer as Port instance
	 */
	public Port getProducer();
	
	
	//	public int getID();
	
	/**
	 * Get the port recipe id.
	 * Receipe id is the fixed id which is parsed from the XML recipe file.
	 * In a recipe file, every port has a unique recipe id.
	 * @return recipe id in int type
	 */
	//public int getRecipeID();
	
	/**
	 * Get the signal of the port. A generic signal in Object type is used here.
	 * The actual signal is in the type defined in the recipe file.
	 * @return generic signal in Object type 
	 */
	public Object getSignal();
	
	/**
	 * Get the lock status of the port. This method is usually called by its
	 * proxy, that is an PortAgent object.
	 * @return lock status converted to String type
	 */
	public String getStatus();
	
	/**
	 * This method gets the type of the signal of the port. It is defined in the
	 * recipe file as a string. When the port is created, it will be parsed by
	 * a ClassLoader to a class type.
	 * @return a Class type
	 */
	public Class<?> getType();
	
	/**
	 * Release a token of the port. The number of the tokens of the port is the
	 * number of processors that are using this port to do processing. 
	 * The port are unlocked if all tokens are released
	 * @throws ProcessFailedException 
	 * @throws ProcessorChainException 
	 */
	public void releaseToken() throws ProcessorChainException, ProcessFailedException;
	
	/**
	 * Set a signal to the cach of the port. A cach is a temporary holder for
	 * the signal if the port is locked.
	 * <p>
	 * If the port is locked, the cach will hold the signal tempararily.
	 * If the port is not locked, it will pass it to the signal property of the port.
	 * 	 
	 * @param signal  generic signal in Object type
	 * @throws ProcessorChainException 
	 * @throws ProcessFailedException 
	 */
	public void setCach(final Object signal) throws ProcessorChainException, ProcessFailedException;
	
	/**
	 * Set the dimension property of the port, that is also the dimension of the signal.
	 * @param dimension in int type
	 */
	public void setDimension(final int dimension);
	
	/**
	 * Set output to this port only, which means 
	 * the signal will not be passed on to its consumer.
	 * The signal stops here.
	 * @param signal  generic signal in Object type
	 * @throws ProcessorChainException 
	 * @throws ProcessFailedException 
	 */
	public void setOutput(final Object signal) throws ProcessorChainException, ProcessFailedException;

	/**
	 * Set the output signal to the consumer. And stop the processing of the chain at this stage. 
	 * @param signal
	 * @throws ProcessorChainException
	 * @throws ProcessFailedException 
	 * Created on 08/04/2009
	 */
	public void setOutputToConsumer(final Object signal) throws ProcessorChainException, ProcessFailedException;

	/**
	 * Set parent of the port.
	 * The parent of the port is a processor.
	 * @param parent processor as Processor object
	 */
	public void setParent(final Processor parent);
	
	/**
	 * Set the producer of the port.
	 * A producer is another port that will pass its signal to this port when 
	 * the signal is changed.
	 * @param producer as Port object
	 */
	public void setProducer(Port producer);
	
	/**
	 * Set the recipe id of the port.
	 * A receipe id is the one get from the XML receipe file.
	 * @param id  recipe id in int type
	 * @see #getRecipeID() 
	 */
//	public void setReceipeID(int id);
	
	/**
	 * Set the type of the signal of the port.
	 * @param type in Class type, which is reflected by a string
	 * @see #getPortType()
	 */
	public void setType(final Class<?> type);
	
	/**
	 * The core name of a port has to be the same as the parameter name in the concrete processor. Then 
	 * the real name of the port can be appending some string and a dot in front of the core name. For
	 * example, if the core name of the port is 'inputPort', then the real name of the port can be 
	 * 'processor1.inputPort'. 
	 * @return String value
	 * Created on 26/11/2008
	 */
	public String getCoreName();
}
