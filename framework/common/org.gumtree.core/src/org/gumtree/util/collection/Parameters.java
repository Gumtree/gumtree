/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bragg Institute - initial API and implementation
 ******************************************************************************/

package org.gumtree.util.collection;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedHashMap;

import org.gumtree.core.object.ObjectConvertException;
import org.gumtree.util.PropertiesHelper;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class Parameters extends LinkedHashMap<String, Object> implements IParameters {

	private static final long serialVersionUID = -6259564633460436931L;

	@XStreamOmitField
	private volatile PropertyChangeSupport changeSupport;
	
	public <T> T get(String key, Class<T> type) {
		if (get(key) != null) {
			return convert(get(key), type);
		}
		return null;
	}
	
	public <T> T get(String key, Class<T> type, T defaultValue) {
		T value = get(key, type);
		return value == null ? defaultValue : value;
	}

	@SuppressWarnings("unchecked")
	private <T> T convert(Object value, Class<T> type) throws ObjectConvertException {
		// Case for string
		if (value instanceof String) {
			String stringValue = (String) value;
			if (String.class.equals(type)) {
				return (T) stringValue;
			} else if (int.class.equals(type) || Integer.class.equals(type)) {
				return (T) (Integer) Integer.parseInt(stringValue);
			} else if (long.class.equals(type) || Long.class.equals(type)) {
				return (T) (Long) Long.parseLong(stringValue);
			} else if (float.class.equals(type) || Float.class.equals(type)) {
				return (T) (Float) Float.parseFloat(stringValue);
			} else if (double.class.equals(type) || Double.class.equals(type)) {
				return (T) (Double) Double.parseDouble(stringValue);
			} else if (boolean.class.equals(type) || Boolean.class.equals(type)) {
				return (T) Boolean.valueOf(stringValue);
			} else {
				throw new ObjectConvertException("Cannot convert String to " + type.getName());
			}
		} else if (String.class.equals(type)) {
			return (T) value.toString();
		} else if (type.isAssignableFrom(value.getClass())) {
			return (T) value;
		} else {
			throw new ObjectConvertException("Cannot convert " + value.getClass() + " to " + type.getName());
		}
	}
	
	public String getString(String key) {
		return get(key, String.class);
	}
	
	public String getString(String key, String defaultValue) {
		return get(key, String.class, defaultValue);
	}
	
	public Object put(String key, Object value) {
		Object oldValue = get(key);
		if (value instanceof String) {
			value = PropertiesHelper.substitueWithProperties((String) value);
		}
		Object result = super.put(key, value);
		firePropertyChange(key, oldValue, value);
		return result;
	}
	
	public Object remove(Object key) {
		Object result = super.remove(key);
		// Can't do this because new value has to be a non-null value
//		getChangeSupport().firePropertyChange(key, result, null);
		return result;
	}
	
	private PropertyChangeSupport getChangeSupport() {
		if (changeSupport == null) {
			synchronized (this) {
				if (changeSupport == null) {
					changeSupport = new PropertyChangeSupport(this);
				}
			}
		}
		return changeSupport;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		getChangeSupport().addPropertyChangeListener(listener);
	}
	
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		getChangeSupport().addPropertyChangeListener(propertyName, listener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		getChangeSupport().removePropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		getChangeSupport().removePropertyChangeListener(propertyName, listener);
	}
	
	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		getChangeSupport().firePropertyChange(propertyName, oldValue, newValue);
	}
	
}
