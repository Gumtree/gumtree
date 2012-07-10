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
import java.util.Map;

import org.gumtree.core.object.IDisposable;
import org.osgi.framework.ServiceRegistration;

public interface IServiceRegistrationManager extends IDisposable {

	public ServiceRegistration<?> registerService(Class<?> clazz, Object service);

	public ServiceRegistration<?> registerService(Class<?> clazz,
			Object service, Dictionary<String, ?> properties);

	public ServiceRegistration<?> registerService(Class<?> clazz,
			Object service, Map<String, ?> properties);

	public void unregisterService(ServiceRegistration<?> registration);

}
