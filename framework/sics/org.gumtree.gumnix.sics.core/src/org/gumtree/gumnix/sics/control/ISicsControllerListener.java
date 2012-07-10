package org.gumtree.gumnix.sics.control;

public interface ISicsControllerListener {

	public void statusChanged(ControllerStatus newStatus);
	
	public void controllerInterrupted();
	
}
