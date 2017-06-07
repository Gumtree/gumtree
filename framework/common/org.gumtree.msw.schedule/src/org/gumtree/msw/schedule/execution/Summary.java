package org.gumtree.msw.schedule.execution;

import java.util.Map;

public class Summary {
	// fields
	private final Map<String, Object> parameters;
	private final long processingTime; // seconds
	private final boolean interrupted;
	private final String notes;

	// construction
	public Summary(Map<String, Object> parameters, long processingTime, boolean interrupted, String notes) {
		this.parameters = parameters;
		this.processingTime = processingTime;
		this.interrupted = interrupted;
		this.notes = notes;
	}
	
	// properties
	public Map<String, Object> getParameters() {
		return parameters;
	}
	public long getProcessingTime() {
		return processingTime;
	}
	public boolean getInterrupted() {
		return interrupted;
	}
	public String getNotes() {
		return notes;
	}
}
