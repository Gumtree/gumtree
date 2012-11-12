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

package org.gumtree.gumnix.sics.control;

public enum ServerStatus {

	UNKNOWN("UNKNOW"), EAGER_TO_EXECUTE("EAGER TO EXECUTE"), COUNTING("COUNTING"), DRIVING("DRIVING"), WAIT("WAIT"), PAUSED("PAUSED");

	private ServerStatus(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	public static ServerStatus parseStatus(String message) {
		if (message == null) {
			return UNKNOWN;
		}
		message = message.trim();
		if (message.startsWith("Eager")) {
			return EAGER_TO_EXECUTE;
		} else if (message.startsWith("Counting")) {
			return COUNTING;
		} else if (message.startsWith("Driving")) {
			return DRIVING;
		} else if (message.startsWith("Paused")) {
			return PAUSED;
		} else if (message.startsWith("User requested Wait")) {
			return WAIT;
		} else {
			return UNKNOWN;
		}
	}
	
	private String text;
	
}
