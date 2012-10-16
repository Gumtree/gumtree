package org.gumtree.workflow.support;

import org.gumtree.workflow.model.TaskModel;

public interface IParentActor {

	public void handleTaskCompletion(TaskModel taskModel);
	
}
