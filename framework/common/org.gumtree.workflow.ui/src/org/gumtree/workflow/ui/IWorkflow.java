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

import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.util.collection.IParameters;
import org.gumtree.workflow.ui.events.WorkflowEvent;

/**
 * Workflow is a sequence of tasks that can be executed in a linear order.
 * 
 * @author 	Tony Lam
 * @since 	1.2
 */
public interface IWorkflow {

	/*************************************************************************
	 * Workflow information
	 *************************************************************************/
	
	/**
	 * Returns the context map of this workflow. Context is a useful way to store
	 * shared objects across all tasks.
	 * 
	 * @return	context of this workflow
	 */
	public IWorkflowContext getContext();

	/**
	 * Returns the configuration parameters for this workflow. The parameter map
	 * is used when worklow is loaded or saved.  It generally contains metadata for
	 * configuring the workflow UI appearance.
	 * 
	 * @return	parameter map
	 */
	public IParameters getParameters();
	
	/**
	 * Returns the assigned id of this workflow from the workflow manager.
	 * The assigned id will return 0 if this workflow has not been assigned
	 * with an unique id.
	 * 
	 * @return	assigned id from the workflow manager
	 */
	public long getAssignedId();

	/*************************************************************************
	 * Workflow I/O
	 *************************************************************************/

	/**
	 * Sets the initial input for the first task before execution.
	 * 
	 * @return
	 */
	public void setRunInput(Object input);

	
	/**
	 * Sets the initial input list for the first task before execution.
	 * 
	 * @return
	 */
	public void setRunInputs(List<Object> inputs);
	
	/**
	 * Returns the results from the last run.
	 * 
	 * @return
	 */
	public List<Object> getRunResults();
	
	/**
	 * Clears the input and result of this workflow
	 */
	public void clearData();
	
	/*************************************************************************
	 * Workflow structure
	 *************************************************************************/
	
	/**
	 * Returns a list of immutable tasks that is managed by this workflow.
	 * 
	 * @return	tasks in this workflow
	 */
	public List<ITask> getTasks();
	
	/**
	 * Appends a new task to this workflow.
	 * 
	 * @param task	task to append
	 */
	public void addTask(ITask task);
	
	/**
	 * Remove an existing task from this workflow.
	 * 
	 * @param task	task to remove
	 */
	public void removeTask(ITask task);
	
	/**
	 * Inserts a new task to this workflow.
	 * 
	 * @param index	insertion index
	 * @param task	task to insert
	 */
	public void insertTask(int index, ITask task);
	
	/**
	 * Insert a list of new tasks to this workflow.
	 * @param index insertion index
	 * @param taskList a list of tasks
	 */
	void insertTasks(int index, List<ITask> taskList);
	
	/**
	 * Swaps two existing tasks.
	 * 
	 * @param task1
	 * @param task2
	 */
	public void swapTask(ITask task1, ITask task2);
	
	/**
	 * Set index of an existing task.
	 * 
	 * @param index
	 * @param task
	 */
	public void setTask(int index, ITask task);
	
	/*************************************************************************
	 * Workflow execution
	 *************************************************************************/
	
	/**
	 * Run this workflow.
	 * 
	 * @throws WorkflowException	when in invalid state
	 */
	public void run() throws WorkflowException;
	
	/**
	 * Stop this workflow.
	 * 
	 * @throws WorkflowException	when in invalid state
	 */
	public void stop() throws WorkflowException;
	
	/**
	 * Pause this workflow.
	 * 
	 * @throws WorkflowException	when in invalid state
	 */
	public void pause() throws WorkflowException;
	
	/**
	 * Resume this workflow.
	 * 
	 * @throws WorkflowException	 when in invalid state
	 */
	public void resume() throws WorkflowException;
	
	/**
	 * Dispose this workflow
	 */
	public void dispose();
	
	/*************************************************************************
	 * Workflow status
	 *************************************************************************/
	
	public WorkflowState getState();
	
	/**
	 * Adds a workflow event listener to this workflow.
	 * 
	 * @param listener	a workflow event handler
	 */
	public <T extends WorkflowEvent> void addEventListener(IEventHandler<T> listener);
	
	/**
	 * Removes a workflow event listener to this workflow.
	 * 
	 * @param listener	a registered workflow event handler
	 */
	public <T extends WorkflowEvent> void removeEventListener(IEventHandler<T> listener);


	
}
