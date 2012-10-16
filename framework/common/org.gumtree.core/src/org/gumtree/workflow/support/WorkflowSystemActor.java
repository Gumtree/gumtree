package org.gumtree.workflow.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gumtree.workflow.IWorkflow;
import org.gumtree.workflow.IWorkflowSystem;
import org.gumtree.workflow.model.WorkflowModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Option;

import akka.actor.ActorRef;
import akka.actor.TypedActor;
import akka.actor.TypedActorFactory;
import akka.actor.TypedProps;
import akka.actor.TypedActor.PostRestart;
import akka.actor.TypedActor.PostStop;
import akka.actor.TypedActor.PreRestart;
import akka.actor.TypedActor.PreStart;
import akka.actor.TypedActor.Receiver;
import akka.dispatch.Future;
import akka.dispatch.Futures;
import akka.japi.Creator;

public class WorkflowSystemActor implements IWorkflowSystem, PreStart,
		PreRestart, PostRestart, PostStop, Receiver {

	private static final Logger logger = LoggerFactory
			.getLogger(WorkflowSystemActor.class);

	private List<ActorRef> workflowRefs;

	public WorkflowSystemActor() {
		super();
		workflowRefs = new ArrayList<ActorRef>(2);
	}

	/*************************************************************************
	 * Actor methods
	 *************************************************************************/

	@Override
	public void preStart() {
	}

	@Override
	public void preRestart(Throwable reason, Option<Object> message) {
	}

	@Override
	public void postRestart(Throwable reason) {
	}

	@Override
	public void postStop() {
	}

	@Override
	public void onReceive(Object message, ActorRef actor) {
	}

	/*************************************************************************
	 * Public methods
	 *************************************************************************/

	@Override
	public Future<ActorRef> addWorkflow(final WorkflowModel model) {
		// Get typed system
		TypedActorFactory factory = TypedActor.get(TypedActor.context());
		// Create workflow
		IWorkflow workflow = factory
				.typedActorOf(new TypedProps<WorkflowActor>(IWorkflow.class,
						new Creator<WorkflowActor>() {
							public WorkflowActor create() {
								return new WorkflowActor(model);
							}
						}));
		// Assign name
//		model.setAssignedId(factory.getActorRefFor(workflow).path().name());
		// Cache workflow
		ActorRef workflowRef = factory.getActorRefFor(workflow);
		workflowRefs.add(workflowRef);
		logger.info("Workflow has been successfully added from the system.");
		// Return workflow
		return Futures.successful(workflowRef, TypedActor.dispatcher());
	}

	public Future<List<ActorRef>> getAvailableWorkflowRefs() {
		return Futures.successful(Collections.unmodifiableList(workflowRefs),
				TypedActor.dispatcher());
	}

	// TODO:
	// - check workflow state before remove (ie stop execution before removing
	// it)
	// - remove completed workflow some time after (eg in 24 hours)
	@Override
	public Future<Boolean> removeWorkflow(ActorRef workflowRef) {
		// Stop the workflow (remove from the system)
		TypedActor.context().stop(workflowRef);
		/// Does this work with remote actor reference???
		workflowRefs.remove(workflowRef);
		logger.info("Workflow has been successfully removed from the system.");
		// Return result
		return Futures.successful(true, TypedActor.dispatcher());
	}

}
