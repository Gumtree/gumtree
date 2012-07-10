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
package au.gov.ansto.bragg.process.port;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import au.gov.ansto.bragg.process.configuration.VarConfiguration;
import au.gov.ansto.bragg.process.exception.ProcessFailedException;
import au.gov.ansto.bragg.process.exception.ProcessorChainException;
import au.gov.ansto.bragg.process.processor.Processor;

public class Var_ extends Port_ implements Var {

	public enum Usage{option, parameter, region, hidden};
	public static final long serialVersionUID = 1L;

//	private Var<T> producer = null;
//	private List<Var<T>> consumerList;
//	protected int numberOfConsumer = 0;
	protected Object defaultValue = null;
	protected int ownerID = 0;
	protected Object max = null;
	protected Object min = null;
	protected int UIWidth = 0;
	protected Usage usage = Usage.parameter;
	protected Tuner tuner = null;
	protected String label = null;
	protected List options;
	protected Map<String, String> properties;

	public Var_(final VarConfiguration configuration, final Processor parent) 
	throws ProcessorChainException {
		super(configuration, parent);
		try {
			setDefaultValue(configuration.getDefaultValue());
		} catch (Exception e) {
			throw new ProcessorChainException("failed to set default value to the port " + 
					configuration.getName() + ": " + e.getMessage(), e);
		} 
		if (configuration.getOwner() != 0) setOwner(configuration.getOwner());
		if (configuration.getMax() != null) setMaxInString(configuration.getMax());
		if (configuration.getMin() != null) setMinInString(configuration.getMin());
		if (configuration.getUIWidth() != null)
			try {
				setUIWidth(configuration.getUIWidth());
			} catch (Exception e) {
			}
		if (configuration.getUsage() != null) setUsage(configuration.getUsage());
		if (configuration.getLabel() != null) setLabel(configuration.getLabel());
		else setLabel(getCoreName());
		if (configuration.getOptions() != null) setOptions(configuration.getOptions());
		if (configuration.getProperties() != null)
			setProperties(configuration.getProperties());
	}

	private void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	private void setUIWidth(String width) {
		UIWidth = Integer.valueOf(width);
	}

	private void setOptions(String text) throws ProcessorChainException {
		options = new ArrayList<Object>();
		if (text.isEmpty())
			return;
		String[] words = text.split(",");
		for (int i = 0; i < words.length; i++) {
			words[i] = words[i].trim();
			Constructor<?> constructor = null;
			try {
				constructor = type.getConstructor(new Class[]{String.class});
			} catch (Exception e) {
				throw new ProcessorChainException("failed to set option values to the port " + getName() + ": "
						+ "the type " + type + " does not have a string constructor." + e.getMessage(), e);
			} 
			try {
				options.add(constructor.newInstance(new Object[]{words[i]}));
			} catch (Exception e) {
				throw new ProcessorChainException("failed to set option value to the port " + getName() + ": "
						+ "can not generate a " + type + " with string value " + words[i] + ", " + 
						e.getMessage(), e);
			}

			
		}
	}

	public String getPortType(){
		return "var";
	}

	protected void setOwner(int ownerID){
		this.ownerID = ownerID;
	}

	protected void setMaxInString(String maxString) throws ProcessorChainException 
	{
		Constructor<?> constructor = null;
		try {
			constructor = type.getConstructor(String.class);
		} catch (Exception e) {
			throw new ProcessorChainException("failed to set maximum value to the port " + getName() + ": "
					+ "the type " + type + " does not have a string constructor." + e.getMessage(), e);
		} 
		try {
			this.max = constructor.newInstance(maxString);
		} catch (Exception e) {
			throw new ProcessorChainException("failed to set maximum value to the port " + getName() + ": "
					+ "can not generate a " + type + " with string value " + maxString + ", " + 
					e.getMessage(), e);
		}
	}
	
	public void setMax(Object value){
		max = value;
	}

	protected void setMinInString(String minString) throws ProcessorChainException{
		Constructor<?> constructor = null;
		try {
			constructor = type.getConstructor(String.class);
		} catch (Exception e) {
			throw new ProcessorChainException("failed to set minimum value to the port " + getName() + ": "
					+ "the type " + type + " does not have a string constructor." + e.getMessage(), e);
		} 
		try {
			this.min = constructor.newInstance(minString);
		} catch (Exception e) {
			throw new ProcessorChainException("failed to set minimum value to the port " + getName() + ": "
					+ "can not generate a " + type + " with string value " + minString + ", " + 
					e.getMessage(), e);
		}
	}

	public void setMin(Object value){
		min = value;
	}

	protected void setUIWidth(int UIWidth){
		this.UIWidth = UIWidth;
	}
	public int getOwnerID(){
		return ownerID;
	}

	public Object getMax(){
		return max;
	}
	
	public Object getMin(){
		return min;
	}
	
