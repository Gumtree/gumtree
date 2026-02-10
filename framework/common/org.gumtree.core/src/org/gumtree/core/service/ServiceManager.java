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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gumtree.core.internal.Activator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

public class ServiceManager implements IServiceManager {

	private static Logger logger = LoggerFactory
			.getLogger(ServiceManager.class);

	private BundleContext context;

	private long defaultTimeout = DEFAULT_TIMEOUT;

	private Map<String, ServiceTracker<?, ?>> cachedServiceTrackers;

	public ServiceManager() {
		this(Activator.getContext());
	}

	public ServiceManager(BundleContext context) {
		this.context = context;
	}

	/*************************************************************************
	 * Single service retrieval
	 *************************************************************************/

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gumtree.core.util.IServiceManager#getService(java.lang.Class)
	 */
	@Override
	public <T> T getService(Class<T> serviceClass)
			throws ServiceNotFoundException {
		return getService(serviceClass, getDefaultTimeout());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gumtree.core.util.IServiceManager#getServiceNow(java.lang.Class)
	 */
	@Override
	public <T> T getServiceNow(Class<T> serviceClass)
			throws ServiceNotFoundException {
		return getService(serviceClass, 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gumtree.core.util.IServiceManager#getService(java.lang.Class,
	 * long)
	 */
	@Override
	public <T> T getService(Class<T> serviceClass, long timeout)
			throws ServiceNotFoundException {
		ServiceTracker<T, T> tracker = getTracker(serviceClass, timeout);
		if (tracker.getTrackingCount() < 1) {
			try {
				tracker.waitForService(timeout);
			} catch (InterruptedException e) {
				logger.error(
						"Failed to track service " + serviceClass.getName()
								+ ".", e);
			}
		}
		return tracker.getService();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gumtree.core.util.IServiceManager#getService(java.lang.Class,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public <T> T getService(Class<T> serviceClass, String key, String value)
			throws ServiceNotFoundException {
		return getService(serviceClass, key, value, getDefaultTimeout());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gumtree.core.util.IServiceManager#getServiceNow(java.lang.Class,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public <T> T getServiceNow(Class<T> serviceClass, String key, String value)
			throws ServiceNotFoundException {
		return getService(serviceClass, key, value, 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gumtree.core.util.IServiceManager#getService(java.lang.Class,
	 * java.lang.String, java.lang.String, long)
	 */
	@Override
	public <T> T getService(Class<T> serviceClass, String key, String value,
			long timeout) throws ServiceNotFoundException {
		ServiceTracker<T, T> tracker = getTracker(serviceClass, timeout);
		if (tracker.getTrackingCount() < 1) {
			try {
				tracker.waitForService(timeout);
			} catch (InterruptedException e) {
				logger.error(
						"Failed to track service " + serviceClass.getName()
								+ ".", e);
			}
		}
		for (ServiceReference<T> ref : tracker.getServiceReferences()) {
			Object propKey = ref.getProperty(key);
			if (propKey != null && propKey.equals(value)) {
				return tracker.getService(ref);
			}
		}
		return null;
	}

	/*************************************************************************
	 * Multi services retrieval
	 *************************************************************************/

	@Override
	public <T> List<T> getServices(Class<T> serviceClass) {
		return getServices(serviceClass, getDefaultTimeout());
	}

	@Override
	public <T> List<T> getServicesNow(Class<T> serviceClass) {
		// No wait time
		return getServices(serviceClass, 1);
	}

	@Override
	public <T> List<T> getServices(Class<T> serviceClass, long timeout) {
		ServiceTracker<T, T> tracker = null;
		try {
			tracker = getTracker(serviceClass, timeout);
		} catch (ServiceNotFoundException e) {
			return Collections.<T> emptyList();
		}
		ServiceReference<T>[] refs = tracker.getServiceReferences();
		List<T> services = new ArrayList<T>();
		if (refs != null) {
			for (ServiceReference<T> ref : refs) {
				services.add((T) tracker.getService(ref));
			}
		}
		return services;
	}

	@Override
	public <T> List<T> getServices(Class<T> serviceClass, String key,
			String value) {
		return getServices(serviceClass, key, value, getDefaultTimeout());
	}

	@Override
	public <T> List<T> getServicesNow(Class<T> serviceClass, String key,
			String value) {
		// No wait time
		return getServices(serviceClass, key, value, 1);
	}

	@Override
	public <T> List<T> getServices(Class<T> serviceClass, String key,
			String value, long timeout) {
		List<T> services = new ArrayList<T>();
		ServiceTracker<T, T> tracker = null;
		try {
			tracker = getTracker(serviceClass, timeout);
		} catch (ServiceNotFoundException e) {
			return Collections.<T> emptyList();
		}
		for (ServiceReference<T> ref : tracker.getServiceReferences()) {
			Object propKey = ref.getProperty(key);
			if (propKey != null && propKey.equals(value)) {
				services.add(tracker.getService(ref));
			}
		}
		return services;
	}

	@Override
	public <T> List<IServiceDescriptor<T>> getServiceDescriptorsNow(
			Class<T> serviceClass) {
		return getServiceDescriptors(serviceClass, 1);
	}

	@Override
	public <T> List<IServiceDescriptor<T>> getServiceDescriptors(
			Class<T> serviceClass, long timeout) {
		ServiceTracker<T, T> tracker = null;
		try {
			tracker = getTracker(serviceClass, timeout);
		} catch (ServiceNotFoundException e) {
			return Collections.<IServiceDescriptor<T>> emptyList();
		}
		ServiceReference<T>[] refs = tracker.getServiceReferences();
		List<IServiceDescriptor<T>> descs = new ArrayList<IServiceDescriptor<T>>();
		if (refs != null) {
			for (ServiceReference<T> ref : refs) {
				Map<String, Object> properties = new HashMap<String, Object>(2);
				for (String propertyId : ref.getPropertyKeys()) {
					properties.put(propertyId, ref.getProperty(propertyId));
				}
				descs.add(new ServiceDescriptor<T>((T) tracker.getService(ref),
						properties));
			}
		}
		return descs;
	}

	/*************************************************************************
	 * Servicing methods
	 *************************************************************************/

	@Override
	public <T> ServiceTracker<T, T> getTrackerNow(Class<T> serviceClass)
			throws ServiceNotFoundException {
		return getTracker(serviceClass, 1);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> ServiceTracker<T, T> getTracker(Class<T> serviceClass,
			long timeout) throws ServiceNotFoundException {
		String serviceName = serviceClass.getName();
		ServiceTracker<?, ?> tracker = getCachedServiceTrackers().get(
				serviceName);
		// Create tracker
		if (tracker == null) {
			synchronized (this) {
				if (tracker == null) {
					try {
						tracker = new ServiceTracker<T, T>(getContext(),
								serviceName, null);
						tracker.open();
						getCachedServiceTrackers().put(serviceName, tracker);
					} catch (Exception e) {
						throw new ServiceNotFoundException(e);
					}
				}
			}
		}
		return (ServiceTracker<T, T>) tracker;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gumtree.core.util.IServiceManager#getTimeout()
	 */
	@Override
	public long getDefaultTimeout() {
		return defaultTimeout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gumtree.core.util.IServiceManager#setTimeout(long)
	 */
	@Override
	public void setDefaultTimeout(long timeout) {
		this.defaultTimeout = timeout;
	}

	private BundleContext getContext() {
		return context;
	}

	private Map<String, ServiceTracker<?, ?>> getCachedServiceTrackers() {
		if (cachedServiceTrackers == null) {
			cachedServiceTrackers = new HashMap<String, ServiceTracker<?, ?>>(2);
		}
		return cachedServiceTrackers;
	}

	public synchronized void disposeObject() {
		if (cachedServiceTrackers != null) {
			for (ServiceTracker<?, ?> tracker : cachedServiceTrackers.values()) {
				tracker.close();
			}
			cachedServiceTrackers.clear();
			cachedServiceTrackers = null;
		}
		context = null;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(ServiceManager.class)
				.add("defaultTimeout", getDefaultTimeout()).toString();
	}
	
}
