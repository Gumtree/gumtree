package org.gumtree.control.events;

public interface ISicsModelListener {

	void update(String path, Object oldValue, Object newValue);
	
}
