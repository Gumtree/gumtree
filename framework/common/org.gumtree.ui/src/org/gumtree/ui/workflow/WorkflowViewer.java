package org.gumtree.ui.workflow;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.widgets.swt.forms.ExtendedFormComposite;
import org.gumtree.workflow.IWorkflow;

public class WorkflowViewer extends ExtendedFormComposite {

	@Inject
	@Optional
	private IWorkflow workflow;
	
	public WorkflowViewer(Composite parent, int style) {
		super(parent, style);
	}

	@PostConstruct
	public void render() {
		
	}
	
	@Override
	protected void disposeWidget() {
	}

	/*************************************************************************
	 * Components
	 *************************************************************************/
	
	public IWorkflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(IWorkflow workflow) {
		this.workflow = workflow;
	}

	
}
