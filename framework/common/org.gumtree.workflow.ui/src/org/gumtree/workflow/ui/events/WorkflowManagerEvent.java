package org.gumtree.workflow.ui.events;

import org.gumtree.service.eventbus.Event;
import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.util.IWorkflowManager;
import org.gumtree.workflow.ui.util.WorkflowManagerOperation;

public class WorkflowManagerEvent extends Event {

	private WorkflowManagerOperation operation;
	
	private IWorkflow target;
	
	public WorkflowManagerEvent(IWorkflowManager manager,
			WorkflowManagerOperation operation, IWorkflow target) {
		super(manager);
		this.operation = operation;
		this.target = target;
	}

	public IWorkflowManager getWorkflowManager() {
		return (IWorkflowManager) getPublisher();
	}

	public WorkflowManagerOperation getOperation() {
		return operation;
	}

	public IWorkflow getTarget() {
		return target;
	}

}
