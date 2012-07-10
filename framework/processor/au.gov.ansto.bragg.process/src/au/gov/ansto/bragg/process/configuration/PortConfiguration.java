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
 * @author  nxi
 */
public interface PortConfiguration extends Configuration {
	
	public final static int DEFAULT_DIMENSION = 0;
	public final static String DEFAULT_TYPE = "port";
	public final static int DEFAULT_PARENTID = 0;
	public final static String DEFAULT_PARENTNAME = "frame";
	
	/*
	 * Get dimension information of the signal
	 */
	public int getDimension();
	
	/*
	 * Get parent name;
	 */
	public String getParentName();
	
	/*
	 * Get the type of the signal 
	 */
	public String getType();
	
	/*
	 * Set the dimension property
	 */
	public void setDimension(final int dimension);
	
	/*
	 * Set the parent id property
	 */
	public void setParentName(final String parentName);
	
	/*
	 * Set the type of the signal
	 */
	public void setType(final String type);
}
