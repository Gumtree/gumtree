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

package org.gumtree.service.dataaccess.support;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

import org.gumtree.core.service.IServiceManager;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.service.dataaccess.IDataChangeListener;
import org.gumtree.service.dataaccess.IDataHandler;
import org.gumtree.service.dataaccess.IDataProvider;
import org.gumtree.service.dataaccess.providers.AbstractDataProvider;

/**
 * Abstract implementation of the data access manager. It provides some basic
 * features that are common to all implementers.
 * 
 * @author Tony Lam
 * @since 1.4
 */
public abstract class AbstractDataAccessManager implements IDataAccessManager {

	private static final String PROP_SCHEME = "scheme";

	private IServiceManager serviceManager;
	
	public AbstractDataAccessManager() {
		super();
	}

	public <T> T get(URI uri, Class<T> representation) {
		return get(uri, representation, new HashMap<String, Object>(0));
	}

	public <T> void get(final URI uri, final Class<T> representation,
			final IDataHandler<T> callback) {
		get(uri, representation, callback, new HashMap<String, Object>(0));
	}

	@SuppressWarnings("rawtypes")
	protected List<IDataProvider> getDataProviders(String scheme) {
		List<IDataProvider> providers = getServiceManager().getServicesNow(
				IDataProvider.class, PROP_SCHEME, scheme);

		// TODO: provide a way to reconfigure the data providers order
		for (IDataProvider provider : providers) {
			if (provider instanceof AbstractDataProvider) {
				((AbstractDataProvider<?>) provider).setServiceManager(getServiceManager());
			}
		}
		return providers;
	}

	@SuppressWarnings("rawtypes")
	public void addDataChangeListener(IDataChangeListener listener) {
		List<IDataProvider> providers = getDataProviders(listener.getScheme());
		for (IDataProvider provider : providers) {
			provider.addDataChangeListener(listener);
		}
	}

	@SuppressWarnings("rawtypes")
	public void removeDataChangeListener(IDataChangeListener listener) {
		List<IDataProvider> providers = getDataProviders(listener.getScheme());
		for (IDataProvider provider : providers) {
			provider.removeDataChangeListener(listener);
		}
	}

	public IServiceManager getServiceManager() {
		return serviceManager;
	}

	public void setServiceManager(IServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

}
