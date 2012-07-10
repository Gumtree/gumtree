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

package org.gumtree.sics.io;

public class SicsCommunicationConstants {

	public enum JSONTag {
		CONNECTION("con"), TRANSACTION("trans"), OBJECT("object"), FLAG("flag"), DATA(
				"data");

		private JSONTag(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}

		private String text;
	}

	public enum Flag {
		/**
		 * Normal output
		 */
		OUT,

		/**
		 * Status output
		 */
		STATUS,

		/**
		 * Event output
		 */
		EVENT,

		/**
		 * Warning output
		 */
		WARNING,

		/**
		 * Error output
		 */
		ERROR,

		/**
		 * Finish output
		 */
		FINISH
	}

	public static final String CMD_SET_JSON_PROTOCOL = "protocol set json";

	public static final String CMD_GLOBAL_NOTIFY = "hnotify / 1";

	public static final String REPLY_OK = "OK";

	public static final String REPLY_LOGIN_OK = "Login OK";

	public static final String REPLY_BAD_LOGIN = "ERROR: Bad login";

	private SicsCommunicationConstants() {
		super();
	}

}
