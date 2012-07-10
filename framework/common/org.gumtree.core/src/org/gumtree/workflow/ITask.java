package org.gumtree.workflow;

public interface ITask<I, O> {

	public TaskState getState();
	
	public ITask<I, O> setInput(I input);
	
	public I getInput();
	
	public O getOutput();
	
	public ITask<I, O> run();
	
	public ITask<I, O> stop();
	
	public ITask<I, O> pause();
	
	public ITask<I, O> resume();
	
}
