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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogNotificationProvider extends
		AbstractNotificationProvider<LogNotification> {

	private static final Logger logger = LoggerFactory
			.getLogger(LogNotificationProvider.class);

	public void handleNotification(LogNotification notifcation) {
		logger.info(notifcation.getMessage());
	}

	public boolean isLongOperation(LogNotification notification) {
		// Sending log message is quick
		return false;
	}

}
