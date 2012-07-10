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
package au.gov.ansto.bragg.process.agent;


/**
 * A child of Agent. Which represents a Port object. 
 * <p>
 * The principal of the agent is a Port instance.
 * <p>
 * Created on 20/02/2007, 4:58:43 PM
 * <p>
 * Last modified 20/02/2007, 4:58:43 PM
 * @author nxi
 * @version M1
 * @since V1.0
 * @see Agent
 */
public interface PortAgent extends Agent {

	/**
	 * This method get the lock status of the principal of the agent, that is
	 * the lock status of a Port instance.
	 * @return lock status in boolean type
	 * @since V1.0
	 */
	public boolean getLockStatus();
	
	/**
	 * Get the signal flag of the principal of the agent, which is the 
	 * flag status of a Port instance.
	 * @return flag information in boolean type
	 * @since V1.0
	 * @deprecated in M1
	 */
	public boolean getSignalFlag();
}
