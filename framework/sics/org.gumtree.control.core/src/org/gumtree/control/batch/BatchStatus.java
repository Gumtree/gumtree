package org.gumtree.control.batch;

public enum BatchStatus {
	DISCONNECTED, IDLE, RUNNING, ERROR;

	public static BatchStatus parseStatus(String text) {
		return BatchStatus.valueOf(text.toUpperCase());
	}
}
