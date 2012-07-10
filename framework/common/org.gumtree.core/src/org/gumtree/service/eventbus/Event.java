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

package org.gumtree.service.eventbus;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Event implements IEvent {

	private Object publisher;
	
	private Date time;
	
	private Map<String, Object> properties;
	
	public Event(Object publisher) {
		this(publisher, new HashMap<String, Object>(0));
	}
	
	public Event(Object publisher, Map<String, Object> properties) {
		this.publisher = publisher;
		this.time = Calendar.getInstance().getTime();
		this.properties = Collections.unmodifiableMap(properties);
	}
	
	public Object getPublisher() {
		return publisher;
	}

	public Date getTime() {
		return time;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}
	
}
