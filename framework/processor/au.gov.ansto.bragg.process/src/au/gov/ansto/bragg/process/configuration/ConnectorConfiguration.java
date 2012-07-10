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

/**
 * @author nxi
 * Created on 05/02/2007, 1:01:31 PM
 * Last modified 05/02/2007, 1:01:31 PM
 * 
 */
public interface ConnectorConfiguration extends Configuration {
	
	/*
	 * Get consumer id 
	 */
	public String getConsumer();
	
	/*
	 * Get producer id
	 */
	public String getProducer();
	
	/*
	 * Set consumer id
	 * taking an int type parameter
	 */
	public void setConsumer(final String consumerID);
	
	/*
	 * Set producer id
	 * taking an int type parameter
	 */
	public void setProducer(final String producerID);

}
