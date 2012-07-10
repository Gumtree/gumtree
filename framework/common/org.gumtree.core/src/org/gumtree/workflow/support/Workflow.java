package org.gumtree.workflow.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gumtree.workflow.ITask;
import org.gumtree.workflow.IWorkflow;

public class Workflow implements IWorkflow {

	private List<ITask<?, ?>> tasks;
	
	public Workflow() {
		tasks = new ArrayList<ITask<?,?>>(2);
	}
	
	@Override
	public IWorkflow appendTask(ITask<?, ?> task) {
		tasks.add(task);
		return this;
	}
	
	@Override
	public IWorkflow addTask(ITask<?, ?> task, int index) {
		tasks.add(index, task);
		return this;
	}
	
	@Override
	public IWorkflow removeTask(int index) {
		tasks.remove(index);
		return this;
	}
	
	@Override
	public IWorkflow removeAllTasks() {
		tasks.clear();
		return this;
	}
	
	@Override
	public ITask<?, ?> getTask(int index) {
		return tasks.get(index);
	}
	
	@Override
	public List<ITask<?, ?>> getTasks() {
		return Collections.unmodifiableList(tasks);
	}
	
}
