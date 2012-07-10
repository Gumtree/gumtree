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
import java.util.List;

import au.gov.ansto.bragg.process.common.Common_;
import au.gov.ansto.bragg.process.common.exception.IllegalNameSetException;
import au.gov.ansto.bragg.process.exception.ProcessFailedException;
import au.gov.ansto.bragg.process.exception.ProcessorChainException;

/**
 * @author nxi
 * Created on 21/02/2007, 1:55:04 PM
 * Last modified 21/02/2007, 1:55:04 PM
 * 
 */
public class Tuner_ extends Common_ implements Tuner {

	public static final long serialVersionUID = 1L;

	protected Var consumer = null;
	protected boolean changeFlag = false;
	List<TunerPortListener> listeners;

	public Tuner_(){
		super();
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.process.port.Tuner#getConsumer()
	 */
	protected Var getConsumer() throws NullPointerException{
		if (consumer == null) throw new NullPointerException("null var consumer");
		return consumer;
	}

	public Object getMax() throws NullPointerException{
		return this.getConsumer().getMax();
	}

	public Object getMin() throws NullPointerException{
		return this.getConsumer().getMin();
	}

	public int getOwnerID() throws NullPointerException{
		return getConsumer().getOwnerID();
	}

	public String getUsage(){
		return getConsumer().getUsage();
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.process.port.Tuner#getSignal()
	 */
	public Object getSignal() throws NullPointerException{
		return getConsumer().getSignal();
	}

	public String getType() throws NullPointerException{
		return getConsumer().getType().getName();
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.process.port.Tuner#setConsumer(au.gov.ansto.bragg.process.port.Var)
	 */
	public void setConsumer(Var port) throws NullPointerException, ProcessorChainException{
		if (port == null) 
			throw new NullPointerException("null port");
		consumer = port;
		port.setTuner(this);
		try {
			setName(port.getName());
		} catch (IllegalNameSetException e) {
			throw new ProcessorChainException("illegal name " + port.getName());
		}
//		consumer.addVarPortListener(new TunerPortListener(this){
//
//			@Override
//			public void updateUIMax(final Object max) {
//				consumer.setMax(max);
//			}
//
//			@Override
//			public void updateUIMin(final Object min) {
//				consumer.setMin(min);
//			}
//
//			@Override
//			public void updateUIOptions(final List<?> options) {
//				consumer.setOptions(options);
//			}
//
//			@Override
//			public void updateUIValue(final Object value) {
//				consumer.updateValue(value);
//			}			
//		});
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.process.port.Tuner#setSignal(java.lang.Object)
	 */
	public void setSignal(Object signal) throws ProcessorChainException, ProcessFailedException {
		changeFlag = true;
		System.out.println("Send Signal to port: var_" +
				"_" + getConsumer().getName() + ", value=" + (signal == null ? "null" : signal.toString()));
		getConsumer().setCach(signal);
		if (listeners != null){
			for (TunerPortListener listener : listeners)
				listener.updateUIValue(signal);
		}
	}

	public String toString(){
		String result = "<tuner>\n";
		result += "<id>" + getID() + "</id>\n";
		result += "<name>" + getName() + "</name>\n";
		result += "<consumer>var_" + getConsumer().getID() + "_" + getConsumer().getName() 
		+ "</consumer>\n";
		result += "<usage>" + (getUsage() == null?"null":getUsage()) +"</usage>\n";
		result += "<label>" + getLabel() + "</label>\n";
		result += "</tuner>\n";
		return result;
	}

	public boolean isChanged(){
		return changeFlag;
	}

	public void resetChangeFlag(){
		changeFlag = false;
	}

	public String getLabel(){
//		if (consumer.getLabel() != null) return consumer.getLabel();
		return consumer.getLabel();
	}

	public List<?> getOptions() {
		return consumer.getOptions();
	}

	public void setStringSignal(String tunerValue) throws ProcessorChainException, ProcessFailedException {
		Class<?> type = null;
		try {
			type = getConsumer().getType();	
		} catch (Exception e) {
			throw new ProcessorChainException("can not get type of the tuner " + getName() + ": " +
					e.getMessage(), e);
		}
		Object signal = null;
		if (!tunerValue.toLowerCase().equals("null")){
			Constructor<?> constructor = null;
			try {
				constructor = type.getConstructor(new Class[]{String.class});
			} catch (Exception e) {
				throw new ProcessorChainException("failed to set value to the tuner " + getName() + ": "
						+ "the type " + type + " does not have a string constructor." + e.getMessage(), e);
			} 
			try {
				signal = constructor.newInstance(new Object[]{tunerValue});
			} catch (Exception e) {
				throw new ProcessorChainException("failed to set value to the tuner " + getName() + ": "
						+ "can not generate a " + type + " with string value " + tunerValue + ", " + 
						e.getMessage(), e);
			}
		}
		setSignal(signal);

	}

	public int getUIWidth() {
		return getConsumer().getUIWidth();
	}
	
	public void addVarPortListener(TunerPortListener listener){
		getConsumer().addVarPortListener(listener);
	}
	
	public void removeVarPortListener(TunerPortListener listener){
		getConsumer().removeVarPortListener(listener);
	}

	public void setMax(Object max) {
		getConsumer().setMax(max);
	}

	public void setMin(Object min) {
		getConsumer().setMin(min);
	}

	public void setOptions(List<?> options) {
		getConsumer().setOptions(options);
	}

	public void updateValue(Object value) {
		getConsumer().updateValue(value);
	}

	public String getCoreName() {
		return getConsumer().getCoreName();
	}
	
	public void addChangeListener(TunerPortListener listener){
		if (listeners == null)
			listeners = new ArrayList<TunerPortListener>();
		listeners.add(listener);
	}
	
	public void removeChangeListener(TunerPortListener listener){
		if (listeners != null)
			listeners.remove(listener);
	}

	public boolean isVisible() {
		if (getUsage() != null || getUsage().toLowerCase().equals("hidden"))
			return true;
		return false;
	}

	@Override
	public String getProperty(String propertyName) {
		return getConsumer().getProperty(propertyName);
	}
	
	
}
