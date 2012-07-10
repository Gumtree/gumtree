package org.gumtree.gumnix.sics.internal.control;

import org.gumtree.gumnix.sics.control.IDeviceProperty;

import ch.psi.sics.hipadaba.Device;
import ch.psi.sics.hipadaba.impl.PropertyImpl;

public class DevicePropertyImpl extends PropertyImpl implements IDeviceProperty {

	private Device device;

	public DevicePropertyImpl(Device device) {
		this.device = device;
	}

	public Device getDevice() {
		return device;
	}

}
