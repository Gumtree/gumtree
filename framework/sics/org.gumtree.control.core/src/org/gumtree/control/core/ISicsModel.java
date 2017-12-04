package org.gumtree.control.core;

import org.gumtree.control.events.ISicsModelListener;

public interface ISicsModel {

	void addModelListener(ISicsModelListener listener);
	
	void removeModelListener(ISicsModelListener listener);
	
	
}
