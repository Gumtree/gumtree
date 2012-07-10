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

public class InConfiguration_ extends PortConfiguration_ implements
		InConfiguration {

	public final static long serialVersionUID = 1L;
	
	protected int producerID;
	
	public InConfiguration_(String name, int dimension, String type, String parentName) {
		super(name, dimension, type, parentName);
	}

	/*
	public int getProducerID() {
		// TODO Auto-generated method stub
		return producerID;
	}

	public void setProducerID(final int producerID) {
		// TODO Auto-generated method stub
		this.producerID = producerID;
	}
	*/
	
	public String toString(){
		String result = "<in_configuration>\n";
		result += super.toString();
		result += "</in_configuration>\n";
		return result;
	}

}
