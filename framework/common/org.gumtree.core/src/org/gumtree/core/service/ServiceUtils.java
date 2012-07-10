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

import org.gumtree.core.internal.Activator;
import org.osgi.framework.ServiceRegistration;

@SuppressWarnings("restriction")
public final class ServiceUtils {

	/**
	 * Returns a registered service from the OSGi runtime. For more service
	 * related operation, use getServiceManager().
	 * 
	 * @param <T>
	 * @param serviceClass
	 * @return service or null if service is unavailable after default timeout
	 */
	public static <T> T getService(Class<T> serviceClass) {
		return getServiceManager().getService(serviceClass);
	}

	public static <T> T getServiceNow(Class<T> serviceClass) {
		return getServiceManager().getServiceNow(serviceClass);
	}

	public static <T> ServiceRegistration<?> registerService(
			Class<T> serviceClass, T service) {
		return Activator.getDefault().getServiceRegistrationManager()
				.registerService(serviceClass, service);
	}

	public static <T> ServiceRegistration<?> registerService(
			Class<T> serviceClass, T service, Map<String, ?> properties) {
		return Activator.getDefault().getServiceRegistrationManager()
				.registerService(serviceClass, service, properties);
	}

	public static void unregisterService(ServiceRegistration<?> registration) {
		Activator.getDefault().getServiceRegistrationManager()
				.unregisterService(registration);
	}

	// TODO: client should use e4 DI to inject service manager instead
	@Deprecated
	public static IServiceManager getServiceManager() {
		return Activator.getDefault().getEclipseContext()
				.get(IServiceManager.class);
		// return Activator.getDefault().getServiceManager();
	}

	private ServiceUtils() {
	}

}
