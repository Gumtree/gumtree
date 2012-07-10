package org.gumtree.workflow.ui.events;

import org.gumtree.service.eventbus.Event;
import org.gumtree.workflow.ui.ITaskView;

public class TaskViewEvent extends Event {

	public enum TaskViewEventType {
		REFRESH
	}

	private TaskViewEventType type;
	
	public TaskViewEvent(ITaskView taskView, TaskViewEventType type) {
		super(taskView);
	}

	public ITaskView getPublisher() {
		return (ITaskView) super.getPublisher();
	}
	
	public TaskViewEventType getType() {
		return type;
	}
	
}
