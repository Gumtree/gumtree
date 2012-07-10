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

package org.gumtree.workflow.ui.util;

import static org.gumtree.workflow.ui.config.WorkflowConfigConstants.PARAM_INTRO_CLASS;
import static org.gumtree.workflow.ui.config.WorkflowConfigConstants.PARAM_TITLE;

import org.gumtree.core.object.ObjectCreateException;
import org.gumtree.core.object.ObjectFactory;
import org.gumtree.util.PropertiesHelper;
import org.gumtree.workflow.ui.AbstractMarkerTask;
import org.gumtree.workflow.ui.DefaultWorkflowIntroView;
import org.gumtree.workflow.ui.ITask;
import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.IWorkflowIntroView;
import org.gumtree.workflow.ui.MarkerType;
import org.gumtree.workflow.ui.config.WorkflowConfigConstants;

public final class WorkflowUtils {

	public static ITask getNextTask(IWorkflow workflow, ITask task) {
		int index = workflow.getTasks().indexOf(task);
		if (index == -1 || index + 1 >= workflow.getTasks().size()) {
			return null;
		} else {
			return workflow.getTasks().get(index + 1);
		}
	}
	
	public static ITask getPreviousTask(IWorkflow workflow, ITask task) {
		int index = workflow.getTasks().indexOf(task);
		if (index <= 0) {
			return null;
		} else {
			return workflow.getTasks().get(index - 1);
		}
	}
	
	/**
	 * Indicates whether the first visible task has the same given task ID from the
	 * argument.
	 * 
	 * @param task
	 * @return true if the first task as the same task
	 */
	public static boolean isFirstVisibleTask(IWorkflow workflow, ITask task) {
		ITask firstVisibleTask = null;
		for (ITask workflowTask : workflow.getTasks()) {
			if (workflowTask.isVisible()) {
				firstVisibleTask = workflowTask;
				break;
			}
		}
		// Compare by reference
		return firstVisibleTask != null && firstVisibleTask == task;
	}
	
	/**
	 * Indicates whether the last visible task has the same given task ID from the
	 * argument.
	 * 
	 * @param taskId
	 * @return true if the last task as the same task ID
	 */
	public static boolean isLastVisibleTask(IWorkflow workflow, ITask task) {
		ITask lastVisibleTask = null;
		for (int i = workflow.getTasks().size() - 1; i >= 0; i--) {
			ITask workflowTask = workflow.getTasks().get(i);
			if (workflowTask.isVisible()) {
				lastVisibleTask = workflowTask;
				break;
			}
		}
		// Compare by reference
		return lastVisibleTask != null && lastVisibleTask == task;
	}
	
	public static String getWorkflowTitle(IWorkflow workflow) {
		return workflow.getParameters().getString(PARAM_TITLE, "Workflow");
	}
	
	public static IWorkflowIntroView createIntroView(IWorkflow workflow) throws ObjectCreateException {
		String introClassname  = workflow.getParameters().getString(PARAM_INTRO_CLASS);
		if (introClassname == null) {
			return new DefaultWorkflowIntroView();
		} else {
			introClassname = PropertiesHelper.substitueWithProperties(introClassname);
			return (IWorkflowIntroView) ObjectFactory.instantiateObject(introClassname);
		}
	}
	
	public static void addNewTask(IWorkflow workflow, ITaskDescriptor taskDesc) throws ObjectCreateException {
		ITask task = taskDesc.createNewTask();
		task.getParameters().put(WorkflowConfigConstants.PARAM_LABEL, taskDesc.getLabel());
		task.getParameters().put(WorkflowConfigConstants.PARAM_COLOUR, taskDesc.getColorString());
		workflow.addTask(task);
	}
	
	public static void addNewTask(IWorkflow workflow, ITaskDescriptor taskDesc, int index) throws ObjectCreateException {
		ITask task = taskDesc.createNewTask();
		task.getParameters().put(WorkflowConfigConstants.PARAM_LABEL, taskDesc.getLabel());
		task.getParameters().put(WorkflowConfigConstants.PARAM_COLOUR, taskDesc.getColorString());
		workflow.insertTask(index, task);
	}
	
	public static void updateMarkerLevel(IWorkflow workflow) { 	
		int level = -1;
		for (ITask task : workflow.getTasks()) {
			if (task instanceof AbstractMarkerTask) {
				AbstractMarkerTask markerTask = (AbstractMarkerTask) task;
				if (markerTask.getMarkerType() == MarkerType.START) {
					markerTask.setLevel(++level);
				} else if (markerTask.getMarkerType() == MarkerType.END) {
					markerTask.setLevel(level--);
				}
			}
		}
	}
	
	public static int findMarkerIndex(IWorkflow workflow, int level, MarkerType type) {
		for (ITask task : workflow.getTasks()) {
			if (task instanceof AbstractMarkerTask) {
				AbstractMarkerTask markerTask = (AbstractMarkerTask) task;
				if (markerTask.getLevel() == level && markerTask.getMarkerType() == type) {
					return workflow.getTasks().indexOf(markerTask);
				}
			}
		}
		return -1;
	}
	
	private WorkflowUtils() {
		super();
	}
	
}
