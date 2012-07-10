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

import java.util.List;

import org.gumtree.core.object.IDisposable;
import org.osgi.util.tracker.ServiceTracker;

public interface IServiceManager extends IService, IDisposable {

	// 30 sec time out on getting service
	public static final long DEFAULT_TIMEOUT = 1 * 30 * 1000;

	// Maximum timeout is virtually no timeout
	public static final long NO_TIMEOUT = Long.MAX_VALUE;

	/*************************************************************************
	 * Single service retrieval
	 *************************************************************************/
	public <T> T getService(Class<T> serviceClass)
			throws ServiceNotFoundException;

	public <T> T getServiceNow(Class<T> serviceClass)
			throws ServiceNotFoundException;

	public <T> T getService(Class<T> serviceClass, long timeout)
			throws ServiceNotFoundException;

	public <T> T getService(Class<T> serviceClass, String key, String value)
			throws ServiceNotFoundException;

	public <T> T getServiceNow(Class<T> serviceClass, String key, String value)
			throws ServiceNotFoundException;

	public <T> T getService(Class<T> serviceClass, String key, String value,
			long timeout) throws ServiceNotFoundException;

	/*************************************************************************
	 * Multi services retrieval
	 *************************************************************************/

	public <T> List<T> getServices(Class<T> serviceClass);

	public <T> List<T> getServicesNow(Class<T> serviceClass);

	public <T> List<T> getServices(Class<T> serviceClass, long timeout);

	public <T> List<T> getServices(Class<T> serviceClass, String key,
			String value);

	public <T> List<T> getServicesNow(Class<T> serviceClass, String key,
			String value);

	public <T> List<T> getServices(Class<T> serviceClass, String key,
			String value, long timeout);

	public <T> List<IServiceDescriptor<T>> getServiceDescriptors(
			Class<T> serviceClass, long timeout);

	public <T> List<IServiceDescriptor<T>> getServiceDescriptorsNow(
			Class<T> serviceClass);

	/*************************************************************************
	 * Servicing methods
	 *************************************************************************/

	public <T> ServiceTracker<T, T> getTrackerNow(Class<T> serviceClass)
			throws ServiceNotFoundException;

	public <T> ServiceTracker<T, T> getTracker(Class<T> serviceClass,
			long timeout) throws ServiceNotFoundException;

	public long getDefaultTimeout();

	public void setDefaultTimeout(long timeout);

}
