/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package org.gumtree.workflow.ui.viewer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.workflow.ui.ITask;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.events.TaskViewEvent;
import org.gumtree.workflow.ui.internal.InternalImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractWorkflowViewer implements IWorkflowViewer {

	private static Logger logger = LoggerFactory.getLogger(AbstractWorkflowViewer.class);
	
	private IWorkflow workflow;
	
	private FormToolkit toolkit;
	
	private boolean isDisposed;
	
	private List<ITaskView> taskViews;
	
	private IEventHandler<TaskViewEvent> taskViewEventHandler;
	
	public AbstractWorkflowViewer() {
		taskViews = new ArrayList<ITaskView>(2);
		taskViewEventHandler = new IEventHandler<TaskViewEvent>() {
			public void handleEvent(TaskViewEvent event) {
				handleTaskViewRefreshEvent(event.getPublisher());
			}
		};
	}
	
	/*************************************************************************
	 * Getters & Setters 
	 *************************************************************************/
	
	public IWorkflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(IWorkflow workflow) {
		// Only set workflow once
//		if (getWorkflow() == null) {
			this.workflow = workflow;
//		}
	}
	
	public List<ITaskView> getTaskViews() {
		return taskViews;
	}
	
	/*************************************************************************
	 * UI related methods 
	 *************************************************************************/
	
	public void createPartControl(Composite parent) {
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				// Dispose this viewer upon the disposal of parent
				dispose();
			}		
		});
		createViewerControl(parent);
		setFocus();
	}
	
	public void setFocus() {
	}
	
	protected abstract void createViewerControl(Composite parent);
	
	protected abstract void handleTaskViewRefreshEvent(ITaskView taskView);

	
	/*************************************************************************
	 * Helper function for object creation 
	 *************************************************************************/
	
	protected FormToolkit getToolkit() {
		if (toolkit == null) {
			toolkit = new FormToolkit(Display.getDefault());
		}
		return toolkit;
	}
	
	protected ITaskView createTaskView(ITask task) {
		ITaskView taskView = task.createTaskView();
		taskView.addEventListener(taskViewEventHandler);
		taskViews.add(taskView);
		return taskView;
	}
	
	// Note: this method does not remove taskView from cache
	protected void destoryTaskView(ITaskView taskView) {
		taskView.removeEventListener(taskViewEventHandler);
		taskView.dispose();
	}

	protected void createErrorTaskView(Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		Label label = getToolkit().createLabel(parent, "");
		label.setImage(InternalImage.ERROR_TASK.getImage());
		getToolkit().createLabel(parent, "Cannot create task UI.");
	}
	
	/*************************************************************************
	 * Disposal methods
	 *************************************************************************/
	
	public boolean isDisposed() {
		return isDisposed;
	}
	
	public void dispose() {
		if (isDisposed()) {
			logger.info("Unnecessary dispose method call.");
			return;
		}
		if (taskViews != null) {
			for (ITaskView taskView : taskViews) {
				destoryTaskView(taskView);
			}
			taskViews.clear();
			taskViews = null;
		}
		if (toolkit != null) {
			toolkit.dispose();
			toolkit = null;
		}
		if (workflow != null) {
			workflow.dispose();
			workflow = null;
		}
		taskViewEventHandler = null;
		isDisposed = true;
	}
	
}
