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

package org.gumtree.service.dataaccess;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;

/**
 * Data change listener for monitoring a single resource.
 * 
 * @author Tony Lam
 *
 * @param <T>
 */
public abstract class DataChangeAdapter<T> implements IDataChangeListener {

	private URI uri;
	
	private Class<T> representation;
	
	@SuppressWarnings("unchecked")
	public DataChangeAdapter(URI uri) {
		this.uri = uri;
		this.representation = findEventType(this);
	}
	
	public Class<T> getRepresentation(URI uri) {
		return representation;
	}

	public String getScheme() {
		return uri.getScheme();
	}

	@SuppressWarnings("unchecked")
	public void handleDataChange(URI uri, Class<?> representation, Object data) {
		handleData((T) data);
	}

	public abstract void handleData(T data);
	
	public boolean matchUri(URI uri) {
		return this.uri.equals(uri);
	}

	@SuppressWarnings("rawtypes")
	private static Class findEventType(DataChangeAdapter adapter) {
		if (adapter == null) {
			return null;
		}
		
		// Get generic information
		Type type = adapter.getClass().getGenericSuperclass();
		Type[] actualTypes = ((ParameterizedType) type).getActualTypeArguments();
		if (actualTypes.length == 1) {
			return (Class) actualTypes[0];
		}

		return null;
	}
	
}
