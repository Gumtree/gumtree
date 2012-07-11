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
package au.gov.ansto.bragg.echidna.exp.core;

import au.gov.ansto.bragg.echidna.exp.exception.InitializeCommandException;


/**
 * @author nxi
 *
 */
public interface Command {

	
	public String run();
	
	public void setExperiment(EchidnaExperiment experiment);

	public void setParameter(String ...params) throws InitializeCommandException;
	
	public String getShortDescription();
	
	public String getHelp();
}
