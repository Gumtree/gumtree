package org.gumtree.workflow.ui.internal;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.gumtree.workflow.ui.IWorkflow;

public class WorkflowEditorInput implements IEditorInput {

	private IWorkflow workflow;
	
	public WorkflowEditorInput(IWorkflow workflow) {
		this.workflow = workflow;
	}
	
	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return InternalImage.WORKFLOW.getDescriptor();
	}

	public String getName() {
		return "Workflow";
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return "Workflow";
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkflow.class) {
			return workflow;
		}
		return Platform.getAdapterManager().getAdapter(workflow, adapter);
	}

}
