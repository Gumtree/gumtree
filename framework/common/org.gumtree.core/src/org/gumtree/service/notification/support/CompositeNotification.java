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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gumtree.service.notification.INotification;

public class CompositeNotification extends AbstractNotification {

	private static final long serialVersionUID = -980368731685920869L;

	private List<INotification> notifications;

	public CompositeNotification() {
		super();
		notifications = new ArrayList<INotification>(2);
	}

	public void appendNotification(INotification notification) {
		// if (notification instanceof AbstractNotification) {
		// // Synchronise timestamp
		// ((AbstractNotification) notification).setTimestamp(getTimestamp());
		// }
		notifications.add(notification);
	}

	public void appendNotification(List<INotification> notifications) {
		this.notifications.addAll(notifications);
	}

	public List<INotification> getNotifications() {
		return Collections.unmodifiableList(notifications);
	}

	public String getProtocol() {
		return null;
	}

}
