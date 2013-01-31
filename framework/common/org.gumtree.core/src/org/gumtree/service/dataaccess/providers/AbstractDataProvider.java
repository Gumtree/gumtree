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

package org.gumtree.service.dataaccess.providers;

import java.util.List;
import java.util.Map;

import org.gumtree.core.service.IServiceManager;
import org.gumtree.service.dataaccess.DataAccessException;
import org.gumtree.service.dataaccess.IDataChangeListener;
import org.gumtree.service.dataaccess.IDataConverter;
import org.gumtree.service.dataaccess.IDataProvider;
import org.gumtree.service.dataaccess.RepresentationNotSupportedException;
import org.gumtree.util.messaging.IListenerManager;
import org.gumtree.util.messaging.ListenerManager;

public abstract class AbstractDataProvider<K> implements IDataProvider<K> {

	private static final String PROP_PROVIDER = "provider";
	
	private IServiceManager serviceManager;
	
	protected IListenerManager<IDataChangeListener> listenerManager;
	
	public AbstractDataProvider() {
		super();
		listenerManager = new ListenerManager<IDataChangeListener>();
	}
	
	@SuppressWarnings("unchecked")
	public List<IDataConverter<K>> getDataConverter() {
//		Object providers = (Object) ServiceUtils.getServiceManager()
//				.getServices(IDataConverter.class, PROP_PROVIDER,
//						getClass().getName());
		Object providers = getServiceManager().getServicesNow(
				IDataConverter.class, PROP_PROVIDER, getClass().getName());
		return (List<IDataConverter<K>>) providers;
	}
	
	protected <T> T convert(K rawData, Class<T> representation, Map<String, Object> properties) throws DataAccessException {
		T result = null;
		Exception exception = null;
		for (IDataConverter<K> converter : getDataConverter()) {
			try {
				result = converter.convert(rawData, representation, properties);
				if (result != null) {
					return result;
				}
			} catch (Exception e) {
				exception  = e;
			}
		}
		if (exception != null) {
			throw new DataAccessException(exception);
		} else {
			throw new RepresentationNotSupportedException();
		}
	}
	
	public void addDataChangeListener(IDataChangeListener listener) {
		listenerManager.addListenerObject(listener);
	}
	
	public void removeDataChangeListener(IDataChangeListener listener) {
		listenerManager.removeListenerObject(listener);
	}
	     
	public IServiceManager getServiceManager() {
		return serviceManager;
	}

	public void setServiceManager(IServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

}
