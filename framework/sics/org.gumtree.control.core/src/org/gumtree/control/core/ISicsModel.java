package org.gumtree.control.core;

import java.io.IOException;

import org.gumtree.control.events.ISicsModelListener;
import org.gumtree.control.model.ModelStatus;

import ch.psi.sics.hipadaba.SICS;

public interface ISicsModel {

	int getSize();
	
	void addModelListener(ISicsModelListener listener);
	
	void removeModelListener(ISicsModelListener listener);
	
	void loadFromString(String xml) throws IOException;
	
	void loadFromFile(String filename) throws IOException;
	
	ISicsController findController(String idOrPath);
	
	ISicsController findControllerById(String deviceId);
	
	ISicsController findControllerByPath(String path);
	
	ISicsController findChildController(ISicsController controller, String relativePath);
	
	ISicsController findParentController(ISicsController controller);
	
	ISicsController[] getSicsControllers();
	
	ModelStatus getStatus();
	
	SICS getBase();
	
	void dispose();
}
