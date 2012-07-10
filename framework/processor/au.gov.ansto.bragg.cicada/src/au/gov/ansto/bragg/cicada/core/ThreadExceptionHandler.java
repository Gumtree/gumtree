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
package au.gov.ansto.bragg.cicada.core;

/**
 * Extend this class to catch exceptions thrown by algorithm thread. 
 * Subscribe an instance to the algorithm manager at the runtime. 
 * @author nxi
 *
 */
public interface ThreadExceptionHandler {

	public void catchException(Algorithm algorithm, Exception e);
	
}
