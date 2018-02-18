package org.gumtree.control.events;

import org.gumtree.control.model.ModelStatus;

public interface ISicsModelListener {

	void update(String path, Object oldValue, Object newValue);
	
	public void statusChanged(ModelStatus newStatus);
	
	public void controllerInterrupted();
}
