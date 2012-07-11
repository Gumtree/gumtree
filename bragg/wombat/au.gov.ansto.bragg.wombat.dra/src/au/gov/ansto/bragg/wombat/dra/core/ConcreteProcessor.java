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
package au.gov.ansto.bragg.wombat.dra.core;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;

/**
 * The abstract concreate processor.
 * @author nxi
 *
 */
public interface ConcreteProcessor {

//	public List<Object> process(Object ...objects );
	public Boolean process() throws Exception;

	public DataStructureType getDataStructureType();
	
	public DataDimensionType getDataDimensionType();
	
}
