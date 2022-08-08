/**
 * 
 */
package org.gumtree.control.events;

import org.gumtree.control.model.PropertyConstants;

/**
 * @author nxi
 *
 */
public abstract class SicsControllerAdapter implements ISicsControllerListener {

	public void updateState(PropertyConstants.ControllerState oldState, PropertyConstants.ControllerState newState) {}
	
	public void updateValue(Object oldValue, Object newValue) {}
	
	public void updateEnabled(boolean isEnabled) {}
	
	public void updateTarget(Object oldValue, Object newValue) {}
	
}
