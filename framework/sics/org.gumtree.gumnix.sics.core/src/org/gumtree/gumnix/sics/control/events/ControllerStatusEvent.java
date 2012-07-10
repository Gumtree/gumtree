package org.gumtree.gumnix.sics.control.events;

import java.net.URI;

import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.core.dataaccess.SicsDataAccessUtils;

public class ControllerStatusEvent extends SicsControllerEvent {

	private ControllerStatus status;
	
	private URI uri;
	
	public ControllerStatusEvent(IComponentController controller, ControllerStatus status) {
		super(controller);
		this.status = status;
		uri = SicsDataAccessUtils.createControllerStatusURI(controller);
	}
	
	public ControllerStatus getStatus() {
		return status;
	}

	public URI getURI() {
		return uri;
	}
	
}
