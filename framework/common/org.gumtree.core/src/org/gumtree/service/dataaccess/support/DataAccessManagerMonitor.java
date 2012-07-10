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

import java.util.ArrayList;
import java.util.List;

import org.gumtree.core.service.ServiceUtils;
import org.gumtree.service.dataaccess.IDataProvider;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class DataAccessManagerMonitor implements IDataAccessManagerMonitor {

	private static final String PROP_SCHEME = "scheme";
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public DataAccessProviderInfo[] getAvailableProviders() {
		ServiceTracker tracker = ServiceUtils.getServiceManager().getTrackerNow(IDataProvider.class);
		List<DataAccessProviderInfo> results = new ArrayList<DataAccessProviderInfo>();
		for (ServiceReference ref : tracker.getServiceReferences()) {
			String scheme = (String) ref.getProperty(PROP_SCHEME);
			Object provider = tracker.getService(ref);
			DataAccessProviderInfo info = new DataAccessProviderInfo(scheme,
					provider.getClass().getName());
			results.add(info);
		}
		return results.toArray(new DataAccessProviderInfo[results.size()]);
	}

	@Override
	public String getRegistrationKey() {
		return "org.gumtree.core:type=DataAccessManager";
	}
	
}
