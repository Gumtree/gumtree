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

package org.gumtree.service.directory.support;

import java.util.Hashtable;

import org.gumtree.service.directory.IDirectoryService;
import org.gumtree.util.messaging.EventBuilder;

import com.google.common.base.Objects;

/**
 * Default implementation of directory service.
 * 
 * @author Tony Lam
 * @since 1.4
 */
public class DirectoryService implements IDirectoryService {

	public Hashtable<String, Object> directory;

	public DirectoryService() {
		directory = new Hashtable<String, Object>();
	}

	public synchronized void bind(final String name, final Object object) {
		directory.put(name, object);
		new EventBuilder(EVENT_TOPIC_BIND).append(EVENT_PROP_NAME, name)
				.append(EVENT_PROP_OBJECT, object).post();
	}

	public void unbind(String name) {
		directory.remove(name);
		new EventBuilder(EVENT_TOPIC_UNBIND).append(EVENT_PROP_NAME, name)
				.post();
	}

	public Object lookup(String name) {
		return directory.get(name);
	}

	@SuppressWarnings("unchecked")
	public <T> T lookup(String name, Class<T> type) {
		return (T) directory.get(name);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(DirectoryService.class).toString();
	}

}
