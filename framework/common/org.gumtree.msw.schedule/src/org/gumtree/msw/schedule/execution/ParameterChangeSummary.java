package org.gumtree.msw.schedule.execution;

import java.util.Map;

public class ParameterChangeSummary extends Summary {
	// fields
	private final String name; 
	private final Map<String, Object> parameters;
	
	// construction
	public ParameterChangeSummary(String name, Map<String, Object> parameters, long processingTime) {
		this(name, parameters, processingTime, false);
	}
	public ParameterChangeSummary(boolean interrupted) {
		this(null, null, 0, interrupted);
	}
	public ParameterChangeSummary(String name, Map<String, Object> parameters, long processingTime, boolean interrupted) {
		super(processingTime, interrupted);
		
		this.name = name;
		this.parameters = parameters;
	}

	// properties
	public Object getName() {
		return name;
	}
	public Map<String, Object> getParameters() {
		return parameters;
	}
}
