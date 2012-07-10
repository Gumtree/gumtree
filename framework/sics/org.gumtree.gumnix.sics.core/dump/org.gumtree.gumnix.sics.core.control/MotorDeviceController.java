package org.gumtree.gumnix.sics.control;

import java.util.List;

import org.gumtree.gumnix.sics.control.IComponentController.ComponentStatus;
import org.gumtree.gumnix.sics.internal.control.PropertyEntry;

import ch.psi.sics.hipadaba.DataType;
import ch.psi.sics.hipadaba.Property;

public class MotorDeviceController extends DeviceController {

	private Property positionProperty;

	public MotorDeviceController() {
		super();
	}

	public void updatePropertyEntry(String path, String value) {
		super.updatePropertyEntry(path, value);
	}

	private Property getPositionProperty() {
		if(positionProperty == null) {
			for(Property property : (List<Property>)getDevice().getProperty()) {
				if(property.getId().equals("position")) {
					positionProperty = property;
					break;
				}
			}
		}
		return positionProperty;
	}
}
