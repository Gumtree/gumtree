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

import java.util.List;

public class OutConfiguration_ extends PortConfiguration_ implements
		OutConfiguration {

	public final static long serialVersionUID = 1L;
	
	protected List<Integer> consumerIDList;
	
	public OutConfiguration_(String name, int dimension, String type, String parentName) {
		super(name, dimension, type, parentName);
	}

	/*
	public void addConsumerID(final int consumerID) {
		// TODO Auto-generated method stub
		consumerIDList.add(consumerID);
	}

	public List<Integer> consumerIDList() {
		// TODO Auto-generated method stub
		return consumerIDList;
	}
	*/

	public String toString(){
		String result = "<out_configuration>\n";
		result += super.toString();
		result += "</out_configuration>\n";
		return result;
	}

}
