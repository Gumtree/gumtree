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

package org.gumtree.workflow.ui.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.gumtree.core.object.ObjectConfigException;
import org.gumtree.core.object.ObjectCreateException;
import org.gumtree.core.object.ObjectFactory;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.util.PlatformUtils;
import org.gumtree.util.PropertiesHelper;
import org.gumtree.util.collection.IParameters;
import org.gumtree.util.collection.Parameters;
import org.gumtree.workflow.ui.AbstractMarkerTask;
import org.gumtree.workflow.ui.IControllableTask;
import org.gumtree.workflow.ui.ITask;
import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.IWorkflowContext;
import org.gumtree.workflow.ui.MarkerType;
import org.gumtree.workflow.ui.WorkflowException;
import org.gumtree.workflow.ui.WorkflowState;
import org.gumtree.workflow.ui.config.TaskConfig;
import org.gumtree.workflow.ui.config.WorkflowConfig;
import org.gumtree.workflow.ui.events.WorkflowEvent;
import org.gumtree.workflow.ui.events.WorkflowStateEvent;
import org.gumtree.workflow.ui.events.WorkflowStructuralEvent;
import org.gumtree.workflow.ui.util.WorkflowUtils;

// workflow = controller
// context = model
// task = controller
public class Workflow implements IWorkflow {
	
	private IParameters parameters;
	
	private IWorkflowContext context;
	
	private WorkflowState state;
	
	private List<ITask> tasks;
	
	private List<ITask> taskCache;
	
	private long assignedId;
	
	private long taskIdCounter;
	
	private IControllableTask currentRunningTask;
	
	private List<Object> runInputs;
	
	private List<Object> runResults;
	
	private Object stateLock = new Object();
	
	private int nextExecutionTaskIndex;
	
	/**
	 * Class constructor.
	 */
	public Workflow() {
		super();
		tasks = new ArrayList<ITask>();
		setWorkflowState(WorkflowState.NEW, "Workflow initialised");
		taskIdCounter = 0;
		context = new WorkflowContext();
	}
	
	/*************************************************************************
	 * Initialisation methods
	 *************************************************************************/
	
	public synchronized void configure(WorkflowConfig config) throws ObjectConfigException {
		if (config == null) {
			throw new IllegalArgumentException("Workflow config cannot be null.");
		}
		// Restore context
		for (Entry<String, Object> entry: config.getContext().entrySet()) {
			// entry from the config must be persistable
			context.put(entry.getKey(), entry.getValue(), true);
		}
		// Set parameters;
		parameters = config.getParameters();
		// Set tasks
		for (TaskConfig taskConfig : config.getTaskConfigs()) {
			ITask task = createTask(taskConfig);
			// Add to task list
			addTask(task);
		}
	}
	
	private ITask createTask(TaskConfig taskConfig) {
		// Create task
		String classname = PropertiesHelper.substitueWithProperties(taskConfig.getClassname());
		IControllableTask task = null;
		try {
			task = ObjectFactory.instantiateObject(classname, IControllableTask.class);
		} catch (ObjectCreateException e) {
			throw new ObjectConfigException("Cannot instantiate task " + classname, e);
		}
		// Initialise task (optional when taskConfig is missing)
		task.configure(taskConfig.getParameters(), taskConfig.getDataModel());
		return task;
	}
	
