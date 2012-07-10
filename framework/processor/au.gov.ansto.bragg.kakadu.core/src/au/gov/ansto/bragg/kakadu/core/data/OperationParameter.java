/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.core.data;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.plot.Position;
import au.gov.ansto.bragg.datastructures.core.plot.StepDirection;
import au.gov.ansto.bragg.kakadu.core.Util;
import au.gov.ansto.bragg.process.exception.ProcessFailedException;
import au.gov.ansto.bragg.process.exception.ProcessorChainException;
import au.gov.ansto.bragg.process.port.Tuner;
import au.gov.ansto.bragg.process.port.TunerPortListener;

/**
 * The class describes one operation parameter.
 * Perameter's name, type and default value can be defined 
 * only in time of creation of the parameter and cannot be
 * redefined.
 * <p>Operation Parameters are used for definition of values 
 * which are going to be used in process of operation running. 
 * 
 * @author Danil Klimontov (dak)
 */
public class OperationParameter 
//implements IOperationParameter 
{

	protected final List<OperationParameterListener> operationParameterListeners = new ArrayList<OperationParameterListener>();

	protected Tuner tuner;
	private String name;
	private String uiLabel;
	protected Object value;
	private Object defaultValue;
	private OperationParameterType type;
	private List<?> options;
	
	protected boolean isChanged = false;
	protected Object oldValue;
	private Class<?> parameterValueClass;
	
	
	
	
	public OperationParameter(Tuner tuner) {
		this.tuner = tuner;
		
		initOperationParameter();
		tuner.addVarPortListener(new TunerPortListener(tuner){
		
					@Override
					public void updateUIMax(final Object max) {
					}
		
					@Override
					public void updateUIMin(final Object min) {
					}
		
					@Override
					public void updateUIOptions(final List<?> options) {
					}
		
					@Override
					public void updateUIValue(final Object signal) {
						value = signal;
					}			
				});
	}

	protected void initOperationParameter() {
		name = tuner.getName();
		uiLabel = tuner.getLabel();
		options = tuner.getOptions();
		value = defaultValue = prepareServerValue();
//		defaultValue = prepareServerValue();
//		value = tuner.getSignal();

		//define OperationParameterType 
		String tunerType = tuner.getType();
		parameterValueClass = String.class;
		try {
			parameterValueClass = Class.forName(tunerType);
		} catch (ClassNotFoundException e) {
			System.out.println("ERROR> Tuner value type parsing error: " + tunerType);
			e.printStackTrace();
		}
		if (options != null){
			type = OperationParameterType.Option;
		}else if (Number.class.isAssignableFrom(parameterValueClass)) {
			type = OperationParameterType.Number;
		} else if (parameterValueClass == Boolean.class) {
			type = OperationParameterType.Boolean;
		} else if (parameterValueClass == String.class) {
			type = OperationParameterType.Text;
		} else if (parameterValueClass == URI.class) {
			type = OperationParameterType.Uri;
		} else if (parameterValueClass == StepDirection.class) {
			type = OperationParameterType.StepDirection;
		} else if (parameterValueClass == Position.class) {
			type = OperationParameterType.Position;
		} else if (parameterValueClass == IGroup.class && tuner.getUsage().equals("region")) {
			type = OperationParameterType.Region;
		} else {
			type = OperationParameterType.Unknown;
		}
	}

	protected Object prepareServerValue() {
		return tuner.getSignal();
	}
	
	public void updateValueFromServer() {
		value = prepareServerValue();
		isChanged = false;
		
		fireServerDataUpdatedEvent(value);
	}

	public void resetDefault(){
		setValue(getDefaultValue());
		tuner.setOptions(options);
		
		fireServerDataUpdatedEvent(value);
	}
	/**
	 * Creates new OperationParameter instance.
	 * @param name name of parameter.
	 * @param type type of parameter.
	 * @param defaultValue default value for the parameter.
	 */
//	public OperationParameter(String name, OperationParameterType type, Object defaultValue) {
//		this(name, type, defaultValue, defaultValue);
//	}
	
	/**
	 * Creates new OperationParameter instance.
	 * @param name name of parameter.
	 * @param type type of parameter.
	 * @param value a value for the parameter.
	 * @param defaultValue default value for the parameter.
	 */
//	public OperationParameter(String name, OperationParameterType type, Object value, Object defaultValue) {
//		this.name = name;
//		this.type = type;
//		this.value = value;
//		this.defaultValue = defaultValue;
//	}

	/**
	 * Gets value of the parameter.
	 * @return value specified for the parameter.
	 */
	public Object getValue() {
		return value;
	}
	
	/**
	 * Sets value for the parameter.
	 * @param value new parameter's value
	 */
	public void setValue(Object value) {
		if (isChanged) {
			this.value = value;
		} else if (!Util.areEqual(value, this.value)) {
			oldValue = this.value;
			this.value = value;
			if (oldValue != value)
				isChanged = true;
		}		
	}
	
	/**
	 * Gets default value of the parameter.
	 * @return default value object.
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}
	
	/**
	 * Gets parameter's name.
	 * @return string name of the parameter.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets UI label for the parameter.
	 * @return text to represent the object on UI
	 */
	public String getUILabel() {
		return uiLabel;
	}
	
	/**
	 * Gets type of the parameter.
	 * @return an object to identify parameter's type
	 * @see OperationParameterType
	 */
	public OperationParameterType getType() {
		return type;
	}
	
	public Class<?> getParameterValueClass() {
		return parameterValueClass;
	}

	public boolean isChanged() {
		return isChanged;
	}
	
	public void saveChanges() throws ProcessorChainException, ProcessFailedException, IllegalAccessException {
		if (isChanged) {
			tuner.setSignal(value);
			isChanged = false;
		}
	}
	
	public void revertChanges() {
		if (isChanged) {
			value = oldValue;
			isChanged = false;
		}
	}
	
	public void loadDefaultValue() {
		setValue(getDefaultValue());
	}

	public Object getMaxValue() {
		return tuner.getMax();
//		return new Double(100);
	}
	
	public Object getMinValue() {
		return tuner.getMin();
//		return new Double(-100);
	}
	
	public void addOperationParameterListener(OperationParameterListener operationParameterListener) {
		operationParameterListeners.add(operationParameterListener);
	}
	public void removeOperationParameterListener(OperationParameterListener operationParameterListener) {
		operationParameterListeners.remove(operationParameterListener);
	}
	public void removeAllOperationParameterListeners() {
		operationParameterListeners.clear();
	}
	public List<OperationParameterListener> getOperationParameterListeners() {
		return new ArrayList<OperationParameterListener>(operationParameterListeners);
	}
	
	protected void fireServerDataUpdatedEvent(Object newData) {
		for (OperationParameterListener operationParameterListener : operationParameterListeners) {
			operationParameterListener.serverDataUpdated(this, newData);
		}
	}

	public interface OperationParameterListener {
		void serverDataUpdated(OperationParameter operationParameter, Object newData);
		
	}

	public List<?> getOptions() {
		return tuner.getOptions();
	}

	public void updateValue() {
		value = prepareServerValue();
	}

	public int getUIWidth() {
		return tuner.getUIWidth();
	}

	public void addVarPortListener(TunerPortListener listener){
		tuner.addVarPortListener(listener);
	}
	
	public void removeVarPortListener(TunerPortListener listener){
		tuner.removeVarPortListener(listener);
	}

	public Tuner getTuner(){
		return tuner;
	}
	
	public void setChanged(boolean isChanged){
		this.isChanged = isChanged;
	}

	public String getProperty(String propertyName) {
		// TODO Auto-generated method stub
		return tuner.getProperty(propertyName);
	}
}
