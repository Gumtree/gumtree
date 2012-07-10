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

public class CollectionUtils {

	public static <K, V> Map<K, V> createMap(K key, V value) {
		return new MapBuilder<K, V>(key, value).get();
	}
	
	private CollectionUtils() {
		super();
	}
	
}
