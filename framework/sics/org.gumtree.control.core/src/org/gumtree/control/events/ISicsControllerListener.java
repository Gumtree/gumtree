package org.gumtree.control.events;

import org.gumtree.control.core.ControllerState;

public interface ISicsControllerListener {

	void updateState(ControllerState oldState, ControllerState newState);
	void updateValue(Object oldValue, Object newValue);
	void updateEnabled(boolean isEnabled);
	
}
