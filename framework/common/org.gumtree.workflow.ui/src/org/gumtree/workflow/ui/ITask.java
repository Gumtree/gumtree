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

import org.eclipse.swt.graphics.Image;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.util.collection.IParameters;
import org.gumtree.workflow.ui.events.TaskEvent;

/**
 * Task is an unit of executable block in a workflow. 
 * 
 * @since 1.0
 */
public interface ITask {
	
	/**
	 * Returns the unique ID of this task.
	 * 
	 * @return the task's unique ID
	 */
	public long getId();
	
	/**
	 * Returns the label of this task.
	 * 
	 * @return the task's label
	 */
	public String getLabel();
	
	
	/**
	 * Returns the color description of the task
	 * @return String value
	 */
	public String getColorString();
	/**
	 * Returns the icon of this task.
	 * 
	 * @return the task's icon
	 */
	public Image getIcon();
	
	/**
	 * Indicates whether this task is visible to the UI or not.
	 * 
	 * @return the visibility of this task in the workflow
	 */
	public boolean isVisible();
	
	/**
	 * Indicates whether this task is enabled in the workflow or not
	 * 
	 * @return the enability of this task in the workflow
	 */
	public boolean isEnable();
	
	/**
	 * Returns the core data model this task represents.
	 * 
	 * @return the data model of this task
	 */
	public Object getDataModel();
	
	/**
	 * Returns the configuration parameters for this task.
	 * 
	 * @return parameters
	 */
	public IParameters getParameters();
	
	/**
	 * Persists configuration parameters.  This method is called
	 * when its belonging workflow is saved.
	 * 
	 * @param parameters the configuration parameters to persist
	 */
	public void persist(IParameters parameters);
	
	/**
	 * Returns the workflow that manages this task.
	 *  
	 * @return	the managing workflow
	 */
	public IWorkflow getWorkflow();
	
	/*************************************************************************
	 * Task event 
	 *************************************************************************/
	
	/**
	 * Adds an event listener to this task.  Event listener will be
	 * notified when task state is changed.
	 * 
	 * @param listener	an event listener
	 * @see				TaskEvent
	 */
	public void addEventListener(IEventHandler<TaskEvent> listener);
	
	/**
	 * Removes an event listener from this task.  It has no effect if
	 * the supplied listener has not been previously added to this task.
	 * 
	 * @param listener	an event listener
	 */
	public void removeEventListener(IEventHandler<TaskEvent> listener);
	
	/**
	 * Creates a visual representation of this task.
	 *  
	 * @return	a new UI view that represent this task
	 */
	public ITaskView createTaskView();
	
	/**
	 * Returns the current state of this task.
	 * 
	 * @return the task's state
	 */
	public TaskState getState();
	
	/*************************************************************************
	 * Task data flow
	 *************************************************************************/
	
	/**
	 * Returns a set of supported input types.
	 * 
	 * @return
	 */
	public Class<?>[] getInputTypes();
	
	/**
	 * @return
	 */
	public boolean isInputSupported();
	
	/**
	 * Returns a set of supported output types.
	 * 
	 * @return
	 */
	public Class<?>[] getOutputTypes();
	
	/**
	 * @return
	 */
	public boolean isOutputSupported();
	
	/**
	 * If this is false, all inputs from the previous task will be sent in one call.
	 * 
	 * @return
	 */
	public boolean isMultiInputSupported();
	
	/**
	 * Gets the current list of run results. Results stay in memory unless
	 * clearRunResults() is called.
	 * 
	 * @return an array of result
	 */
	public Object[] getRunResults();
	
}
