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

public class EmailNotification extends AbstractNotification {

	private static final long serialVersionUID = 9156098384497870205L;

	private static final String PROTOCOL = "email";

	private String from;

	private String to;

	private String subject;

	private String text;

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getProtocol() {
		return PROTOCOL;
	}

	@Override
	public String toString() {
		return "EmailNotification [from=" + from + ", to=" + to + ", subject="
				+ subject + ", text=" + text + "]";
	}

}
