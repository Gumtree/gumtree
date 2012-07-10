package org.gumtree.gumnix.sics.control.events;

import org.gumtree.gumnix.sics.control.ControllerStatus;

public interface IComponentControllerListener {

	public void componentStatusChanged(ControllerStatus newStatus);

}
