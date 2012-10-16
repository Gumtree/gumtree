package org.gumtree.workflow;

import java.util.List;

import org.gumtree.workflow.model.WorkflowModel;

import akka.actor.ActorRef;
import akka.dispatch.Future;

public interface IWorkflowSystem {

	public Future<ActorRef> addWorkflow(WorkflowModel model);

	public Future<List<ActorRef>> getAvailableWorkflowRefs();

	public Future<Boolean> removeWorkflow(ActorRef workflowRef);

}
