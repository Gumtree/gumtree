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

import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;

public class OsgiEventBuilder {

	private String topic;
	
	private Map<String, Object> properties;
	
	public OsgiEventBuilder(String topic) {
		this.topic = topic;
		properties = new HashMap<String, Object>(2);
	}
	
	public OsgiEventBuilder append(String key, Object value) {
		properties.put(key, value);
		return this;
	}
	
	public OsgiEventBuilder remove(String key) {
		properties.remove(key);
		return this;
	}
	
	public Event get() {
		append(EventConstants.TIMESTAMP, System.currentTimeMillis());
		return new Event(topic, properties);
	}
	
}
