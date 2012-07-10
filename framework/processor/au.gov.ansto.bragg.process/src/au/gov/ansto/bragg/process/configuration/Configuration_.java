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

import au.gov.ansto.bragg.process.common.Common_;

public class Configuration_ extends Common_ implements Configuration {
	
	public final static long serialVersionUID = 1L; 

//	protected int receipeID;

	Configuration_(){
		super("configuration");
	}
	
//	Configuration_(String name){
//		this(name);
////		this.receipeID = receipeID;
//	}
	
	Configuration_(String name){
		super(name);
	}
	
//	public int getReceipeID(){
//		return receipeID;
//	}
}
