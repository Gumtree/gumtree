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

package org.gumtree.service.preferences;

import org.gumtree.core.service.IService;

public interface IPreferencesManager extends IService {

	public String get(String key);
	
	public String get(String key, String def);
	
	public int getInt(String key);
	
	public int getInt(String key, int def);
	
	public long getLong(String key);
	
	public long getLong(String key, long def);
	
	public float getFloat(String key);
	
	public float getFloat(String key, float def);
	
	public double getDouble(String key);
	
	public double getDouble(String key, double def);
	
	public boolean getBoolean(String key);
	
	public boolean getBoolean(String key, boolean def);
	
	public void set(String key, String value);
	
	public void setInt(String key, int value);
	
	public void setLong(String key, long value);
	
	public void setFloat(String key, float value);
	
	public void setDouble(String key, double value);
	
	public void setBoolean(String key, boolean value);
	
	public void reset(String key);
	
	public void save();
	
}
