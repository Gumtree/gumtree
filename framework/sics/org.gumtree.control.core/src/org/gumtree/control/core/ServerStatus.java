package org.gumtree.control.core;

public enum ServerStatus {
	UNKNOWN("UNKNOWN"), EAGER_TO_EXECUTE("EAGER TO EXECUTE"), COUNTING("COUNTING"), 
	DRIVING("DRIVING"), WAIT("WAIT"), PAUSED("PAUSED"), RUNNING_A_SCAN("RUNING A SCAN"),
	PROCESSING_A_BATCH_FILE("PROCESSING A BATCH FILE"),	NO_BEAM("NO BEAM"),	
	WRITING_DATA("WRITING DATA"), HALTED("HALTED"), 
	WAITING_FOR_USER_INPUT("WAITING FOR USER INPUT"), WORKING("WORKING");

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
		} else if (message.startsWith("running a scan")) {
			return RUNNING_A_SCAN;
		} else if (message.startsWith("processing a batch file")) {
			return PROCESSING_A_BATCH_FILE;
		} else if (message.startsWith("paused")) {
			return PAUSED;
		} else if (message.startsWith("user requested wait")) {
			return WAIT;
		} else if (message.startsWith("no beam")) {
			return NO_BEAM;
		} else if (message.startsWith("writing data")) {
			return WRITING_DATA;
		} else if (message.startsWith("halted")) {
			return HALTED;
		} else if (message.startsWith("working")) {
			return WORKING;
		} else if (message.startsWith("waiting for user input")) {
			return WAITING_FOR_USER_INPUT;
		} else {
			return UNKNOWN;
		}
	}

	private String text;
}
