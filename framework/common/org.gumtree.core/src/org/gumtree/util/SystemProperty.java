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

package org.gumtree.util;

import org.gumtree.core.service.ServiceNotFoundException;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.service.preferences.IPreferencesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemProperty implements ISystemProperty {
	
	private static Logger logger = LoggerFactory.getLogger(SystemProperty.class); 
	
	private static IPreferencesManager preferences;
	
	private String key;
	
	public SystemProperty(String key, String def) {
		this.key = key;
		// Set default
		if (System.getProperty(key) == null) {
			System.setProperty(key, def);
		}
		// We only set this once using static field
		if (preferences == null) {
			try {
				preferences = ServiceUtils.getServiceManager().getServiceNow(
						IPreferencesManager.class);
			} catch (ServiceNotFoundException e) {
				logger.warn("Preferences manager service is not available.  Preferences save is now disabled.");
			}
		}
	}
	
	@Override
	public String getKey() {
		return key;
	}
	
	@Override
	public String getValue() {
		if (preferences != null) {
			return preferences.get(getKey());
		}
		return System.getProperty(getKey());
	}
	
	@Override
	public int getInt() {
		if (preferences != null) {
			return preferences.getInt(getKey()); 
		}
		return Integer.getInteger(getKey());
	}

	@Override
	public long getLong() {
		if (preferences != null) {
			return preferences.getLong(getKey());
		}
		return Long.getLong(getKey());
	}
	
	@Override
	public float getFloat() {
		if (preferences != null) {
			return preferences.getFloat(getKey());	
		}
		return Float.parseFloat(getValue());
	}

	@Override
	public double getDouble() {
		if (preferences != null) {
			return preferences.getDouble(getKey());
		}
		return Double.parseDouble(getValue());
	}
	
	@Override
	public boolean getBoolean() {
		if (preferences != null) {
			return preferences.getBoolean(getKey());
		}
		return Boolean.getBoolean(getKey());
	}
	
	@Override
	public ISystemProperty setValue(String value) {
		if (preferences != null) {
			preferences.set(getKey(), value);
		}
		return this;
	}

	@Override
	public ISystemProperty setInt(int value) {
		if (preferences != null) {
			preferences.setInt(getKey(), value);
		}
		return this;
	}

	@Override
	public ISystemProperty setLong(long value) {
		if (preferences != null) {
			preferences.setLong(getKey(), value);
		}
		return this;
	}

	@Override
	public ISystemProperty setFloat(float value) {
		if (preferences != null) {
			preferences.setFloat(getKey(), value);
		}
		return this;
	}

	@Override
	public ISystemProperty setDouble(double value) {
		if (preferences != null) {
			preferences.setDouble(getKey(), value);
		}
		return this;
	}

	@Override
	public ISystemProperty setBoolean(boolean value) {
		if (preferences != null) {
			preferences.setBoolean(getKey(), value);
		}
		return this;
	}
	
	@Override
	public void reset() {
		if (preferences != null) {
			preferences.reset(getKey());
		}
	}
	
	@Override
	public void save() {
		if (preferences != null) {
			preferences.save();
		}
	}

}
