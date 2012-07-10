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

package org.gumtree.service.dataaccess.converters;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.methods.GetMethod;
import org.gumtree.service.dataaccess.DataAccessException;
import org.gumtree.service.dataaccess.IDataConverter;
import org.gumtree.service.dataaccess.RepresentationNotSupportedException;

public class HttpGeneralConverter implements IDataConverter<GetMethod> {

	private static final List<Class<?>> SUPPORTED_FORMATS = Arrays.asList(new Class<?>[] {
			InputStream.class, String.class });
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T convert(GetMethod getMethod, Class<T> representation,
			Map<String, Object> properties)
			throws RepresentationNotSupportedException {
		// Quick check
		if (!getSupportedRepresentations().contains(representation)) {
			throw new RepresentationNotSupportedException();
		}
		try {
			if (representation.equals(InputStream.class)) {
				return (T) getMethod.getResponseBodyAsStream();
			} else if (representation.equals(String.class)) {
				return (T) getMethod.getResponseBodyAsString();
			}
		} catch (Exception e) {
			throw new DataAccessException(e);
		}
		throw new RepresentationNotSupportedException();
	}

	public List<Class<?>> getSupportedRepresentations() {
		return SUPPORTED_FORMATS;
	}

}
