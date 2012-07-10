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

package org.gumtree.util.messaging;

import java.util.HashMap;
import java.util.Map;

import org.gumtree.core.service.ServiceUtils;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;

public abstract class OsgiEventHandler implements org.osgi.service.event.EventHandler {
	
	private Map<String, Object> properties;
	
	private volatile ServiceRegistration<?> registration;
	
	public OsgiEventHandler(String topic) {
		properties = new HashMap<String, Object>(2);
		properties.put(OsgiEventConstants.EVENT_TOPIC, topic);
	}
	
	public OsgiEventHandler(String topic, String filter) {
		this(topic);
		properties.put(OsgiEventConstants.EVENT_FILTER, filter);
	}
	
	public OsgiEventHandler activate() {
		if (registration == null) {
			synchronized (this) {
				if (registration == null) {
					registration = ServiceUtils.registerService(
							org.osgi.service.event.EventHandler.class, this,
							properties);
				}
			}
		}
		return this;
	}

	public OsgiEventHandler deactivate() {
		if (registration != null) {
			synchronized (this) {
				if (registration != null) {
					ServiceUtils.unregisterService(registration);
				}
			}
		}
		return this;
	}

	public static long getTimestamp(Event event) {
		return (Long) event.getProperty(OsgiEventConstants.TIMESTAMP);
	}
	
	public static Object getSource(Event event) {
		return event.getProperty(OsgiEventConstants.EVENT_SOURCE);
	}
	
	public static String getTopic(Event event) {
		return (String) event.getProperty(OsgiEventConstants.EVENT_TOPIC);
	}

	public static String getString(Event event, String key) {
		return event.getProperty(key).toString();
	}
}
