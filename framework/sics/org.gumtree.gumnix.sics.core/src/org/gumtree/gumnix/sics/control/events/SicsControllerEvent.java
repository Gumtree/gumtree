package org.gumtree.gumnix.sics.control.events;

import java.net.URI;
import java.util.Map;

import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.service.eventbus.Event;

public abstract class SicsControllerEvent extends Event {

	public SicsControllerEvent(IComponentController controller) {
		super(controller);
	}
	
	public SicsControllerEvent(IComponentController controller, Map<String, Object> properties) {
		super(controller, properties);
	}
	
	public IComponentController getController() {
		return (IComponentController) getPublisher();
	}
	
	public abstract URI getURI();

}
