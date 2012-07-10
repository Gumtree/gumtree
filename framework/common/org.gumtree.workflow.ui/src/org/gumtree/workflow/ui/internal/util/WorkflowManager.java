package org.gumtree.workflow.ui.internal.util;

import java.util.List;
import java.util.Vector;

import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.util.PlatformUtils;
import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.events.WorkflowManagerEvent;
import org.gumtree.workflow.ui.internal.Workflow;
import org.gumtree.workflow.ui.util.IWorkflowManager;
import org.gumtree.workflow.ui.util.WorkflowManagerOperation;

public class WorkflowManager implements IWorkflowManager {

	private List<IWorkflow> workflowQueue;
	
	private static long workflowId;
	
	public WorkflowManager() {
		super();
		workflowQueue = new Vector<IWorkflow>();
		workflowId = 1;
	}
	
	public void appendWorkflow(IWorkflow workflow) {
		workflowQueue.add(workflow);
		// assign unique id if possible
		if (workflow instanceof Workflow) {
			((Workflow) workflow).assignedId(workflowId++);
		}
		// notify listeners
		WorkflowManagerEvent event = new WorkflowManagerEvent(this,
				WorkflowManagerOperation.APPEND_WORKFLOW, workflow);
		PlatformUtils.getPlatformEventBus().postEvent(event);
	}
	
	public void removeWorkflow(final IWorkflow workflow) {
		workflowQueue.remove(workflow);
		WorkflowManagerEvent event = new WorkflowManagerEvent(this,
				WorkflowManagerOperation.REMOVE_WORKFLOW, workflow);
		PlatformUtils.getPlatformEventBus().postEvent(event);
	}
	
	public IWorkflow[] getWorkflowQueue() {
		synchronized(workflowQueue) {
			return workflowQueue.toArray(new IWorkflow[workflowQueue.size()]);
		}
	}

	public void addEventListener(IEventHandler<WorkflowManagerEvent> listener) {
		PlatformUtils.getPlatformEventBus().subscribe(this, listener);
	}

	public void removeEventListener(IEventHandler<WorkflowManagerEvent> listener) {
		PlatformUtils.getPlatformEventBus().unsubscribe(this, listener);
	}
	
}
