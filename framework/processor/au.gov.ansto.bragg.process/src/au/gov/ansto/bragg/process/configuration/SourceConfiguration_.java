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

public class SourceConfiguration_ extends ProcessorConfiguration_ implements
		SourceConfiguration {
	public final static long serialVersionUID = 1L;
	public SourceConfiguration_(final String name, final String parentName, final String classType	, final String methodName){
		super(name, parentName, classType, methodName);
	}
}
