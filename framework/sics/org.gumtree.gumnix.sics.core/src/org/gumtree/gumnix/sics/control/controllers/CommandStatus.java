package org.gumtree.gumnix.sics.control.controllers;

public enum CommandStatus {

	IDLE, BUSY, STARTING, PAUSED, UNKNOWN;
	
	public static CommandStatus getStatus(String value) {
		if (value == null) {
			return UNKNOWN;
		}
		for (CommandStatus status : values()) {
			if (status.name().equalsIgnoreCase(value)) {
				return status;
			}
		}
		return UNKNOWN;
	}
	
}
