/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.workflow.ui.viewer2;

import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.forms.FormControlWidget;
import org.gumtree.workflow.ui.IWorkflow;

public abstract class AbstractWorkflowViewerComponent extends FormControlWidget implements IWorkflowViewerComponent {

	private IWorkflow workflow;
	
	private IWorkflowViewer workflowViewer;
	
	// Flag to indicate if this widget has been rendered before
	private boolean initialised = false;
	
	public AbstractWorkflowViewerComponent(Composite parent, int style) {
		super(parent, style);
	}

	protected void widgetDispose() {
		componentDispose();
		workflowViewer = null;
		workflow = null;
	}
	
	public void afterParametersSet() {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				createUI();
				initialised = true;
			}
		});
	}
	
	protected abstract void componentDispose();

	protected abstract void createUI();
	
	protected abstract void refreshUI();
	
	public IWorkflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(IWorkflow workflow) {
		this.workflow = workflow;
		if (initialised) {
			SafeUIRunner.asyncExec(new SafeRunnable() {
				public void run() throws Exception {
					refreshUI();
				}
			});
		}
	}
	
	public IWorkflowViewer getWorkflowViewer() {
		return workflowViewer;
	}
	
	public void setWorkflowViewer(IWorkflowViewer workflowViewer) {
		this.workflowViewer = workflowViewer;
	}

	protected void configureViewerComponent(IWorkflowViewerComponent component) {
		if (component != null) {
			component.setWorkflow(getWorkflow());
			component.setWorkflowViewer(getWorkflowViewer());
			component.afterParametersSet();
			getWorkflowViewer().addViewerComponent(component);
		}
	}
	
}
