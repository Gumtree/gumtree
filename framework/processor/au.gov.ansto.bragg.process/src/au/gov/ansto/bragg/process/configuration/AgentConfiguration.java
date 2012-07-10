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
 * @author  nxi  Created on 23/02/2007, 2:23:43 PM  Last modified 23/02/2007, 2:23:43 PM
 */
public interface AgentConfiguration extends Configuration {

	/*
	 * Get the principal name property
	 */
	public String getPrincipal();
	
	/*
	 * Get the receipe ID of the principal
	 */
	public String getPName();
	
	/*
	 * Get the description property;
	 */
	public String getDescription();
}
