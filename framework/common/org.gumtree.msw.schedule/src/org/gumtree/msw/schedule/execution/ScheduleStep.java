package org.gumtree.msw.schedule.execution;

import java.util.HashMap;
import java.util.Map;

import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.ElementPath;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.schedule.ScheduledNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleStep {
	
	private static final Logger logger = LoggerFactory.getLogger(ScheduleStep.class);
	
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
		logger.error(String.format("%s, isEnabled=%b", this.elementPath, isEnabled));
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
		
		Element element = scheduledNode.getSourceElement();
		result.put("ElementName", element.getPath().getElementName());
		result.put("ElementPath", element.getPath().toString());
		result.put("ElementRoot", element.getPath().getRoot().toString());
		
		for (IDependencyProperty property : scheduledNode.getProperties())
			result.put(property.getName(), scheduledNode.get(property));
		
		return result;
	}
}
