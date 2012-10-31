package org.gumtree.util.akka;

import akka.actor.ActorRef;
import akka.actor.TypedActor;

public class EventRegisterActor implements IEventRegister {

	@Override
	public void subscribe(ActorRef subscriber, Class<?> channel) {
		TypedActor.context().system().eventStream()
				.subscribe(subscriber, channel);
	}

}
