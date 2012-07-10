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
import java.util.Map;

public class MapBuilder<K, V> {

	private Map<K, V> map;
	
	public MapBuilder() {
		map = new HashMap<K, V>(2);
	}
	
	public MapBuilder(K key, V value) {
		this();
		map.put(key, value);
	}
	
	public MapBuilder<K, V> append(K key, V value) {
		map.put(key, value);
		return this;
	}
	
	public Map<K, V> get() {
		return map;
	}
	
}
