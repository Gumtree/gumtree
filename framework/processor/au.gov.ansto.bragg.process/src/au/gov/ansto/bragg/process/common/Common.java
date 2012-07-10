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

package au.gov.ansto.bragg.process.common;

import au.gov.ansto.bragg.base.common.All;
import au.gov.ansto.bragg.process.common.exception.IllegalNameSetException;

/**
 * Common ancestor of classes of au.gov.ansto.bragg.process component. <p>   Reviewing Andrew's code of DRA framework Created on Jan. 19th 2007 The au.gov.ansto.bragg.process and au.gov.ansto.bragg.algorithm packages implement interfaces extending Common interface. <p> The Common interface extends All interface from au.gov.ansto.bragg.base.common package. Common interface cast object id into int type
 * @author  nxi
 * @version  V1.0
 * @since  M1
 */
public interface Common extends All {
	
	/**
	 * Default name of a Common instance
	 */
	public final static String UNDEFINED_NAME = "Undefined";
	
	/**
	 * Get object id. This mehtod override the getID() method in 
	 * au.gov.ansto.bragg.base.common.All class in order to return 
	 * an int type value.
	 * @return id in int type
	 * @see au.gov.ansto.bragg.base.common.All#is()
	 */
	public int getID();
	
	/**
	 * This mehod return a name string of the Common instance.
	 * @return name in String
	 */
	public String getName();
	
	/**
	 * This method return a timestamp of the instance at which last.
	 * accessed.
	 * @return timestamp in Long
	 */
	public Long getTimestamp();
	
	/**
	 * This method set a String value to the name of the Common instance.
	 * @param name  in String
	 * @throws IllegalNameSetException  invalid name
	 */
	public void setName(final String name) throws IllegalNameSetException;
	
	/**
	 * This method set a time stamp when the instance is accessed.
	 * @param timestamp  in Long instance
	 * @throws IllegalArgumentException  invalid argument
	 */
	public void setTimestamp(final Long timestamp) throws IllegalArgumentException;
}
