package org.gumtree.workflow;

public interface ITask<I, O> {

	public void run();
	
	public void setInput(I input);
	
	public O getOutput();

}
