package org.gumtree.control.batch;

public enum BatchStatus {
	DISCONNECTED, IDLE, PREPARING, EXECUTING, ERROR;

	public static String RUNNING_TEXT = "Executing";
	public static String IDLE_TEXT = "Idle";
	public static String START_STATE = "STARTED";
	public static String FINISH_STATE = "FINISH";
	public static String RANGE_STATE = "RANGE";
	public static String EXE_PREFIX = "exe ";
	public static String EXECUTING_PREFIX = "Executing ";
	
	public static BatchStatus parseStatus(String text) {
		
		return BatchStatus.valueOf(text.toUpperCase());
	}
}
