package org.gumtree.workflow.support;

import org.gumtree.workflow.ITask;
import org.gumtree.workflow.TaskState;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

public abstract class AbstractTask<I, O> implements ITask<I, O> {

	@XStreamOmitField
	private TaskState state;

	@XStreamOmitField
	private I input;

	@XStreamOmitField
	private O output;

	public AbstractTask() {
	}
	
	@Override
	public TaskState getState() {
		if (state == null) {
			state = TaskState.IDLE;
		}
		return state;
	}

	protected void setState(TaskState state) {
		this.state = state;
	}

	@Override
	public I getInput() {
		return input;
	}

	@Override
	public ITask<I, O> setInput(I input) {
		this.input = input;
		return this;
	}

	@Override
	public O getOutput() {
		return output;
	}

	protected void setOutput(O output) {
		this.output = output;
	}

	@Override
	public ITask<I, O> run() {
		// TODO: synchronisation lock
		if (getState() != TaskState.IDLE) {
			// TODO: error
		}
		setState(TaskState.RUNNING);
		setOutput(runTask(getInput()));
		setState(TaskState.IDLE);
		return this;
	}

	protected abstract O runTask(I input);
	
	@Override
	public ITask<I, O> stop() {
		stopTask();
		setState(TaskState.IDLE);
		return this;
	}

	protected void stopTask() {
	}
	
	@Override
	public ITask<I, O> pause() {
		pauseTask();
		setState(TaskState.PAUSED);
		return this;
	}

	protected void pauseTask() {
	}
	
	@Override
	public ITask<I, O> resume() {
		if (getState() != TaskState.PAUSED) {
			// Error
		}
		setState(TaskState.RUNNING);
		resumeTask();
		return this;
	}
	
	protected void resumeTask() {
	}

}
