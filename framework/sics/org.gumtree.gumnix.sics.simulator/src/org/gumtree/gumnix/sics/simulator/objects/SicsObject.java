package org.gumtree.gumnix.sics.simulator.objects;

public abstract class SicsObject implements ISicsObject {

	private String id;

	public SicsObject(String id) {
		setId(id);
	}

	public String getId() {
		return id;
	}

	protected void setId(String id) {
		this.id = id;
	}

}
