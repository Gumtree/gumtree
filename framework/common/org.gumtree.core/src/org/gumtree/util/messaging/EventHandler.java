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

import org.eclipse.e4.core.services.events.IEventBroker;
import org.gumtree.core.internal.Activator;
import org.gumtree.util.eclipse.FilterBuilder;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
public abstract class EventHandler implements
		org.osgi.service.event.EventHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(EventHandler.class);

	private String topic;

	private String filter;

	private IEventBroker eventBroker;

	private volatile Boolean activated;

	public EventHandler(String topic) {
		this(topic, null);
	}

	public EventHandler(String topic, String filterKey, String filterValue) {
		this(topic, new FilterBuilder(filterKey, filterValue).get());
	}

	public EventHandler(String topic, String filter) {
		this.topic = topic;
		this.filter = filter;
		activated = false;
	}

	public EventHandler activate() {
		if (!isActivated()) {
			synchronized (activated) {
				if (!isActivated()) {
					if (getEventBroker() != null) {
						getEventBroker().subscribe(topic, filter, this, true);
						activated = true;
					} else {
						logger.warn("Event broker is missing (topic={}).",
								topic);
					}
				}
			}
		}
		return this;
	}

	public EventHandler deactivate() {
		if (isActivated()) {
			synchronized (activated) {
				if (isActivated()) {
					if (getEventBroker() != null) {
						getEventBroker().unsubscribe(this);
						activated = false;
					} else {
						logger.warn("Event broker is missing.");
					}
				}
			}
		}
		return this;
	}

	public EventHandler changeFilter(String filter) {
		this.filter = filter;
		if (isActivated()) {
			deactivate();
			activate();
		}
		return this;
	}

	public boolean isActivated() {
		return activated;
	}

	public IEventBroker getEventBroker() {
		if (eventBroker == null) {
			eventBroker = Activator.getDefault().getEclipseContext()
					.get(IEventBroker.class);
		}
		return eventBroker;
	}
	
	public EventHandler setEventBroker(IEventBroker eventBroker) {
		this.eventBroker = eventBroker;
		return this;
	}

	public static long getTimestamp(Event event) {
		return (Long) event.getProperty(EventConstants.TIMESTAMP);
	}

	public static Object getSource(Event event) {
		return event.getProperty(OsgiEventConstants.EVENT_SOURCE);
	}

	public static String getTopic(Event event) {
		return (String) event.getProperty(EventConstants.EVENT_TOPIC);
	}

	public static Object getProperty(Event event, String key) {
		return event.getProperty(key);
	}

	public static String getString(Event event, String key) {
		return event.getProperty(key).toString();
	}

}
