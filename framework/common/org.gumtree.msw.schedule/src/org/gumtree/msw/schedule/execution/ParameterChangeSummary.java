package org.gumtree.msw.schedule.execution;

import java.util.Map;

public class ParameterChangeSummary extends Summary {
	// fields
	private final String name; 
	
	// construction
	public ParameterChangeSummary(String name, Map<String, Object> parameters, long processingTime, boolean interrupted, String notes) {
		super(parameters, processingTime, interrupted, notes);
		
		this.name = name;
	}

	// properties
	public Object getName() {
		return name;
	}
}
