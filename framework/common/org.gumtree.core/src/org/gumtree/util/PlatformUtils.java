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

package org.gumtree.util;

import org.gumtree.core.service.IServiceManager;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.service.eventbus.IEventBus;

/**
 * The central class for access to the GumTree Platform. This class cannot
 * be instantiated or subclassed by clients; all functionality is provided
 * by static methods.  Features include:
 * <ul>
 * <li>the platform data graph adapter registry</li>
 * <li>dynamic plugin tracker service</li>
 * <li>service locator for inversion of control</li>
 * </ul>
 *
 * @since 1.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class PlatformUtils {

	/**
	 * Unique identifer for the program argument for specifying log config file location.
	 */
	public static final String ARG_LOG_CONFIG = "logConfig";
	
	/**
	 * Unique identifer for the platform event bus
	 */
	public static final String ID_PLATFORM_EVENT_BUS = "platformEventBus";
	
	public static final String EVENT_TOPIC_RUNTIME_STARTUP_MESSAGE = "org/gumtree/runtime/startupMessage";
	
	public static final String EVENT_PROP_RUNTIME_STARTUP_MESSAGE = "message";
	
	/**
	 * Private constructor to block instance creation.
	 */
	private PlatformUtils() {
		super();
	}
	
	public static IEventBus getPlatformEventBus() {
		return getServiceManager().getService(IEventBus.class, "id", ID_PLATFORM_EVENT_BUS);
	}
	
	/**
	 * Returns the OSGi service manager from the system.
	 * 
	 * @return service manager.
	 */
	public static IServiceManager getServiceManager() {
		return ServiceUtils.getServiceManager();
	}

}
