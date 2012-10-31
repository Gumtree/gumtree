package org.gumtree.util.akka;

import java.io.Serializable;

import org.gumtree.util.akka.ExtendedUntypedActor.InMessage.CreateChild;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Scheduler;
import akka.actor.UntypedActor;

public abstract class ExtendedUntypedActor extends UntypedActor {

	public static interface InMessage extends Serializable {
		public static class CreateChild {
			public Props props;
			public String id;

			public CreateChild(Props props, String id) {
				this.props = props;
				this.id = id;
			}
		}
	}

	public void createChild(CreateChild message) {
		if (!getContext().actorFor(message.id).isTerminated()) {
			getContext().actorOf(message.props, message.id);
		}
	}
	
	public void publishEvent(Object event) {
		getContext().system().eventStream().publish(event);
	}

	public Scheduler getSchulder() {
		return getContext().system().scheduler();
	}
	
	public ActorSystem getSystem() {
		return getContext().system();
	}

}
