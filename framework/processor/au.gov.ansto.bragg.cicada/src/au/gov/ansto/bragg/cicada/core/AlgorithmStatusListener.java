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

import au.gov.ansto.bragg.cicada.core.Algorithm.AlgorithmStatus;

/**
 * @author nxi
 * Created on 08/09/2008
 */
public interface AlgorithmStatusListener {

	public void onStatusChanged(AlgorithmStatus status);
	public void setStage(int operationIndex, AlgorithmStatus status);
}
