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
import java.util.Map;

public interface IParameters extends Map<String, Object> {

	public <T> T get(String key, Class<T> type);
	
	public <T> T get(String key, Class<T> type, T defaultValue);
	
	public String getString(String key);
	
	public String getString(String key, String defaultValue);

	public void addPropertyChangeListener(PropertyChangeListener listener);
	
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);
	
	public void removePropertyChangeListener(PropertyChangeListener listener);
	
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);
	
}
