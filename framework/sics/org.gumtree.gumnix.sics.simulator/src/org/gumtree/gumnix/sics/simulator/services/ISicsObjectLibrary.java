package org.gumtree.gumnix.sics.simulator.services;

import org.gumtree.gumnix.sics.simulator.objects.ISicsObject;

public interface ISicsObjectLibrary {

	public void addSicsObject(ISicsObject object);

	public ISicsObject getSicsObject(String id);

	public void removeSicsOjObject(ISicsObject object);

}
