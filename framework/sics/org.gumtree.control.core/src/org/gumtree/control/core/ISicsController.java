package org.gumtree.control.core;

import java.util.List;

import org.gumtree.control.events.ISicsControllerListener;

import ch.psi.sics.hipadaba.Component;

public interface ISicsController {

	List<ISicsController> getChildren();
	boolean isEnabled();
	void setEnabled(boolean isEnabled);
	void addControllerListener(ISicsControllerListener listener);
	void removeControllerListener(ISicsControllerListener listener);
	Component getModel();
	
	/**
	 * Returns the id of this component.
	 * 
	 * @return component id
	 */
	String getId();
	
	/**
	 * Returns the path of the wrapped component.
	 *
	 * @return the path string for the component
	 */
	String getPath();
	
	/**
	 * Returns the device id, if available, of this controller.
	 * 
	 * @return the device id; or null if it does not exist
	 */
	String getDeviceId();

}
