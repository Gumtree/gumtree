package org.gumtree.gumnix.sics.internal.control;

import org.gumtree.gumnix.sics.control.IDevicePropertyCallback;

import ch.psi.sics.hipadaba.Property;

public class InternalPropertyCallback implements IDevicePropertyCallback {

	private boolean dataReady;
	
	private String propertyValue;
	
	public InternalPropertyCallback() {
		dataReady = false;
		propertyValue = null;
	}
	
	public void handleReply(Property property, String value) {
		propertyValue = value;
		dataReady = true;
	}

	public boolean isDataReady() {
		return dataReady;
	}

	public String getValue() {
		return propertyValue;
	}

	
}
