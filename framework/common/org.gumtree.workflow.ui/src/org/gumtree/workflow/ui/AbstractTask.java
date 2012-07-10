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

import static org.gumtree.workflow.ui.config.WorkflowConfigConstants.PARAM_COLOUR;
import static org.gumtree.workflow.ui.config.WorkflowConfigConstants.PARAM_ENABLE;
import static org.gumtree.workflow.ui.config.WorkflowConfigConstants.PARAM_LABEL;
import static org.gumtree.workflow.ui.config.WorkflowConfigConstants.PARAM_VISIBLE;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.gumtree.core.object.ObjectConfigException;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.util.PlatformUtils;
import org.gumtree.util.collection.IParameters;
import org.gumtree.util.collection.Parameters;
import org.gumtree.workflow.ui.events.TaskEvent;
import org.gumtree.workflow.ui.util.ITaskDescriptor;
import org.gumtree.workflow.ui.util.ITaskRegistry;

public abstract class AbstractTask implements IControllableTask {
	
	private long id;
	
	private IWorkflow workflow;
	
	private TaskState state;
	
	private Object dataModel;
	
	private boolean interrupted;
	
	private IParameters parameters;
	
	private String title;
	
	private Object stateLock = new Object();
	
	// Stores the execution results
	private volatile List<Object> results;
	
	public AbstractTask() {
		super();
		setState(TaskState.IDLE);
	}

