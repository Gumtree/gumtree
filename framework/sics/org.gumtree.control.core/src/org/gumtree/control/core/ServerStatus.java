package org.gumtree.control.core;

public enum ServerStatus {
	UNKNOWN("UNKNOWN"), EAGER_TO_EXECUTE("EAGER TO EXECUTE"), COUNTING(
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
