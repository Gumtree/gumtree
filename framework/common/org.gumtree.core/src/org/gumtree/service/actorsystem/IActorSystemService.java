package org.gumtree.service.actorsystem;

import akka.actor.ActorRefFactory;

public interface IActorSystemService {

	public ActorRefFactory getActorSystem();
	
}
