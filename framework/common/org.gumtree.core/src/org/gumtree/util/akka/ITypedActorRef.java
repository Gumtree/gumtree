package org.gumtree.util.akka;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public interface ITypedActorRef<T> {

	public ActorRef getActorRef();

	public Class<T> getType();

	public T asActor(ActorSystem actorSystem);

	public T asActor(ActorContext actorContext);
	
	public T asActor();
	
}
