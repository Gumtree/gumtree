package org.gumtree.gumnix.sics.control;

import ch.psi.sics.hipadaba.Device;
import ch.psi.sics.hipadaba.Property;

public interface IDeviceListener {

	public void propertyChanged(Device device, Property property, String newValue);

}
