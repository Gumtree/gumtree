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


public class ConnectorConfiguration_ extends Configuration_ implements
		ConnectorConfiguration {

	public final static long serialVersionUID = 1L;
	
	protected String producerID;
	protected String consumerID;
	
	public ConnectorConfiguration_(String name, final String producerID, final String consumerID) {
		super(name);
		setConsumer(consumerID);
		setProducer(producerID);
		// TODO Auto-generated constructor stub
	}

	public String getConsumer(){
		return consumerID;
	}
	
	public String getProducer(){
		return producerID;
	}

	public void setConsumer(final String consumerID){
		this.consumerID = consumerID;
	}
	
	public void setProducer(final String producerID){
		this.producerID = producerID;
	}
	
	public String toString(){
		String result = "<connector_configuration>\n"; 
		result += super.toString();
		result += "<producer_id>" + getProducer() + "</producer_id>\n";
		result += "<consumer_id>" + getConsumer() + "</consumer_id>\n";
		result += "</connector_configuration>\n";
		return result;
	}
}