	public void configure(IParameters parameters, Object dataModel) throws ObjectConfigException {
		this.dataModel = dataModel;
		this.parameters = parameters;
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.ITask#createTaskView()
	 */
	public ITaskView createTaskView() {
		ITaskView view = createViewInstance();
		if (!(view instanceof ITaskView)) {
			view = new DefaultTaskView();
		}
		// reference view to this task if possible
		if (view instanceof AbstractTaskView) {
			((AbstractTaskView) view).setTask(this);
		}
		((AbstractTaskView) view).setTask(this);
		return view;
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.ITask#getIcon()
	 */
	public Image getIcon() {
		// Gets the icon from the task registry
		ITaskRegistry registry = ServiceUtils.getService(ITaskRegistry.class);
		ITaskDescriptor desc = registry.getTaskDescriptorById(getClass().getName());
		if (desc != null) {
			return desc.getIcon();
		}
		return null;
	}
	
	public String getColorString() {
		return getParameters().getString(PARAM_COLOUR);
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.ITask#isVisible()
	 */
	public boolean isVisible() {
		return getParameters().get(PARAM_VISIBLE, Boolean.class, true);
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.ITask#isEnable()
	 */
	public boolean isEnable() {
		return getParameters().get(PARAM_ENABLE, Boolean.class, true);
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.ITask#getDataModel()
	 */
	public Object getDataModel() {
		if (dataModel == null) {
			dataModel = createModelInstance();
		}
		return dataModel;
	}
	
	public IParameters getParameters() {
		if (parameters == null) {
			parameters = new Parameters();
		}
		return parameters;
	}
	
	public void clearTask() {
		interrupted = false;
		setState(TaskState.IDLE);
		getResultList().clear();
	}
	
	/*************************************************************************
	 * Task execution 
	 *************************************************************************/
	
	public List<Object> runTask(List<Object> input) throws WorkflowException {
		// Check state
		if (!getState().equals(TaskState.IDLE)) {
			throw new WorkflowException("Invalid task state to run the task (" + getState().name() + ").");
		}
		
		// Set state to running
		setState(TaskState.RUNNING);
		if (input == null || input.size() == 0 || !isInputSupported()) {
			// Handles no input
			safeRun(null);
		} else {
			if (isMultiInputSupported()) {
				// Handles input one by one
				for (Object inputElement : input) {
					safeRun(inputElement);
				}
			} else {
				// Handles all inputs in one go
				safeRun(input);
			}
			// Since the actual run() implementation may return straight away, this will cause
			// synchronization issue with the event bus.
			//
			// For example, if the state event (running and completed) is sent to the event bus
			// at about the same time (within less than a millisecond), both events will not be 
			// performed in the sending order, as the thread context switching is undeterministic
			// (ie completed event is processed before running event).
			//
			// The solution to this is to ensure the time interval between event is greater than the
			// 1 ms resolution.
			try {
				Thread.sleep(10);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}
		// Set state to completed
		if (getState().equals(TaskState.RUNNING)) {
			setState(TaskState.COMPLETED);
		}
		
		// Return results
		return getResultList();
	}
	
	private void safeRun(Object input) throws WorkflowException {
		try {
			Object result = run(input);
			if (result != null) {
				getResultList().add(result);
			}
		} catch (Exception e) {
			setState(TaskState.ERROR);
			throw new WorkflowException("Failed to execute task.", e);
//			logger.error("Failed to execute task.", e);
		}
	}
	
	public void stopTask() {
		if (getState().equals(TaskState.RUNNING)) {
			interrupted = true;
			handleStop();
			setState(TaskState.ERROR);
		}
	}
	
	public void pauseTask() {
		if (getState().equals(TaskState.RUNNING)) {
			handlePause();
			setState(TaskState.PAUSED);
		}
	}

	public void resumeTask() {
		if (getState().equals(TaskState.PAUSED)) {
			handleResume();
			setState(TaskState.RUNNING);
		}
	}
	
	public void dispose() {
		handleDispose();
		dataModel = null;
		parameters = null;
		PlatformUtils.getPlatformEventBus().clearSubscribers(this);
	}
	
	protected boolean isInterrupted() {
		return interrupted;
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.ITask#getId()
	 */
	public long getId() {
		return id;
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.IControllableTask#setId(long)
	 */
	public void setId(long id) {
		this.id = id;
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.ITask#getLabel()
	 */
	public String getLabel() {
		return getParameters().getString(PARAM_LABEL, getClass().getSimpleName());
	}
	
	public void setLabel(String label) {
		getParameters().put(PARAM_LABEL, label);
	}
	
	/*************************************************************************
	 * Task event 
	 *************************************************************************/
	
	public TaskState getState() {
		return state;
	}
	
	public void setState(TaskState state) {
		synchronized (stateLock) {
			this.state = state;
			PlatformUtils.getPlatformEventBus().postEvent(new TaskEvent(this, state));			
		}
	}
	
	public void addEventListener(IEventHandler<TaskEvent> listener) {
		PlatformUtils.getPlatformEventBus().subscribe(this, listener);
	}
	
	public void removeEventListener(IEventHandler<TaskEvent> listener) {
		PlatformUtils.getPlatformEventBus().unsubscribe(this, listener);
	}

	protected IWorkflowContext getContext() {
		return getWorkflow().getContext();
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.workflow.ui.ITask#getWorkflow()
	 */
	public IWorkflow getWorkflow() {
		return workflow;
	}
	
	public void setWorkflow(IWorkflow workflow) {
		this.workflow = workflow;
	}
	
	/*************************************************************************
	 * Data flow methods
	 *************************************************************************/

	public boolean isInputSupported() {
		return getInputTypes() != null && getInputTypes().length > 0;
	}
	
	public boolean isOutputSupported() {
		return getOutputTypes() != null && getOutputTypes().length > 0;
	}
	
	public boolean isMultiInputSupported() {
		return true;
	}
	
	private List<Object> getResultList() {
		if (results == null) {
			synchronized (this) {
				if (results == null) {
					results = new ArrayList<Object>();		
				}
			}
		}
		return results;
	}
	
	public Object[] getRunResults() {
		return  getResultList().toArray(new Object[getResultList().size()]);
	}
	
	/*************************************************************************
	 * Subclass methods
	 *************************************************************************/
	protected abstract ITaskView createViewInstance();
	
	protected abstract Object createModelInstance();
	
	protected abstract Object run(Object input) throws WorkflowException;
	
	// Subclass may implement this to perform persistence of
	// non data modal related data
	public void persist(IParameters parameters) {
	}
	
	public void initialise() {
	}
	
	protected void handleDispose() {
	}
	
	protected void handleStop() {
	}
	
	protected void handlePause() {
	}
	
	protected void handleResume() {
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	
}
