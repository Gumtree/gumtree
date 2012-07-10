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

package org.gumtree.service.preferences.support;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.gumtree.service.preferences.IPreferencesManager;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

public class PreferencesManager implements IPreferencesManager {

	private static final Logger logger = LoggerFactory
			.getLogger(PreferencesManager.class);

	private static final String EMPTY_STRING = "";

	private static final String ROOT = "org.gumtree.core";

	private volatile Preferences preferences;

	private Lock saveLock;

	public PreferencesManager() {
		saveLock = new ReentrantLock();
	}

	/*************************************************************************
	 * Getters and setters
	 *************************************************************************/

	public Preferences getPreferences() {
		if (preferences == null) {
			synchronized (this) {
				if (preferences == null) {
					preferences = InstanceScope.INSTANCE.getNode(ROOT);
				}
			}
		}
		return preferences;
	}

	public void setPreferences(Preferences preferences) {
		this.preferences = preferences;
	}

	/*************************************************************************
	 * Implemented methods
	 *************************************************************************/

	@Override
	public String get(String key) {
		return get(key, EMPTY_STRING);
	}

	@Override
	public String get(String key, String def) {
		return getPreferences().get(key, System.getProperty(key, def));
	}

	@Override
	public int getInt(String key) {
		return getInt(key, 0);
	}

	@Override
	public int getInt(String key, int def) {
		return getPreferences().getInt(key, Integer.getInteger(key, def));
	}

	@Override
	public long getLong(String key) {
		return getLong(key, 0);
	}

	@Override
	public long getLong(String key, long def) {
		return getPreferences().getLong(key, Long.getLong(key, def));
	}

	@Override
	public float getFloat(String key) {
		return getFloat(key, 0.0f);
	}

	@Override
	public float getFloat(String key, float def) {
		try {
			return getPreferences().getFloat(key,
					Float.parseFloat(System.getProperty(key)));
		} catch (NullPointerException e) {
			return def;
		} catch (NumberFormatException e) {
			return def;
		}
	}

	@Override
	public double getDouble(String key) {
		return getDouble(key, 0.0);
	}

	@Override
	public double getDouble(String key, double def) {
		try {
			return getPreferences().getDouble(key,
					Double.parseDouble(System.getProperty(key)));
		} catch (NullPointerException e) {
			return def;
		} catch (NumberFormatException e) {
			return def;
		}
	}

	@Override
	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	@Override
	public boolean getBoolean(String key, boolean def) {
		try {
			return getPreferences().getBoolean(key, Boolean.getBoolean(key));
		} catch (Exception e) {
			return def;
		}
	}

	@Override
	public void set(String key, String value) {
		getPreferences().put(key, value);
	}

	@Override
	public void setInt(String key, int value) {
		getPreferences().putInt(key, value);
	}

	@Override
	public void setLong(String key, long value) {
		getPreferences().putLong(key, value);
	}

	@Override
	public void setFloat(String key, float value) {
		getPreferences().putFloat(key, value);

	}

	@Override
	public void setDouble(String key, double value) {
		getPreferences().putDouble(key, value);
	}

	@Override
	public void setBoolean(String key, boolean value) {
		getPreferences().putBoolean(key, value);
	}

	@Override
	public void reset(String key) {
		try {
			getPreferences().remove(key);
		} catch (IllegalStateException e) {
			// Do nothing as we allow resetting key even it does not exist
		}
	}

	@Override
	public void save() {
		try {
			saveLock.lock();
			getPreferences().flush();
		} catch (BackingStoreException e) {
			logger.error("Failed to save preference group "
					+ getPreferences().name() + ".", e);
		} finally {
			saveLock.unlock();
		}
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(PreferencesManager.class)
				.add("root", ROOT).toString();
	}

}
