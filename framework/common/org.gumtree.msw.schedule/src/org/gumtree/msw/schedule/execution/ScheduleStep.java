package org.gumtree.msw.schedule.execution;

import java.util.HashMap;
import java.util.Map;

import org.gumtree.msw.elements.ElementPath;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.schedule.ScheduledNode;

public class ScheduleStep {
	// fields
	private final ScheduledNode scheduledNode;
	private final ElementPath elementPath; 
	private final Map<String, Object> parameters;
	private final boolean hasParameters;
	private final boolean isEnabled;
	private final boolean isAcquisition;
	
	// construction
	public ScheduleStep(ScheduledNode scheduledNode, boolean isEnabled, boolean isAcquisition) {
		this.scheduledNode = scheduledNode;
		this.elementPath = scheduledNode.getSourceElement().getPath();
		this.parameters = createPropertyMap(scheduledNode);
		this.hasParameters = true;
		
		this.isEnabled = isEnabled;
		this.isAcquisition = isAcquisition;
	}

	// properties
	public boolean isEnabled() {
		return isEnabled;
	}
	public ScheduledNode getScheduledNode() {
		return scheduledNode;
	}
	public ElementPath getElementPath() {
		return elementPath;
	}
	public Map<String, Object> getParameters() {
		return parameters;
	}
	public boolean hasParameters() {
		return hasParameters;
	}
	public boolean isAcquisition() {
		return isAcquisition;
	}
	
	// helpers
	private static Map<String, Object> createPropertyMap(ScheduledNode scheduledNode) {
		Map<String, Object> result = new HashMap<>();
		for (IDependencyProperty property : scheduledNode.getProperties())
			result.put(property.getName(), scheduledNode.get(property));
		return result;
	}
}
