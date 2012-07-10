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

package org.gumtree.service.notification.support;

import org.gumtree.service.notification.INotification;
import org.gumtree.service.notification.INotificationProvider;

public abstract class AbstractNotificationProvider<T extends INotification>
		implements INotificationProvider<T> {

	private String id = "";

	protected AbstractNotificationProvider() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
