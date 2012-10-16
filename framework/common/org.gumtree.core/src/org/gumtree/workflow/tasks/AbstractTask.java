package org.gumtree.workflow.tasks;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.gumtree.workflow.ITask;

@SuppressWarnings("serial")
public abstract class AbstractTask<I, O> implements ITask<I, O>, Serializable {

	private I input;

	private O output;
	
	private Map<String, Object> properties;

	public AbstractTask() {
		super();
		properties = new HashMap<String, Object>(2);
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

	@Override
	public Map<String, Object> getProperties() {
		return properties;
	}

}
