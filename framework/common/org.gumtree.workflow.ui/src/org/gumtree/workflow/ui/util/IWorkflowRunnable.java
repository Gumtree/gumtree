package org.gumtree.workflow.ui.util;

import org.gumtree.workflow.ui.IWorkflow;

public interface IWorkflowRunnable extends Runnable {

	public IWorkflow getWorkflow();
	
}
