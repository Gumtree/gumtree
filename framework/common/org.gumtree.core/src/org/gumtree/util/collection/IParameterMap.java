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

import java.util.Map;

public interface IParameterMap extends Map<String, String> {

	public String getValue(String key, String defaultValue);
	
	public <T> T getValue(String key, Class<T> type, T defaultValue);
	
}