	protected void setDefaultValue(String defaultValue) throws ProcessorChainException, ProcessFailedException
	{
		if (defaultValue == null) {
			this.defaultValue = null;
			signal = null;
			return;
		}
		if (defaultValue.equals("null") || defaultValue.matches("")){
//			System.out.println("Please setup value for port_var_" + this.getName());
			this.defaultValue = null;
			signal = null;
		}else{
			Constructor<?> constructor = null;
			try {
				constructor = type.getConstructor(String.class);
			} catch (Exception e) {
				throw new ProcessorChainException("failed to set option values to the port " + getName() + ": "
						+ "the type " + type + " does not have a string constructor." + e.getMessage(), e);
			} 
			try {
				this.defaultValue = constructor.newInstance(defaultValue);
			} catch (Exception e) {
				throw new ProcessorChainException("failed to set option value to the port " + getName() + ": "
						+ "can not generate a " + type + " with string value " + defaultValue + ", " + 
						e.getMessage(), e);
			}

			setSignal(this.defaultValue);
		}
	}

	public Object getDefaultValue(){
		return defaultValue;
	}

	/*
	protected void DownLink(Var<T> consumer){
		try{
			if (consumer == null){
				throw new IllegalArgumentException("No Var port.");
			}
			add(consumer);
		}catch(IllegalArgumentException ex){
			throw new IllegalArgumentException(ex.getMessage() + "Down link VAR failed.");
		}
	}*/

	/*
	protected void add(Var<T> consumer){
		try{
			if (consumer == null){
				throw new IllegalArgumentException("No Var port.");
			}
			consumerList.add(consumer);
			numberOfConsumer++;
		}catch(IllegalArgumentException ex){
			throw new IllegalArgumentException(ex.getMessage() + "Down link VAR failed.");
		}
	}*/

	/*
	 * Configure method.
	 *
	public void configure(){

	}*/






	/*
	 * Inform the parent processor that variable is changed,
	 * by calling tokenSignalReady() of its parent processor.
	 * This action may active the processing of the processor.
	 */
	/* 
	 Disabled. The variable will not triger the processing.
	protected void informParent(boolean inPortStatus){
		this.parent().tokenSignalReady();
	}
	 */

	public String toString(){
		String result = "<var>\n" + super.toString();
		result += "<default_value>" + getDefaultValue() + "</default_value>\n"; 
		if (getMin() != null) result += "<min>" + getMin() + "</min>\n";
		if (getMax() != null) result += "<max>" + getMax() + "</max>\n";
		if (options != null && options.size() > 0){
			result += "<options>\n";
			for (Iterator<?> iterator = options.iterator(); iterator.hasNext();) {
				result += "<option>" + iterator.next() + "</option>\n";
			}
			result += "</options>\n";
		}
		if (getLabel() != null) result += "<label>" + getLabel() + "</label>\n";
		if (usage != null) result += "<option>" + usage +"</usage>\n";
		result += "</var>\n";
		return result;
	}

	public Tuner getTuner() {
		if (getProducer() != null){
			if (getProducer() instanceof Var) return ((Var) getProducer()).getTuner();
		}
		return tuner;
	}

	public void setTuner(Tuner tuner) {
		this.tuner = tuner;
	}

	public String getUsage() {
		return usage.toString();
	}

	protected void setUsage(String usage) {
		try {
			this.usage = Usage.valueOf(usage);	
		} catch (Exception e) {
			this.usage = Usage.parameter;
		}
	}
	
	protected void setParentField() throws ProcessorChainException{
		parent.setField(getCoreName(), signal, type);
	}

	public Object getParentField() throws ProcessorChainException {
		return parent.getField(getCoreName());
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<?> getOptions() {
		return options;
	}
	
	public void setOptions(List<?> options){
		this.options = options;
	}
	
	protected boolean isValueAnOption(Object value){
		if (options == null || options.size() == 0)
			return true;
		for (Iterator<?> iterator = options.iterator(); iterator.hasNext();) {
			Object option = (Object) iterator.next();
			if (option.toString().equals(value.toString()))
				return true;		
		}
		return false;
	}
	
	@Override
	protected void setSignal(Object signal) throws ProcessorChainException, ProcessFailedException {
//		if (isValueAnOption(signal))
		if (true)
			super.setSignal(signal);
		else 
			throw new ProcessorChainException(signal + " is not an option");
	}

	public void updateValue(Object signal){
		this.signal = signal;
		try {
			informConsumer();
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			throw new ProcessorChainException("failed to inform consumer");
		} 
	}
	
	public int getUIWidth() {
		return UIWidth;
	}
	
	public void addVarPortListener(TunerPortListener listener){
		getParent().addTunerPortListener(getCoreName(), listener);
		for (Port port : getConsumerList()){
			if (port instanceof Var)
				((Var) port).addVarPortListener(listener);
		}
	}
	
	public void removeVarPortListener(TunerPortListener listener){
		getParent().removeTunerPortListener(getCoreName(), listener);
		for (Port port : getConsumerList()){
			if (port instanceof Var)
				((Var) port).removeVarPortListener(listener);
		}
	}

	@Override
	public String getProperty(String propertyName) {
		if (properties == null)
			return null;
		return properties.get(propertyName);
	}
}
