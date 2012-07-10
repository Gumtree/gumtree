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

public interface ISystemProperty {

	public String getKey();
	
	public String getValue();
	
	public int getInt();
	
	public long getLong();

	public float getFloat();
	
	public double getDouble();
	
	public boolean getBoolean();
	
	public ISystemProperty setValue(String value);
	
	public ISystemProperty setInt(int value);
	
	public ISystemProperty setLong(long value);
	
	public ISystemProperty setFloat(float value);
	
	public ISystemProperty setDouble(double value);
	
	public ISystemProperty setBoolean(boolean value);
	
	public void reset();
	
	public void save();
	
}
