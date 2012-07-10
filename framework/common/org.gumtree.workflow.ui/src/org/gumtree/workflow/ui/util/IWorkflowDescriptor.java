package org.gumtree.workflow.ui.util;

import org.eclipse.jface.resource.ImageDescriptor;
import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.WorkflowException;

public interface IWorkflowDescriptor {

	public String getId();
	
	public String getLabel();
	
	public String getDescription();
	
	public String getCategory();
	
	public String[] getTags();
	
	public ImageDescriptor getIcon();
	
	public IWorkflow createWorkflow() throws WorkflowException;
	
}
