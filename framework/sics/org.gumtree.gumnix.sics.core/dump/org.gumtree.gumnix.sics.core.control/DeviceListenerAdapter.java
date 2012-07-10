package org.gumtree.gumnix.sics.control;

import org.gumtree.gumnix.sics.control.IComponentController.ComponentStatus;

import ch.psi.sics.hipadaba.Device;
import ch.psi.sics.hipadaba.Property;

public class DeviceListenerAdapter implements IDeviceListener {

	public void deviceStatusChanged(ComponentStatus newStatus) {
	}

	public void propertyChanged(Device device, Property property, String newValue) {
	}

}
