package org.gumtree.gumnix.sics.control.events;

import java.net.URI;

import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.core.dataaccess.SicsDataAccessUtils;

public class TargetChangeEvent extends SicsControllerEvent {

	private IComponentData newTarget;
	
	private URI uri;
	
	public TargetChangeEvent(IDynamicController controller, IComponentData newTarget) {
		super(controller);
		this.newTarget = newTarget;
		uri = SicsDataAccessUtils.createControllerTargetURI(controller);
	}

	public IDynamicController getController() {
		return (IDynamicController) getPublisher();
	}
	
	public IComponentData getNewTarget() {
		return newTarget;
	}

	public URI getURI() {
		return uri;
	}
	
}