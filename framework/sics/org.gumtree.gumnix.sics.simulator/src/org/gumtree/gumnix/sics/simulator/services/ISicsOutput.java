package org.gumtree.gumnix.sics.simulator.services;

import org.gumtree.gumnix.sics.simulator.services.ISicsConnection.Flag;

public interface ISicsOutput {

	public int getContextId();

	public String getObjectId();

	public Flag getFlag();

	public Object getOutputObject();

}
