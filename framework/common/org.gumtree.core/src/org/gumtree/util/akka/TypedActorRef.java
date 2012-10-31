package org.gumtree.util.akka;

import java.io.Serializable;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.TypedActor;
import akka.actor.TypedProps;

@SuppressWarnings("serial")
public class TypedActorRef<T> implements ITypedActorRef<T>, Serializable {

	private ActorRef actorRef;

	private Class<T> type;

	public TypedActorRef(ActorRef actorRef, Class<T> type) {
		this.actorRef = actorRef;
		this.type = type;
	}

	@Override
	public ActorRef getActorRef() {
		return actorRef;
	}

	@Override
	public Class<T> getType() {
		return type;
	}

	@Override
	public T asActor(ActorSystem actorSystem) {
		return TypedActor.get(actorSystem).typedActorOf(
				new TypedProps<T>(type), actorRef);
	}

	@Override
	public T asActor(ActorContext actorContext) {
		return TypedActor.get(actorContext).typedActorOf(
				new TypedProps<T>(type), actorRef);
	}

	@Override
	public T asActor() {
		return asActor(TypedActor.context());
	}

}
