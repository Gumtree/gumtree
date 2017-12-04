package org.gumtree.control.core;

import java.util.List;

import org.gumtree.control.events.ISicsControllerListener;

public interface ISicsController {

	List<ISicsController> getChildren();
	boolean isEnabled();
	void setEnabled(boolean isEnabled);
	void addControllerListener(ISicsControllerListener listener);
	void removeControllerListener(ISicsControllerListener listener);
	
}
