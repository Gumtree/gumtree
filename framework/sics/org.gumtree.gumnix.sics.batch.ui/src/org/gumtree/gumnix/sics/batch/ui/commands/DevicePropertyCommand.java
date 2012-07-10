/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package org.gumtree.gumnix.sics.batch.ui.commands;

public class DevicePropertyCommand extends AbstractSicsCommand {

	private String deviceId;
	
	private String propertyId;
	
	private String value;
	
	public DevicePropertyCommand() {
		super();
	}
	
	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		String oldValue = this.deviceId;
		this.deviceId = deviceId;
		firePropertyChange("deviceId", oldValue, deviceId);
	}

	public String getPropertyId() {
		return propertyId;
	}

	public void setPropertyId(String propertyId) {
		String oldValue = this.propertyId;
		this.propertyId = propertyId;
		firePropertyChange("propertyId", oldValue, propertyId);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		String oldValue = this.value;
		this.value = value;
		firePropertyChange("value", oldValue, value);
	}

	public String toScript() {
		if (getDeviceId() != null) {
			if (getPropertyId() != null) {
				if (getValue() != null) {
					return getDeviceId() + " " + getPropertyId() + " "	+ getValue();
				}
				else {
					return getDeviceId() + " " + getPropertyId();
				}
			} else {
				return getDeviceId();
			}
		}
		return "";
	}

}
