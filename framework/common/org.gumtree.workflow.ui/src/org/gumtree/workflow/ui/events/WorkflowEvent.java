package org.gumtree.workflow.ui.events;

import org.gumtree.service.eventbus.Event;
import org.gumtree.workflow.ui.IWorkflow;

public class WorkflowEvent extends Event {
	
	public WorkflowEvent(IWorkflow workflow) {
		super(workflow);
	}

	public IWorkflow getWorkflow() {
		return (IWorkflow) getPublisher();
	}
	
}
