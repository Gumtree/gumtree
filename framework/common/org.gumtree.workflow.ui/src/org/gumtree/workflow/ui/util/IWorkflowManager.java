package org.gumtree.workflow.ui.util;

import org.gumtree.core.service.IService;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.events.WorkflowManagerEvent;

public interface IWorkflowManager extends IService {

	public void appendWorkflow(IWorkflow workflow);
	
	public void removeWorkflow(IWorkflow workflow);
	
	public IWorkflow[] getWorkflowQueue();
	
	public void addEventListener(IEventHandler<WorkflowManagerEvent> listener);
	
	public void removeEventListener(IEventHandler<WorkflowManagerEvent> listener);
	
}
