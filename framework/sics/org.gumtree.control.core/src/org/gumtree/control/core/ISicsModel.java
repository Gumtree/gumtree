package org.gumtree.control.core;

import java.io.IOException;

import org.gumtree.control.events.ISicsModelListener;

public interface ISicsModel {

	void addModelListener(ISicsModelListener listener);
	
	void removeModelListener(ISicsModelListener listener);
	
	void loadFromString(String xml) throws IOException;
	
	void loadFromFile(String filename) throws IOException;
	
	ISicsController findController(String idOrPath);
	
	ISicsController findControllerById(String deviceId);
	
	ISicsController findControllerByPath(String path);
}
