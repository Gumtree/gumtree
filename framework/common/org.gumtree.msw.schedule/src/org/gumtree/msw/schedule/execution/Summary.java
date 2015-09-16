package org.gumtree.msw.schedule.execution;

public class Summary {
	// fields
	private final long processingTime;
	private final boolean interrupted;

	// construction
	public Summary(long processingTime, boolean interrupted) {
		this.processingTime = processingTime;
		this.interrupted = interrupted;
	}
	
	// properties
	public long getProcessingTime() {
		return processingTime;
	}
	public boolean getInterrupted() {
		return interrupted;
	}
}
