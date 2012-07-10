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

package org.gumtree.service.persistence.support;

import java.util.Set;

import org.gumtree.core.management.IManageableBean;

public class ObjectContainerManagerMonitor implements IManageableBean {

	private ObjectContainerManager manager;

	public ObjectContainerManagerMonitor(ObjectContainerManager manager) {
		this.manager = manager;
	}

	public String[] getOpenedObjectContainerIds() {
		Set<String> keys = manager.contexts.keySet();
		return keys.toArray(new String[keys.size()]);
	}

	@Override
	public String getRegistrationKey() {
		return "org.gumtree.core:type=ObjectContainerManager";
	}

}
