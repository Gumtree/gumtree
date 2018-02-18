package org.gumtree.control.events;

import org.gumtree.control.model.PropertyConstants;

public interface ISicsControllerListener {

	void updateState(PropertyConstants.ControllerState oldState, PropertyConstants.ControllerState newState);
	void updateValue(Object oldValue, Object newValue);
	void updateEnabled(boolean isEnabled);
	void updateTarget(Object oldValue, Object newValue);
	
}
