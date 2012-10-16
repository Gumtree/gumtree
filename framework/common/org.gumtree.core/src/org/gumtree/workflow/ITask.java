package org.gumtree.workflow;

import java.util.Map;

public interface ITask<I, O> {

	public Map<String, Object> getProperties();

	public void setInput(I input);

	public O getOutput();

	public void run();

}
