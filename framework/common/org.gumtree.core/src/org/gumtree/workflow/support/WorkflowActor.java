package org.gumtree.workflow.support;

import java.io.Serializable;

import org.gumtree.workflow.IWorkflow;
import org.gumtree.workflow.WorkflowState;
import org.gumtree.workflow.model.TaskModel;
import org.gumtree.workflow.model.WorkflowModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Option;
import akka.actor.TypedActor;
import akka.actor.TypedActor.PostRestart;
import akka.actor.TypedActor.PostStop;
import akka.actor.TypedActor.PreRestart;
import akka.actor.TypedActor.PreStart;
import akka.actor.TypedActorFactory;
import akka.actor.TypedProps;
import akka.dispatch.Future;
import akka.dispatch.Futures;
import akka.japi.Creator;

@SuppressWarnings("serial")
public class WorkflowActor implements IWorkflow, IParentActor, PreStart,
		PreRestart, PostRestart, PostStop, Serializable {

	private static final Logger logger = LoggerFactory
			.getLogger(WorkflowActor.class);

	private WorkflowModel model;

	private WorkflowState state;

	private int completedTasks;
	
	public WorkflowActor(WorkflowModel model) {
		this.model = model;
		state = WorkflowState.IDLE;
	}

	/*************************************************************************
	 * Actor methods
	 *************************************************************************/

	@Override
	public void preStart() {
		for (final TaskModel taskModel : model.getChildren()) {
			TypedActorFactory factory = TypedActor.get(TypedActor.context());
			ITaskActor taskActor = factory
					.typedActorOf(new TypedProps<TaskActor>(ITaskActor.class,
							new Creator<TaskActor>() {
								@Override
								public TaskActor create() throws Exception {
									return new TaskActor(taskModel);
								}
							}));
			taskModel.setAssignedId(factory.getActorRefFor(taskActor).path()
					.name());
		}
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

	/*************************************************************************
	 * Properties
	 *************************************************************************/

	@Override
	public Future<WorkflowModel> getModel() {
		return Futures.successful(model, TypedActor.dispatcher());
	}

	public Future<Boolean> updateModel(WorkflowModel model) {
		// TODO: clone the object ??
		this.model = model;
		return Futures.successful(true, TypedActor.dispatcher());
	}

	@Override
	public Future<WorkflowState> getState() {
		return Futures.successful(state, TypedActor.dispatcher());
	}

	/*************************************************************************
	 * Public methods
	 *************************************************************************/

	@Override
	public Future<Boolean> run() {
		// TODO: Check state before run
		TypedActorFactory factory = TypedActor.get(TypedActor.context());
		completedTasks = 0;
		for (TaskModel taskModel : model.getStartTasks()) {
			ITaskActor taskActor = factory.typedActorOf(
					new TypedProps<ITaskActor>(ITaskActor.class), TypedActor
							.context().actorFor(taskModel.getAssignedId()));
			taskActor.run();
		}
		return Futures.successful(true, TypedActor.dispatcher());
	}

	@Override
	public void handleTaskCompletion(TaskModel taskModel) {
		TypedActorFactory factory = TypedActor.get(TypedActor.context());
		if (taskModel.getNextTasks().size() == 0) {
			completedTasks++;
			if (model.getEndTasks().size() == completedTasks) {
				state = WorkflowState.COMPLETED;
			}
		} else {
			for (TaskModel nextTaskModel : taskModel.getNextTasks()) {
				ITaskActor taskActor = factory.typedActorOf(
						new TypedProps<ITaskActor>(ITaskActor.class),
						TypedActor.context().actorFor(
								nextTaskModel.getAssignedId()));
				taskActor.run();
			}
		}
	}

}
