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
package au.gov.ansto.bragg.cicada.dom.core;

import au.gov.ansto.bragg.cicada.core.exception.ConfigurationException;
import au.gov.ansto.bragg.cicada.core.exception.LoadAlgorithmFileFailedException;

public class CicadaDOMFactory  {

	public CicadaDOMFactory() {
		// TODO Auto-generated constructor stub
		super();
	}

	public Object getDOMroot() {
		// TODO Auto-generated method stub
		Object cicadaDOM = null;
		try {
			cicadaDOM = new CicadaDOM();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LoadAlgorithmFileFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cicadaDOM;
	}

	public static Object getCicadaDOM(){
		Object cicadaDOM = null;
		try {
			cicadaDOM = new CicadaDOM();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LoadAlgorithmFileFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cicadaDOM;
	}
}
