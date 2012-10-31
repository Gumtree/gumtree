package org.gumtree.util.akka;

import akka.actor.ActorRef;

public class ActorMessagePair {

	private ActorRef actorRef;

	private Object message;

	public ActorMessagePair(ActorRef actorRef, Object message) {
		this.actorRef = actorRef;
		this.message = message;
	}

	public ActorRef getActorRef() {
		return actorRef;
	}

	public Object getMessage() {
		return message;
	}

}
