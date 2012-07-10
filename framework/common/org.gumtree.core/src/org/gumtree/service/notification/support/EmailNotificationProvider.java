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
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

public class EmailNotificationProvider extends
		AbstractNotificationProvider<EmailNotification> {

	private static final String PROP_HOST = "notification.email.host";

	private static final String PROP_PORT = "notification.email.port";

	private static final Logger logger = LoggerFactory
			.getLogger(EmailNotificationProvider.class);

	private JavaMailSender mailSender;

	public void handleNotification(EmailNotification notification) {
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setFrom(notification.getFrom());
		msg.setTo(notification.getTo().split("[,;]"));
		msg.setSubject(notification.getSubject());
		msg.setText(notification.getText());
		try {
			getMailSender().send(msg);
			logger.info("Sent email " + notification.toString());
		} catch (MailException e) {
			logger.error("Failed to send email " + notification.toString(), e);
		}
	}

	private JavaMailSender getMailSender() {
		if (mailSender == null) {
			JavaMailSenderImpl mailSenderObject = new JavaMailSenderImpl();
			mailSenderObject.setHost(System.getProperty(PROP_HOST));
			mailSenderObject.setPort(Integer.getInteger(PROP_PORT, 25));
			mailSender = mailSenderObject;
		}
		return mailSender;

	}

	public boolean isLongOperation(EmailNotification notification) {
		// Sending email can be a time consuming task
		return true;
	}

}
