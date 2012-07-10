/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.workflow.ui.models;

import org.gumtree.core.object.ObjectConvertException;

public class SingleDataModel extends AbstractModelObject {

	private Object data;
	
	public SingleDataModel() {
		super();
	}
	
	public SingleDataModel(Object defaulData) {
		this();
		data = defaulData; 
	}
	
	public Object getData() {
		return data;
	}

	@SuppressWarnings("unchecked")
	public <T> T getData(Class<T> type) {
		// Case for string
		if (data instanceof String) {
			String stringValue = (String) data;
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
			return (T) data.toString();
		} else if (type.isAssignableFrom(data.getClass())) {
			return (T) data;
		} else {
			throw new ObjectConvertException("Cannot convert " + data.getClass() + " to " + type.getName());
		}
	}
	
	public void setData(Object data) {
		Object oldValue = this.data;
		this.data = data;
		firePropertyChange("data", oldValue, data);
	}
	
}
