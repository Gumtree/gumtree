package org.gumtree.workflow.support;

import java.util.List;

import org.gumtree.service.actorsystem.IActorSystemService;
import org.gumtree.workflow.IWorkflowSystem;
import org.gumtree.workflow.model.WorkflowModel;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.TypedActor;
import akka.actor.TypedProps;
import akka.dispatch.Future;

public class WorkflowSystemProxy implements IWorkflowSystem {

	private IWorkflowSystem workflowSystemActor;

	private IActorSystemService actorSystemService;

	public void activate() {
		ActorSystem system = (ActorSystem) getActorSystemService()
				.getActorSystem();
		workflowSystemActor = TypedActor.get(system).typedActorOf(
				new TypedProps<WorkflowSystemActor>(IWorkflowSystem.class,
						WorkflowSystemActor.class), "workflowSystem");
	}

	public void deactivate() {
		if (workflowSystemActor != null) {
			ActorSystem system = (ActorSystem) getActorSystemService()
					.getActorSystem();
			TypedActor.get(system).stop(workflowSystemActor);
			workflowSystemActor = null;
		}
		actorSystemService = null;
	}

	/*************************************************************************
	 * Components
	 *************************************************************************/

	public IActorSystemService getActorSystemService() {
		return actorSystemService;
	}

	public void setActorSystemService(IActorSystemService actorSystemService) {
		this.actorSystemService = actorSystemService;
	}

	public IWorkflowSystem getWorkflowSystemActor() {
		return workflowSystemActor;
	}

	/*************************************************************************
	 * Route
	 *************************************************************************/

	@Override
	public Future<ActorRef> addWorkflow(WorkflowModel model) {
		return getWorkflowSystemActor().addWorkflow(model);
	}

	@Override
	public Future<List<ActorRef>> getAvailableWorkflowRefs() {
		return getWorkflowSystemActor().getAvailableWorkflowRefs();
	}

	@Override
	public Future<Boolean> removeWorkflow(ActorRef workflowRef) {
		return getWorkflowSystemActor().removeWorkflow(workflowRef);
	}

}
