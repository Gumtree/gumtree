package org.gumtree.workflow.support;

import org.gumtree.workflow.model.TaskModel;

import akka.dispatch.Future;


public interface ITaskActor {

	public Future<TaskModel> getModel();
	
	public void run();
	
}
