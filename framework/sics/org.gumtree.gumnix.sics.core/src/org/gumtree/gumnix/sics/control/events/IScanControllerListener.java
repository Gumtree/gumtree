package org.gumtree.gumnix.sics.control.events;

public interface IScanControllerListener extends IComponentControllerListener {

	public void scanConfigUpdated();

	public void scanStatusUpdated();

}
