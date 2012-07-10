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
package au.gov.ansto.bragg.process.configuration;

import java.util.Map;


/**
 * @author  nxi
 */
public interface VarConfiguration extends PortConfiguration {

	/*
	 * Add one consumer to the comsumer list
	 *
	public void addConsumerID(final int consumerID);
	
	/*
	 * Get the consumer list
	 *
	public List<Integer> getConsumcerIDList();
	*/
	
	/*
	 * Get the default value property of the port
	 */
	public String getDefaultValue();
	
	/*
	 * Get the maximum limitation of the port
	 */
	public String getMax();

	/*
	 * Get the minimum limitation of the port
	 */	
	public String getMin();
	
	/*
	 * Get the owner information of the port
	 * The owner of the port is a simple processor
	 */
	public int getOwner();
	
	/*
	 * Get the producer of the port which pass signal to this
	 *
	public int getProducerID();
	
	/*
	 * Set the producer ID
	 *
	public void setProducerID(final int producerID);
	*/
	
	public String getUsage();
	
	public void setUsage(String usage);
	
	public String getLabel();

	/**
	 * Get options string for the var port
	 * @return String type
	 * Created on 14/04/2008
	 */
	public String getOptions();
	public String getUIWidth();

	public Map<String, String> getProperties();
}
