package org.gumtree.gumnix.sics.internal.control;

import org.gumtree.gumnix.sics.control.IDeviceController;
import org.gumtree.gumnix.sics.core.SicsUtils;

import ch.psi.sics.hipadaba.Device;
import ch.psi.sics.hipadaba.Property;

public class PropertyEntry {

	private Property property;

	private Device parentDevice;

	private String path;

	private String currentValue;

	private String targetValue;

	private IDeviceController.PropertyStatus status;

	public PropertyEntry(Property property) {
		this(property, SicsUtils.getPath(property), SicsUtils.getParentDevice(property));
	}

	public PropertyEntry(Property property, String path, Device parentDevice) {
		this.property = property;
		this.path = path;
		this.parentDevice = parentDevice;
		status = IDeviceController.PropertyStatus.OUT_OF_SYNC;
	}

	public String getCurrentValue() {
		return currentValue;
	}

	public synchronized void setCurrentValue(String currentValue) {
		this.currentValue = currentValue;
		status = IDeviceController.PropertyStatus.IN_SYNC;
	}

	public String getTargetValue() {
		return targetValue;
	}

	public synchronized void setTargetValue(String targetValue) {
		this.targetValue = targetValue;
	}

	public Property getProperty() {
		return property;
	}

	public String getPath() {
		return path;
	}

	public Device getParentDevice() {
		return parentDevice;
	}

	public IDeviceController.PropertyStatus getStatus() {
		return status;
	}

}
