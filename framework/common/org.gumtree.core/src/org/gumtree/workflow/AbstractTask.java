package org.gumtree.workflow;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class AbstractTask<I, O> implements ITask<I, O>, Serializable {

	private I input;

	private O output;

	public AbstractTask() {
		super();
	}

	public I getInput() {
		return input;
	}

	@Override
	public void setInput(I input) {
		this.input = input;
	}

	@Override
	public O getOutput() {
		return output;
	}

	public void setOutput(O output) {
		this.output = output;
	}

}
