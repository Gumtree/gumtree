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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.gumtree.core.management.IManageableBean;
import org.gumtree.service.dataaccess.DataAccessException;
import org.gumtree.service.dataaccess.IDataHandler;
import org.gumtree.service.dataaccess.IDataProvider;

public class DataAccessManager extends AbstractDataAccessManager {

	private ExecutorService executor;

	private DataAccessManagerMonitor monitor;

	public DataAccessManager() {
		executor = Executors.newFixedThreadPool(1);
		monitor = new DataAccessManagerMonitor();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> T get(URI uri, Class<T> representation,
			Map<String, Object> properties) {
		List<IDataProvider> providers = getDataProviders(uri.getScheme());
		for (IDataProvider provider : providers) {
			T data = (T) provider.get(uri, representation, properties);
			if (data != null) {
				return data;
			}
		}
		throw new DataAccessException("Data provider not found for "
				+ uri.toString());
	}

	public <T> void get(final URI uri, final Class<T> representation,
			final IDataHandler<T> callback, final Map<String, Object> properties) {
		executor.execute(new Runnable() {
			public void run() {
				try {
					T data = get(uri, representation, properties);
					callback.handleData(uri, data);
				} catch (Exception e) {
					callback.handleError(uri, e);
				}
			}
		});
	}

	public void dispose() {
		if (executor != null) {
			executor.shutdown();
			executor = null;
		}
	}

	@Override
	public IManageableBean[] getManageableBeans() {
		return new IManageableBean[] { monitor };
	}

}
