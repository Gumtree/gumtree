package org.gumtree.msw.schedule.execution;

import java.util.Map;

public class InitializationSummary extends Summary {
	// fields
	private final String proposalNumber;
	private final String experimentTitle;
	private final String sampleStage;
	
	// construction
	public InitializationSummary(Map<String, Object> parameters, long processingTime, boolean interrupted, String notes) {
		this(parameters, null, null, null, processingTime, interrupted, notes);
	}
	public InitializationSummary(Map<String, Object> parameters, String proposalNumber, String experimentTitle, String sampleStage, long processingTime, boolean interrupted, String notes) {
		super(parameters, processingTime, interrupted, notes);

		this.proposalNumber = proposalNumber;
		this.experimentTitle = experimentTitle;
		this.sampleStage = sampleStage;
	}

	// properties
	public String getProposalNumber() {
		return proposalNumber;
	}
	public String getExperimentTitle() {
		return experimentTitle;
	}
	public String getSampleStage() {
		return sampleStage;
	}
}
