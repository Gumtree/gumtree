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

import org.eclipse.e4.core.services.events.IEventBroker;
import org.gumtree.core.internal.Activator;
import org.osgi.service.event.EventConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
public class EventBuilder {

	private static final Logger logger = LoggerFactory
			.getLogger(EventBuilder.class);

	private String topic;

	private Map<String, Object> data;

	private IEventBroker eventBroker;

	public EventBuilder(String topic) {
		this.topic = topic;
		data = new HashMap<String, Object>(2);
	}

	public EventBuilder(String topic, IEventBroker eventBroker) {
		this(topic);
		setEventBroker(eventBroker);
	}

	public EventBuilder append(String key, Object value) {
		data.put(key, value);
		return this;
	}

	public EventBuilder remove(String key) {
		data.remove(key);
		return this;
	}

	public IEventBroker getEventBroker() {
		if (eventBroker == null) {
			if (Activator.getDefault() != null) {
				eventBroker = Activator.getDefault().getEclipseContext()
						.get(IEventBroker.class);
			}
		}
		return eventBroker;
	}

	public EventBuilder setEventBroker(IEventBroker eventBroker) {
		this.eventBroker = eventBroker;
		return this;
	}

	public void post() {
		append(EventConstants.TIMESTAMP, System.currentTimeMillis());
		if (getEventBroker() != null) {
			getEventBroker().post(topic, data);
		} else {
			logger.warn("Event broker is missing (topic={}).", topic);
		}
	}

	public void send() {
		append(EventConstants.TIMESTAMP, System.currentTimeMillis());
		if (getEventBroker() != null) {
			getEventBroker().send(topic, data);
		} else {
			logger.warn("Event broker is missing (topic={}).", topic);
		}
	}

}
