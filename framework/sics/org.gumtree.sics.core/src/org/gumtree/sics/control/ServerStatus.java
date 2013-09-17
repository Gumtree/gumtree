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

package org.gumtree.sics.control;

public enum ServerStatus {

	UNKNOWN("UNKNOW"), EAGER_TO_EXECUTE("EAGER TO EXECUTE"), COUNTING(
			"COUNTING"), DRIVING("DRIVING"), WAIT("WAIT"), PAUSE("PAUSE");

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
		message = message.trim().toLowerCase();
		if (message.startsWith("eager")) {
			return EAGER_TO_EXECUTE;
		} else if (message.startsWith("counting")) {
			return COUNTING;
		} else if (message.startsWith("driving")) {
			return DRIVING;
		} else if (message.startsWith("pause")) {
			return PAUSE;
		} else if (message.startsWith("user requested wait")) {
			return WAIT;
		} else {
			return UNKNOWN;
		}
	}

	private String text;

}
