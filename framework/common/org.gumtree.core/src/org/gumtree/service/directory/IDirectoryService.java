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

package org.gumtree.service.directory;

import org.gumtree.core.service.IService;

/**
 * Directory service is a global hash table for storing data. Data is usually
 * stored as key-value pair. The directory 
 * 
 * @author Tony Lam
 * @since 1.4
 * 
 */
public interface IDirectoryService extends IService {

	public static final String EVENT_TOPIC = "org/gumtree/core/directory";
	
	public static final String EVENT_TOPIC_BIND = EVENT_TOPIC + "/bind";
	
	public static final String EVENT_TOPIC_UNBIND = EVENT_TOPIC + "/unbind";
	
	public static final String EVENT_PROP_NAME = "name";
	
	public static final String EVENT_PROP_OBJECT = "object";
			
	public void bind(String name, Object object);

	public Object lookup(String name);

	public <T> T lookup(String name, Class<T> type);

	public void unbind(String name);

}
