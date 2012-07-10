package org.gumtree.gumnix.sics.control.events;

import java.net.URI;

import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.core.dataaccess.SicsDataAccessUtils;

public class ValueChangeEvent extends SicsControllerEvent {

	private IComponentData newValue;
	
	private URI uri;
	
	public ValueChangeEvent(IDynamicController controller, IComponentData newValue) {
		super(controller);
		this.newValue = newValue;
		uri = SicsDataAccessUtils.createControllerURI(controller);
	}

	public IDynamicController getController() {
		return (IDynamicController) getPublisher();
	}
	
	public URI getURI() {
		return uri;
	}
	
	public IComponentData getNewValue() {
		return newValue;
	}
	
}
