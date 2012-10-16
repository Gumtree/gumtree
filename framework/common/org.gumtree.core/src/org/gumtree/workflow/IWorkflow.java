package org.gumtree.workflow;

import org.gumtree.workflow.model.WorkflowModel;

import akka.dispatch.Future;

public interface IWorkflow {

	public Future<WorkflowModel> getModel();

	public Future<Boolean> run();

	public Future<WorkflowState> getState();

}
