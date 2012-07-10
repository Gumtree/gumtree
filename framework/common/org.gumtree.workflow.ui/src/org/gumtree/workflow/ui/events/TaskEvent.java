package org.gumtree.workflow.ui.events;

import org.gumtree.service.eventbus.Event;
import org.gumtree.workflow.ui.ITask;
import org.gumtree.workflow.ui.TaskState;

public class TaskEvent extends Event {

	private TaskState state;
	
	public TaskEvent(ITask task, TaskState state) {
		super(task);
		this.state = state;
	}
	
	public ITask getPublisher() {
		return (ITask) super.getPublisher();
	}
	
	public TaskState getState() {
		return state;
	}
	
}
