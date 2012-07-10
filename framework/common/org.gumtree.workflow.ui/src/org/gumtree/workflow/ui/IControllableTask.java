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

import java.util.List;

import org.gumtree.core.object.ObjectConfigException;
import org.gumtree.util.collection.IParameters;

/**
 * A controllable task interface adds behaviour methods a task. This is used
 * primarily by the workflow object (the task controller).
 * 
 * @since 1.0
 */
public interface IControllableTask extends ITask {

	/**
	 * Initialises this task.  This method is called by the workflow during
	 * the creation of this task.
	 * 
	 * @param parameters				the configuration information for this task
	 * @throws WorkflowConfigException	when this task has failed to initialise from the given config
	 */
	public void configure(IParameters parameters, Object dataModel) throws ObjectConfigException;

	/**
	 * 
	 */
	public void initialise();
	
	/**
	 * 
	 */
	public void dispose();
	
	/**
	 * Sets a unique ID to this task.  This method should be called once only.
	 * 
	 * @param id	a unique ID assigned by the workflow to this task
	 */
	public void setId(long id);
	
	/**
	 * Sets the state of this state.
	 * 
	 * @param state	the current state of this task
	 */
	public void setState(TaskState state);
	
	/**
	 * @param workflow
	 */
	public void setWorkflow(IWorkflow workflow);
	
	/**
	 * Clear the task buffer and state. This method is called prior to run this
	 * task. 
	 */
	public void clearTask();
	
	/*************************************************************************
	 * Task execution 
	 *************************************************************************/
	
	/**
	 * Executes this task.
	 * 
	 * @throws WorkflowException
	 */
	public List<Object> runTask(List<Object> input) throws WorkflowException;
	
	/**
	 * Attempts to stop this task.  Once this method is called, the workflow
	 * will assume this task is stopped, but the internal task execution may
	 * still be running.
	 */
	public void stopTask();
	
	/**
	 * Attempts to pause this task.
	 */
	public void pauseTask();
	
	/**
	 * Attempts to resume this task from a paused state.
	 */
	public void resumeTask();
	
	/**
	 * Set label to the task.
	 * @param label
	 */
	public void setLabel(String label);
}
