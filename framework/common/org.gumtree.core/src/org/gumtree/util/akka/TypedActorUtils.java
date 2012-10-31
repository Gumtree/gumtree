package org.gumtree.util.akka;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Scheduler;
import akka.actor.TypedActor;
import akka.event.EventStream;

public final class TypedActorUtils {

	public static ActorContext context() {
		return TypedActor.context();
	}

	public static ActorSystem system() {
		return TypedActor.context().system();
	}

	public static ActorRef self() {
		return TypedActor.context().self();
	}
	
	public static Scheduler scheduler() {
		return TypedActor.context().system().scheduler();
	}

	public static EventStream eventStream() {
		return TypedActor.context().system().eventStream();
	}
	
	public static void publishEvent(Object event) {
		TypedActor.context().system().eventStream().publish(event);
	}

	private TypedActorUtils() {
		super();
	}

}
