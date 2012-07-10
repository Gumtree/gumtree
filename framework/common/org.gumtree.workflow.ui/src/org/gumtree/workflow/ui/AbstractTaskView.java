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

package org.gumtree.workflow.ui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.util.PlatformUtils;
import org.gumtree.workflow.ui.events.TaskViewEvent;
import org.gumtree.workflow.ui.events.TaskViewEvent.TaskViewEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractTaskView provides the base implementation of ITaskView. Any
 * implementation of ITaskView should subclass this class.
 * 
 * @since 1.0
 */
public abstract class AbstractTaskView implements ITaskView {

	private static Logger logger = LoggerFactory.getLogger(AbstractTaskView.class);
	
	private FormToolkit toolkit;
	
	private IControllableTask task;
	
	private boolean isDisposed;
	
	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.ITaskView#getTask()
	 */
	public IControllableTask getTask() {
		return task;
	}
	
	/**
	 * Sets the associated task for this view. This method is used by
	 * AbstractTask to inject the task instance.
	 * 
	 * @param task
	 */
	public void setTask(IControllableTask task) {
		this.task = task;
	}
	
	/**
	 * Returns the workflow context.
	 * 
	 * @return workflow context from its associated task
	 */
	protected IWorkflowContext getContext() {
		return getTask().getWorkflow().getContext();
	}
	
	/**
	 * Returns an instance of form toolkit for convenience reason.
	 * 
	 * @return
	 */
	protected FormToolkit getToolkit() {
		if (toolkit == null) {
			toolkit = new FormToolkit(Display.getDefault());
		}
		return toolkit;
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.ui.util.IPartControlProvider#isDisposed()
	 */
	public boolean isDisposed() {
		return isDisposed;
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.ui.util.IPartControlProvider#dispose()
	 */
	public void dispose() {
		if (isDisposed()) {
			logger.info("Unnecessary dispose method call.");
			return;
		}
		isDisposed = true;
		if (toolkit != null) {
			toolkit.dispose();
			toolkit = null;
		}
		task = null;
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.ui.util.IPartControlProvider#setFocus()
	 */
	public void setFocus() {
	}
	
	/**
	 * Refreshes the UI component.  Clients can call this method
	 * to refresh its parent UI explicitly.
	 */
	public void fireRefresh() {
		PlatformUtils.getPlatformEventBus().postEvent(new TaskViewEvent(this, TaskViewEventType.REFRESH));
	}
	
	public void addEventListener(IEventHandler<TaskViewEvent> listener) {
		PlatformUtils.getPlatformEventBus().subscribe(this, listener);
	}
	
	public void removeEventListener(IEventHandler<TaskViewEvent> listener) {
		PlatformUtils.getPlatformEventBus().unsubscribe(this, listener);
	}
	
}
