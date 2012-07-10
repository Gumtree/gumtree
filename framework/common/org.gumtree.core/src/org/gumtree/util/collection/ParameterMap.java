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

import java.util.HashMap;

import org.gumtree.util.PropertiesHelper;

public class ParameterMap extends HashMap<String, String> implements
		IParameterMap {

	private static final long serialVersionUID = 379182145167447323L;

	public String put(String key, String value) {
		// Value is processed with the current environment setting
		return super.put(key, PropertiesHelper.substitueWithProperties(value));
	}

	public String getValue(String key, String defaultValue) {
		if (!containsKey(key)) {
			return defaultValue;
		}
		return get(key);
	}

	@SuppressWarnings("unchecked")
	public <T> T getValue(String key, Class<T> type, T defaultValue) {
		// Returns defaultValue if value doesn't exist
		if (!containsKey(key)) {
			return defaultValue;
		}
		try {
			if (String.class.equals(type)) {
				return (T) get(key);
			} else if (int.class.equals(type) || Integer.class.equals(type)) {
				return (T) (Integer) Integer.parseInt(get(key));
			} else if (long.class.equals(type) || Long.class.equals(type)) {
				return (T) (Long) Long.parseLong(get(key));
			} else if (float.class.equals(type) || Float.class.equals(type)) {
				return (T) (Float) Float.parseFloat(get(key));
			} else if (double.class.equals(type) || Double.class.equals(type)) {
				return (T) (Double) Double.parseDouble(get(key));
			} else if (boolean.class.equals(type) || Boolean.class.equals(type)) {
				return (T) Boolean.valueOf(get(key));
			}
		} catch (Exception e) {
		}
		// Returns default if value cannot be converted to the given type
		return defaultValue;
	}

}
