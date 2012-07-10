package org.gumtree.workflow;

import java.util.List;

public interface IWorkflow {
	
	public IWorkflow appendTask(ITask<?, ?> task);
	
	public IWorkflow addTask(ITask<?, ?> task, int index);
	
	public IWorkflow removeTask(int index);
	
	public IWorkflow removeAllTasks();
	
	public ITask<?, ?> getTask(int index);
	
	public List<ITask<?, ?>> getTasks();
	
}
