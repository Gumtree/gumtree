package org.gumtree.workflow.ui.util;

import org.gumtree.core.service.IService;


public interface IWorkflowRegistry extends IService {

	public IWorkflowDescriptor[] getDescriptors();
	
	public IWorkflowDescriptor getDescriptor(String id);
	
}
