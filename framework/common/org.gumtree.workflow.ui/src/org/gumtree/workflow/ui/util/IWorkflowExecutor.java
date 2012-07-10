package org.gumtree.workflow.ui.util;

import org.gumtree.core.service.IService;
import org.gumtree.workflow.ui.IWorkflow;

public interface IWorkflowExecutor extends IService {

	public IWorkflowRunnable[] getScheduledWorkflow();
	
	public void schedule(IWorkflow workflow);
	
	public void stop(IWorkflow workflow);
	
}
