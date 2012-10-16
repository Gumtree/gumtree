package org.gumtree.workflow.support;

import org.gumtree.workflow.model.TaskModel;

public interface ITaskCompletionHandler {

	public void handleTaskCompletion(TaskModel taskModel, Object output);
	
}
