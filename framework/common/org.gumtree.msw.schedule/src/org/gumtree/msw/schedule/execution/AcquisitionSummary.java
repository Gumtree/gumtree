package org.gumtree.msw.schedule.execution;

public class AcquisitionSummary extends Summary {
	// fields
	private final String filename;
	private final long totalSeconds;
	private final long totalCounts;
	private final long monitorCounts;
	
	// construction
	public AcquisitionSummary(String filename, long totalSeconds, long totalCounts, long monitorCounts, long processingTime) {
		this(filename, totalSeconds, totalCounts, monitorCounts, processingTime, false);
	}
	public AcquisitionSummary(long processingTime, boolean interrupted) {
		this(null, 0, 0, 0, processingTime, interrupted);
	}
	public AcquisitionSummary(String filename, long totalSeconds, long totalCounts, long monitorCounts, long processingTime, boolean interrupted) {
		super(processingTime, interrupted);
		
		this.filename = filename;
		this.totalSeconds = totalSeconds;
		this.totalCounts = totalCounts;
		this.monitorCounts = monitorCounts;
	}

	// properties
	public String getFilename() {
		return filename;
	}
	public long getTotalSeconds() {
		return totalSeconds;
	}
	public long getTotalCounts() {
		return totalCounts;
	}
	public long getMonitorCounts() {
		return monitorCounts;
	}
}
