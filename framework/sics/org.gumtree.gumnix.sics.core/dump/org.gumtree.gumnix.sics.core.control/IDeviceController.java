package org.gumtree.gumnix.sics.control;

import ch.psi.sics.hipadaba.Device;
import ch.psi.sics.hipadaba.Property;

public interface IDeviceController extends IComponentController {

	public enum PropertyStatus {
		IN_SYNC, OUT_OF_SYNC
	}

	public Device getDevice();

	public Property getDefaultProperty();

	public void setPropertyValue(Property property, String newValue);

	public String getPropertyCurrentValue(Property property);

	public void getPropertyCurrentValue(Property property, IDevicePropertyCallback callback);

	public String getPropertyTargetValue(Property property);

	public PropertyStatus getPropertyStatus(Property property);

	public void addDeviceListener(IDeviceListener listener);

	public void removeDeviceListener(IDeviceListener listener);

}
