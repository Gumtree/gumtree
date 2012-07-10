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

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

import org.gumtree.core.internal.Activator;
import org.gumtree.core.object.IDisposable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceRegistrationManager implements IServiceRegistrationManager {

	private static final Logger logger = LoggerFactory
			.getLogger(ServiceRegistrationManager.class);

	// Stack of registered OSGi services
	private Stack<ServiceRegistration<?>> registeredServices;

	private BundleContext context;

	public ServiceRegistrationManager() {
		this(Activator.getContext());
	}

	public ServiceRegistrationManager(BundleContext context) {
		this.context = context;
		registeredServices = new Stack<ServiceRegistration<?>>();
	}

	@Override
	public ServiceRegistration<?> registerService(Class<?> clazz, Object service) {
		return registerService(clazz, service, (Dictionary<String, ?>) null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ServiceRegistration<?> registerService(Class<?> clazz,
			Object service, Map<String, ?> properties) {
		return registerService(clazz, service,
				(Dictionary<String, ?>) new Hashtable(properties));
	}

	@Override
	public ServiceRegistration<?> registerService(Class<?> clazz,
			Object service, Dictionary<String, ?> properties) {
		ServiceRegistration<?> serviceRegistration = context.registerService(
				clazz.getName(), service, properties);
		registeredServices.add(serviceRegistration);
		logger.info("Registered service " + clazz.getName() + " for bundle "
				+ context.getBundle().getSymbolicName() + ".");
		return serviceRegistration;
	}

	@Override
	public void unregisterService(ServiceRegistration<?> registration) {
		registeredServices.remove(registration);
		try {
			doUnregisterService(registration);
		} catch (Exception e) {
			logger.warn("Service may already been disposed.", e);
		}
	}

	private void doUnregisterService(ServiceRegistration<?> registration) {
		Object service = context.getService(registration.getReference());
		// Dispose service
		if (service instanceof IDisposable) {
			((IDisposable) service).disposeObject();
		}
		// Unregister
		registration.unregister();
	}

	public void disposeObject() {
		// Manually unregister services in a LIFO way
		while (!registeredServices.isEmpty()) {
			ServiceRegistration<?> registration = registeredServices.pop();
			doUnregisterService(registration);
		}
		registeredServices = null;
		context = null;
	}

}
