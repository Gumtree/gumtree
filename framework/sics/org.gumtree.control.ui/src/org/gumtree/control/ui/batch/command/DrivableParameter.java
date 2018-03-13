/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.control.ui.batch.command;

import org.gumtree.workflow.ui.models.AbstractModelObject;

public class DrivableParameter extends AbstractModelObject {

	private String deviceId;
	
	private float target;

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		String oldValue = this.deviceId;
		this.deviceId = deviceId;
		firePropertyChange("deviceId", oldValue, deviceId);
	}

	public float getTarget() {
		return target;
	}

	public void setTarget(float target) {
		float oldValue = this.target;
		this.target = target;
		firePropertyChange("target", oldValue, target);
	}
		
}
