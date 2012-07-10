package org.gumtree.gumnix.sics.simulator.services;

import org.gumtree.gumnix.sics.simulator.services.ISicsConnection.Flag;

public class SicsOutput implements ISicsOutput {

	private int contextId;

	private String objectId;

	private Flag flag;

	private Object object;

	public SicsOutput(int contextId, String objectId, Flag flag, Object object) {
		this.contextId = contextId;
		this.objectId = objectId;
		this.flag = flag;
		this.object = object;
	}

	public int getContextId() {
		return contextId;
	}

	public Flag getFlag() {
		return flag;
	}

	public String getObjectId() {
		return objectId;
	}

	public Object getOutputObject() {
		return object;
	}

}
