package org.gumtree.workflow.support;

import java.io.Serializable;
import java.util.concurrent.Callable;

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
public class TaskActor implements ITaskActor, IParentActor, PreStart,
		PreRestart, PostRestart, PostStop, Serializable {

	private static final Logger logger = LoggerFactory
			.getLogger(TaskActor.class);

	private TaskModel model;

	private IParentActor parentActor;
	
	public TaskActor(TaskModel model) {
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
			ITaskActor taskActor = factory
					.typedActorOf(new TypedProps<TaskActor>(ITaskActor.class,
							new Creator<TaskActor>() {
								@Override
								public TaskActor create() throws Exception {
									return new TaskActor(taskModel);
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

	public void run() {
		// Check state
		// Check if previous nodes are done
		logger.info("Start task " + model.getName());
		// Logic
		Future<Void> future = Futures.future(new Callable<Void>() {
			public Void call() {
				logger.info("Start task " + model.getName() + " logic");
				try {
					Thread.sleep(Math.round(Math.random() * 5 * 1000));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				logger.info("Completed task " + model.getName() + " logic");
				handleTaskCompleted();
				return null;
			}
		}, TypedActor.dispatcher());
		TypedActorFactory factory = TypedActor.get(TypedActor.context());
		parentActor = factory.typedActorOf(
				new TypedProps<IParentActor>(IParentActor.class), TypedActor
						.context().actorFor("../"));
	}

	private void handleTaskCompleted() {
		parentActor.handleTaskCompletion(model);
	}
	
	@Override
	public void handleTaskCompletion(TaskModel taskModel) {
	}

}
