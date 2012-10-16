package org.gumtree.workflow.support;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.gumtree.core.object.ObjectFactory;
import org.gumtree.workflow.ITask;
import org.gumtree.workflow.ITaskController;
import org.gumtree.workflow.model.TaskModel;
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
public class TaskControllerActor implements ITaskController,
		ITaskCompletionHandler, PreStart, PreRestart, PostRestart, PostStop,
		Serializable {

	private static final Logger logger = LoggerFactory
			.getLogger(TaskControllerActor.class);

	private TaskModel model;

	private ITaskCompletionHandler parentActor;

	public TaskControllerActor(TaskModel model) {
		super();
		this.model = model;
	}

	/*************************************************************************
	 * Properties
	 *************************************************************************/

	public Future<TaskModel> getModel() {
		return Futures.successful(model, TypedActor.dispatcher());
	}

	/*************************************************************************
	 * Life cycle
	 *************************************************************************/

	@Override
	public void preStart() {
		TypedActorFactory factory = TypedActor.get(TypedActor.context());
		for (final TaskModel taskModel : model.getChildren()) {
			ITaskController taskActor = factory
					.typedActorOf(new TypedProps<TaskControllerActor>(
							ITaskController.class,
							new Creator<TaskControllerActor>() {
								@Override
								public TaskControllerActor create()
										throws Exception {
									return new TaskControllerActor(taskModel);
								}
							}));
			// Bad
			taskModel.setName(factory.getActorRefFor(taskActor).path().name());
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
	 * Methods
	 *************************************************************************/

	@Override
	public void run(final Object input) {
		TypedActorFactory factory = TypedActor.get(TypedActor.context());
		parentActor = factory.typedActorOf(
				new TypedProps<ITaskCompletionHandler>(
						ITaskCompletionHandler.class), TypedActor.context()
						.actorFor("../"));
		// Check state
		// Check if previous nodes are done
		logger.info("Start task " + model.getName());
		// Logic
		Future<Void> future = Futures.future(new Callable<Void>() {
			public Void call() {
				ITask task = ObjectFactory.instantiateObject(model.getTaskClass(), ITask.class);
				task.getProperties().putAll(model.getProperties());
				task.setInput(input);
				logger.info("Start task '" + model.getName() + "'");
				task.run();
				logger.info("Completed task '" + model.getName() + "'");
				handleTaskCompleted(task.getOutput());
				return null;
			}
		}, TypedActor.dispatcher());

	}

	private void handleTaskCompleted(Object output) {
		parentActor.handleTaskCompletion(model, output);
	}

	@Override
	public void handleTaskCompletion(TaskModel taskModel, Object output) {
	}

}
