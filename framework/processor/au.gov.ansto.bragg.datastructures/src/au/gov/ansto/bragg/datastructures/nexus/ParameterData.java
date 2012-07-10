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
package au.gov.ansto.bragg.datastructures.nexus;

import java.util.List;

/**
 * @author nxi
 * Created on 19/05/2009
 */
public class ParameterData {

	private String name;
	private List<Object> values;
	private boolean isChanged = false;
	
	public ParameterData(String name){
		
	}
}
