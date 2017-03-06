package org.gumtree.msw.schedule.execution;

import java.util.Map;

public class AcquisitionSummary extends Summary {
	// fields
	private final String filename;
	private final long totalSeconds;
	private final long totalCounts;
	private final long monitorCounts;
	
	// construction
	public AcquisitionSummary(Map<String, Object> parameters, long processingTime, boolean interrupted, String notes) {
		this(parameters, null, 0, 0, 0, processingTime, interrupted, notes);
	}
	public AcquisitionSummary(Map<String, Object> parameters, String filename, long totalSeconds, long totalCounts, long monitorCounts, long processingTime, boolean interrupted, String notes) {
		super(parameters, processingTime, interrupted, notes);

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
