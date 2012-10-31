package org.gumtree.util.akka;

import akka.actor.ActorRef;

public interface IEventRegister {

	void subscribe(ActorRef subscriber, Class<?> channel);
	
}
