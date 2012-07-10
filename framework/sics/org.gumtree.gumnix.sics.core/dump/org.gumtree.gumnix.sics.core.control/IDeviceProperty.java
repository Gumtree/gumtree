package org.gumtree.gumnix.sics.control;

import ch.psi.sics.hipadaba.Device;
import ch.psi.sics.hipadaba.Property;

public interface IDeviceProperty extends Property {

	public Device getDevice();

}
