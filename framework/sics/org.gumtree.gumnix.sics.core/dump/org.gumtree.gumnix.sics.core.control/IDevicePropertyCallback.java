package org.gumtree.gumnix.sics.control;

import ch.psi.sics.hipadaba.Property;

public interface IDevicePropertyCallback {

	public void handleReply(Property property, String value);
	
}
