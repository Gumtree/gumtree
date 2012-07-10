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

import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.ui.util.workbench.IPartControlProvider;
import org.gumtree.workflow.ui.events.TaskViewEvent;

/**
 * Task view is the UI representation of a task.
 * 
 * @since 1.0
 */
public interface ITaskView extends IPartControlProvider {

	/**
	 * Gets the task associates with this view.
	 * 
	 * @return the associated task
	 */
	public IControllableTask getTask();
	
	/**
	 * Adds an event listener to this task.
	 * 
	 * @param listener	an event listener
	 * @see				TaskViewEvent
	 */
	public void addEventListener(IEventHandler<TaskViewEvent> listener);
	
	/**
	 * Removes an event listener from this task view.
	 * 
	 * @param listener	an event listener
	 */
	public void removeEventListener(IEventHandler<TaskViewEvent> listener);
	
}
