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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.gumtree.data.Factory;
import org.gumtree.data.IFactory;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.service.dataaccess.DataAccessException;
import org.gumtree.service.dataaccess.IDataConverter;
import org.gumtree.service.dataaccess.RepresentationNotSupportedException;

public class GumTreeDataConverter implements IDataConverter<IFileStore> {

	private static final List<Class<?>> SUPPORTED_FORMATS = Arrays
			.asList(new Class<?>[] { IDataset.class });

	private IFactory factory;
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T convert(IFileStore fileStore, Class<T> representation,
			Map<String, Object> properties)
			throws RepresentationNotSupportedException {
		// Quick check
		if (!getSupportedRepresentations().contains(representation)) {
			throw new RepresentationNotSupportedException();
		}
		// Convert
		try {
			if (representation.equals(IDataset.class)) {
				return (T) getFactory().createDatasetInstance(fileStore.toLocalFile(
						EFS.NONE, null).toURI());
			}
		} catch (Exception e) {
			throw new DataAccessException(e);
		}
		throw new RepresentationNotSupportedException();
	}

	public IFactory getFactory() {
		if (factory == null) {
			factory = Factory.getFactory();
		}
		return factory;
	}
	
	public void setFactory(IFactory factory) {
		this.factory = factory;
	}
	
	public List<Class<?>> getSupportedRepresentations() {
		return SUPPORTED_FORMATS;
	}

}
