package org.gumtree.workflow;

import org.gumtree.workflow.model.TaskModel;

import akka.dispatch.Future;


public interface ITaskController {

	public Future<TaskModel> getModel();
	
	public void run(Object input);
	
}
