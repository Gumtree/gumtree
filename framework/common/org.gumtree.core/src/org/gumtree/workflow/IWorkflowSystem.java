package org.gumtree.workflow;

import java.util.List;

import org.gumtree.workflow.model.WorkflowModel;

import akka.dispatch.Future;

public interface IWorkflowSystem {

	public Future<IWorkflow> addWorkflow(WorkflowModel model);

	public Future<List<IWorkflow>> getAvailableWorkflows();

	public Future<Boolean> removeWorkflow(IWorkflow workflow);

}
