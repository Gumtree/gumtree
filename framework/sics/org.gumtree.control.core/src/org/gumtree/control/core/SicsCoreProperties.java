/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.control.core;

import org.gumtree.util.ISystemProperty;
import org.gumtree.util.SystemProperty;

public final class SicsCoreProperties {

	public static final ISystemProperty LOGIN_MODE = new SystemProperty(
			"gumtree.sics.loginMode", "normal");
	
	public static final ISystemProperty SERVER_HOST = new SystemProperty(
			"gumtree.control.serverHost", "localhost");

	public static final ISystemProperty SERVER_PORT = new SystemProperty(
			"gumtree.control.subPort", "5555");

	public static final ISystemProperty PUBLISH_PORT = new SystemProperty(
			"gumtree.control.dealerPort", "5566");

	public static final ISystemProperty ROLE = new SystemProperty(
			"gumtree.sics.role", "user");

	public static final ISystemProperty PASSWORD = new SystemProperty(
			"gumtree.sics.password", "");

	public static final ISystemProperty PASSWORD_ENCRYPTED = new SystemProperty(
			"gumtree.sics.passwordEncrypted", "false");

	public static final ISystemProperty TELNET_PORT = new SystemProperty(
			"gumtree.sics.telnetPort", "60001");
	
	public static final ISystemProperty VALIDATION_HOST = new SystemProperty(
			"gumtree.sics.validationHost", "localhost");
	
	public static final ISystemProperty VALIDATION_PORT = new SystemProperty(
			"gumtree.sics.validationPort", "60013");
	
	public static final ISystemProperty DATABASE = new SystemProperty(
			"gumtree.sics.database", "sics.yap");

	public static final ISystemProperty PERSIST_HDB_DATA = new SystemProperty(
			"gumtree.sics.persistHdbData", "true");
	
	public static final ISystemProperty INSTRUMENT_NAME = new SystemProperty(
			"gumtree.sics.instrumenrtName", "");

	// Unit: millisecond
	public static final ISystemProperty PROXY_TIMEOUT = new SystemProperty(
			"gumtree.sics.proxyTimeout", Integer.toString(5 * 60 * 1000));

	public static final ISystemProperty WATCHDOG_NOTIFY = new SystemProperty(
			"gumtree.sics.watchdog.notify", "false");

	public static final ISystemProperty WATCHDOG_NOTIFICATION_SUBJECT = new SystemProperty(
			"gumtree.sics.watchdog.notificationSubject",
			"[GUMTREE][WARNING]SICS is stalled");

	public static final ISystemProperty WATCHDOG_NOTIFICATION_SENDER = new SystemProperty(
			"gumtree.sics.watchdog.notificationSender", "");

	public static final ISystemProperty WATCHDOG_NOTIFICATION_RECIPIENTS = new SystemProperty(
			"gumtree.sics.watchdog.notificationRecipients", "");

	public static final ISystemProperty COMPONENT_CONTROLLER_FACTORY = new SystemProperty(
			"gumtree.sics.componentControllerFactory", "org.gumtree.gumnix.sics.control.ComponentControllerFactory");
	
	public static final ISystemProperty USE_NON_NIO_CHANNEL = new SystemProperty(
			"gumtree.sics.useNonNIOChannel", "false");
	
	public static final ISystemProperty MESSAGE_DROP_ENABLE = new SystemProperty(
			"gumtree.sics.messageDropEnable", "false");

	private SicsCoreProperties() {
		super();
	}

}