	/*************************************************************************
	 * Workflow information
	 *************************************************************************/
	
	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.IWorkflow#context()
	 */
	public IWorkflowContext getContext() {
		return context;
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.IWorkflow#getParameters()
	 */
	public IParameters getParameters() {
		if (parameters == null) {
			parameters = new Parameters();
		}
		return parameters;
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.IWorkflow#getAssignedId()
	 */
	public long getAssignedId() {
		return assignedId;
	}
	
	public synchronized void assignedId(final long id) {
		assignedId = id;
	}

	public void setRunInput(Object input) {
		setRunInputs(Arrays.asList(input));
	}

	public void setRunInputs(List<Object> inputs) {
		runInputs = new ArrayList<Object>(inputs);
	}
	
	
	public List<Object> getRunResults() {
		if (runResults == null) {
			runResults = new ArrayList<Object>(2);
		}
		return runResults;
	}
	
	public void clearData() {
		if (runInputs != null) {
			runInputs.clear();
			runInputs = null;
		}
		if (runResults != null) {
			runResults.clear();
			runResults = null;
		}
	}
	
	/*************************************************************************
	 * Workflow structure
	 *************************************************************************/

	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.IWorkflow#getTasks()
	 */
	public List<ITask> getTasks() {
		if (taskCache == null) {
			taskCache = Collections.unmodifiableList(tasks);
		}
		return taskCache;
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.IWorkflow#addTask(org.gumtree.workflow.ui.ITask)
	 */
	public void addTask(ITask task) {
		synchronized (tasks) {
			// Add to workflow
			tasks.add(task);
			// Set task Id
			if(task instanceof IControllableTask) {
				((IControllableTask) task).setId(taskIdCounter++);
				((IControllableTask) task).setWorkflow(this);
				((IControllableTask) task).initialise();
			}
			// Update cache
			taskCache = Collections.unmodifiableList(tasks);
			PlatformUtils.getPlatformEventBus().postEvent(new WorkflowStructuralEvent(this));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.IWorkflow#removeTask(org.gumtree.workflow.ui.ITask)
	 */
	public void removeTask(ITask task) {
		synchronized (tasks) {
			// Remove from workflow
			tasks.remove(task);
			if(task instanceof IControllableTask) {
				((IControllableTask) task).dispose();
			}
			// Update cache
			taskCache = Collections.unmodifiableList(tasks);
			PlatformUtils.getPlatformEventBus().postEvent(new WorkflowStructuralEvent(this));
		}
	}

	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.IWorkflow#insertTask(int, org.gumtree.workflow.ui.ITask)
	 */
	public void insertTask(int index, ITask task) {
		synchronized (tasks) {
			// Add to workflow
			tasks.add(index, task);
			// Set task Id
			if(task instanceof IControllableTask) {
				((IControllableTask) task).setId(taskIdCounter++);
				((IControllableTask) task).setWorkflow(this);
				((IControllableTask) task).initialise();
			}
			// Update cache
			taskCache = Collections.unmodifiableList(tasks);
			PlatformUtils.getPlatformEventBus().postEvent(new WorkflowStructuralEvent(this));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.IWorkflow#insertTasks(int, java.util.List)
	 */
	public void insertTasks(final int index, final List<ITask> taskList) {
		synchronized (tasks) {
			// Add to workflow
			tasks.addAll(index, taskList);
			// Set task Id
			for (ITask task : taskList) {
				if(task instanceof IControllableTask) {
					((IControllableTask) task).setId(taskIdCounter++);
					((IControllableTask) task).setWorkflow(this);
					((IControllableTask) task).initialise();
				}
			}
			// Update cache
			taskCache = Collections.unmodifiableList(tasks);
			PlatformUtils.getPlatformEventBus().postEvent(new WorkflowStructuralEvent(this));
		}
	}
	
	public void swapTask(ITask task1, ITask task2) {
		synchronized (tasks) {
			if (!tasks.contains(task1) || !tasks.contains(task2)) {
				return;
			}
			int index1 = tasks.indexOf(task1);
			int index2 = tasks.indexOf(task2);
			Collections.swap(tasks, index1, index2);
			// Update cache
			taskCache = Collections.unmodifiableList(tasks);
			PlatformUtils.getPlatformEventBus().postEvent(new WorkflowStructuralEvent(this));
		}
	}
	
	public void setTask(int index, ITask task) {
		synchronized (tasks) {
			if (!tasks.contains(task)) {
				return;
			}
			int oldIndex = tasks.indexOf(task);
			// Do nothing
			if (oldIndex == index) {
				return;
			}
			// Insert
			tasks.add(index, task);
			// Remove old copy
			if (oldIndex < index) {
				tasks.remove(oldIndex);
			} else {
				tasks.remove(oldIndex + 1);
			}
			// Update cache
			taskCache = Collections.unmodifiableList(tasks);
			PlatformUtils.getPlatformEventBus().postEvent(new WorkflowStructuralEvent(this));
		}
	}
	
	/*************************************************************************
	 * Workflow execution
	 *************************************************************************/
	// TODO: synchronisation
	// TODO: proper error handling
	public void run() throws WorkflowException {
		// TODO: is this right?
		// Clear context first (why???)
//		getContext().clear();
		
		/*********************************************************************
		 * Initialise
		 *********************************************************************/
		setWorkflowState(WorkflowState.RUNNING, "Running workflow");
		nextExecutionTaskIndex = 0;
		currentRunningTask = null;
		runResults = null;
		if (runInputs == null) {
			runInputs = new ArrayList<Object>(2);
		}
		List<Object> results = runInputs;
//		for (ITask task : getTasks()) {
//			IControllableTask controllableTask = (IControllableTask) task;
//			// Initialise task by clearing it
//			controllableTask.clearTask();
//		}
		// Analysis looping level
		WorkflowUtils.updateMarkerLevel(this);
		
		/*********************************************************************
		 * Running task
		 *********************************************************************/
		while (true) {
			// Pre condition: end of workflow
			if (nextExecutionTaskIndex >= getTasks().size()) {
				break;
			}
			// Pre condition: stop button is hit
			if (getState() == WorkflowState.STOPPING) {
				break;
			}
			
			currentRunningTask = (IControllableTask) getTasks().get(nextExecutionTaskIndex);
			
			// TODO: Check input
			// Set input
//			if (results != null && results.length > 0) {
//				currentRunningTask.setInput(results[0]);
//			}
			try {
				// Clear all following tasks
				for (int i = nextExecutionTaskIndex; i < getTasks().size(); i++) {
					((IControllableTask) getTasks().get(i)).clearTask();
				}
				List<Object> newResults = currentRunningTask.runTask(results);
				if (!currentRunningTask.isOutputSupported()) {
					// Case 1: this task does not support ouput, we route the result by not update it
				} else {
					results = new ArrayList<Object>(newResults);
				}
			} catch (WorkflowException e) {
				setWorkflowState(WorkflowState.FINISHED, "Workflow ends with error");
				return;
			}
			// See if we need to loop or not
			if (currentRunningTask instanceof AbstractMarkerTask) {
				AbstractMarkerTask markerTask = (AbstractMarkerTask) currentRunningTask;
				if (markerTask.isRepeatLevel()) {
					int level = markerTask.getLevel();
					int nextTaskIndex = WorkflowUtils.findMarkerIndex(this, level, MarkerType.START);
					if (nextTaskIndex != -1) {
						nextExecutionTaskIndex = nextTaskIndex;
						continue;
					}
					if (nextTaskIndex == -1 && level < 0) {
						// Special case: if a loop task is place with no start marker,
						// repeat from the begining (like apple automator)
						nextExecutionTaskIndex = 0;
						continue;
					}
				}
			}
			nextExecutionTaskIndex++;
		}
		
		// Set Result
		runResults = results;
		
		/*********************************************************************
		 * Clean up
		 *********************************************************************/
		setWorkflowState(WorkflowState.FINISHED, "Workflow completed");
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.IWorkflow#stop()
	 */
	public void stop() {
		setWorkflowState(WorkflowState.STOPPING, "Workflow is attemping to stop");
		// We don't know which task we get up to ... so we stop all
		for (ITask task : getTasks()) {
			((IControllableTask) task).stopTask();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.IWorkflow#pause()
	 */
	public void pause() {
		setWorkflowState(WorkflowState.PAUSING, "Workflow is attemping to pause");
		for (ITask task : getTasks()) {
			((IControllableTask) task).pauseTask();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.IWorkflow#resume()
	 */
	public void resume() {
	}
	
	public void dispose() {
		for (ITask task : getTasks()) {
			if (task instanceof IControllableTask) {
				((IControllableTask) task).dispose();
			}
		}
		tasks.clear();
		
		if (context != null) {
			context.clear();
			context = null;
		}
		
		taskCache = null;
		
		parameters = null;
		
		PlatformUtils.getPlatformEventBus().clearSubscribers(this);
	}
	
	/*************************************************************************
	 * Workflow status
	 *************************************************************************/
	
	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.IWorkflow#getState()
	 */
	public WorkflowState getState() {
		return state;
	}
	
	public void setWorkflowState(WorkflowState state, String message) {
		synchronized (stateLock) {
			this.state = state;
			PlatformUtils.getPlatformEventBus().postEvent(new WorkflowStateEvent(this, state, message));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.IWorkflow#addEventListener(org.gumtree.core.eventbus.IEventHandler)
	 */
	public <T extends WorkflowEvent> void addEventListener(IEventHandler<T> listener) {
		PlatformUtils.getPlatformEventBus().subscribe(this, listener);
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.IWorkflow#removeEventListener(org.gumtree.core.eventbus.IEventHandler)
	 */
	public <T extends WorkflowEvent> void removeEventListener(IEventHandler<T> listener) {
		PlatformUtils.getPlatformEventBus().unsubscribe(this, listener);
	}
	
}