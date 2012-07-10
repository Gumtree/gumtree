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

package org.gumtree.core.service;

import java.util.Map;

import org.gumtree.util.string.StringUtils;

import com.google.common.base.Objects;

public class ServiceDescriptor<T> implements IServiceDescriptor<T> {

	private Map<String, Object> properties;
	
	private T service;
	
	public ServiceDescriptor(T service,  Map<String, Object> properties) {
		this.service = service;
		this.properties = properties;
	}
	
	@Override
	public T getService() {
		return service;
	}

	@Override
	public Object getProperty(String propertyId) {
		return properties.get(propertyId);
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("properties", StringUtils.formatMap(properties))
				.toString();
	}

}
