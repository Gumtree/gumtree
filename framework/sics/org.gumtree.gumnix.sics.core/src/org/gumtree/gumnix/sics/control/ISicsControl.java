package org.gumtree.gumnix.sics.control;


public interface ISicsControl {

	public ISicsController getSicsController();

//	public ISicsScan scan();

	// Temporary (it should use the new commandControl() in future)
	public ISicsBatchControl batch();
	
	public boolean isControllerAvailable();

}
