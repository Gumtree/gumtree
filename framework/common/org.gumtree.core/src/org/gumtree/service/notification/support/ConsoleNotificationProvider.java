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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ConsoleNotificationProvider extends
		AbstractNotificationProvider<LogNotification> {

	private DateFormat dateFormat;

	public ConsoleNotificationProvider() {
		dateFormat = SimpleDateFormat.getTimeInstance();
	}

	public void handleNotification(LogNotification notification) {
		System.out.println("[Message - "
				+ dateFormat.format(notification.getTimestamp()) + "] "
				+ notification.getMessage());
	}

	public boolean isLongOperation(LogNotification notification) {
		// Sending console message is quick
		return false;
	}

}
