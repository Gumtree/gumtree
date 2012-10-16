package org.gumtree.workflow.support;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.gumtree.workflow.ITaskController;
import org.gumtree.workflow.IWorkflow;
import org.gumtree.workflow.WorkflowState;
import org.gumtree.workflow.model.TaskModel;
import org.gumtree.workflow.model.WorkflowModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Option;
import akka.actor.ActorRef;
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
public class WorkflowActor implements IWorkflow, ITaskCompletionHandler,
		PreStart, PreRestart, PostRestart, PostStop, Serializable {

	private static final Logger logger = LoggerFactory
			.getLogger(WorkflowActor.class);

	private WorkflowModel model;

	private WorkflowState state;

	private Map<TaskModel, ActorRef> modelRefMap;

	private int completedTasks;

	public WorkflowActor(WorkflowModel model) {
		this.model = model;
		state = WorkflowState.IDLE;
		modelRefMap = new HashMap<TaskModel, ActorRef>();
	}

	/*************************************************************************
	 * Actor methods
	 *************************************************************************/

	@Override
	public void preStart() {
		for (final TaskModel taskModel : model.getChildren()) {
			TypedActorFactory factory = TypedActor.get(TypedActor.context());
			ITaskController taskController = factory
					.typedActorOf(new TypedProps<TaskControllerActor>(
							ITaskController.class,
							new Creator<TaskControllerActor>() {
								@Override
								public TaskControllerActor create()
										throws Exception {
									return new TaskControllerActor(taskModel);
								}
							}));
			modelRefMap.put(taskModel, factory.getActorRefFor(taskController));
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
			ActorRef taskControllerRef = modelRefMap.get(taskModel);
			ITaskController taskController = factory.typedActorOf(
					new TypedProps<ITaskController>(ITaskController.class),
					taskControllerRef);
			taskController.run(null);
		}
		return Futures.successful(true, TypedActor.dispatcher());
	}

	@Override
	public void handleTaskCompletion(TaskModel taskModel, Object output) {
		TypedActorFactory factory = TypedActor.get(TypedActor.context());
		if (taskModel.getNextTasks().size() == 0) {
			completedTasks++;
			if (model.getEndTasks().size() == completedTasks) {
				state = WorkflowState.COMPLETED;
				logger.info("Workflow is completed.");
			}
		} else {
			for (TaskModel nextTaskModel : taskModel.getNextTasks()) {
				ActorRef taskControllerRef = modelRefMap.get(nextTaskModel);
				ITaskController taskController = factory.typedActorOf(
						new TypedProps<ITaskController>(ITaskController.class),
						taskControllerRef);
				taskController.run(output);
			}
		}
	}

}
