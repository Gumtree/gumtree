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
import java.util.Map;

public class VarConfiguration_ extends PortConfiguration_ implements
		VarConfiguration {

	public final static long serialVersionUID = 1L;
	
	protected int producerID;
	protected String defaultValue = null;
	protected int ownerID = 0;
	protected List<Integer> consumerIDList;
	protected String max = null;
	protected String min = null;
	protected String usage = null;
	protected String label = null;
	protected String UIWidth;
	protected String options;
	protected Map<String, String> properties;
	
	public VarConfiguration_(String name, int dimension, String type, String parentName, 
			final String defaultValue, final int ownerID, final String max, 
			final String min, final String usage, String label, String options, String UIWidth,
			final Map<String, String> properties){
		super(name, dimension, type, parentName);
		setDefaultValue(defaultValue);
		setOwner(ownerID);
		setMax(max);
		setMin(min);
		setUsage(usage);
		setLabel(label);
		setUIWidth(UIWidth);
		this.options = options;
		this.properties = properties;
	}

	private void setUIWidth(String width) {
		// TODO Auto-generated method stub
		this.UIWidth = width;
	}

	public String getUIWidth(){
		return UIWidth;
	}
	/*
	public void addConsumerID(final int consumerID) {
		// TODO Auto-generated method stub
		consumerIDList.add(consumerID);
	}

	public List<Integer> getConsumcerIDList() {
		// TODO Auto-generated method stub
		return consumerIDList;
	}
	*/

	private void setLabel(String label) {
		// TODO Auto-generated method stub
		this.label = label;
	}

	public String getDefaultValue(){
		return defaultValue;
	}

	public String getMax(){
		return max;
	}
	
	public String getMin(){
		return min;
	}
	
	public int getOwner(){
		return ownerID;
	}
	
	/*
	public int getProducerID() {
		// TODO Auto-generated method stub
		return producerID;
	}
	*/
	
	protected void setDefaultValue(String defaultValue){
		this.defaultValue = defaultValue;
	}
	
	protected void setMax(String max){
		this.max = max;
	}
	
	protected void setMin(String min){
		this.min = min;
	}
	
	protected void setOwner(int ownerID){
		this.ownerID = ownerID;
	}
	
	/*
	public void setProducerID(final int producerID) {
		// TODO Auto-generated method stub
		this.producerID = producerID;
	}
	*/

	public String toString(){
		String result = "<var_configuration>\n";
		result += super.toString();
		result += "<min>" + (min == null?"null":min) + "</min>\n";
		result += "<max>" + (max == null?"null":max) + "</max>\n";
		result += "<usage>" + (usage == null?"null":usage) + "</usage>\n";
		result += "</var_configuration>\n";
		return result;
	}

	public String getUsage() {
		return usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	public String getLabel() {
		return label;
	}

	public String getOptions() {
		// TODO Auto-generated method stub
		return options;
	}

	@Override
	public Map<String, String> getProperties() {
		return properties;
	}

}
