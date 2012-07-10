package org.gumtree.gumnix.sics.simulator.services;

import java.util.HashMap;
import java.util.Map;

import org.gumtree.gumnix.sics.simulator.objects.ISicsObject;

public class SicsObjectLibrary implements ISicsObjectLibrary {

	private Map<String, ISicsObject> objectMap;

	public SicsObjectLibrary() {
		super();
		objectMap = new HashMap<String, ISicsObject>();
	}

	public void addSicsObject(ISicsObject object) {
		objectMap.put(object.getId(), object);
	}

	public ISicsObject getSicsObject(String id) {
		return objectMap.get(id);
	}

	public void removeSicsOjObject(ISicsObject object) {
		objectMap.remove(object.getId());
	}

}
