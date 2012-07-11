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

package au.gov.ansto.bragg.quokka.experiment.result;

public class EnvironmentSetting {

	private String controllerId;
	
	private float preset;
	
	public String getControllerId() {
		return controllerId;
	}

	protected void setControllerId(String controllerId) {
		this.controllerId = controllerId;
	}

	public float getPreset() {
		return preset;
	}

	protected void setPreset(float preset) {
		this.preset = preset;
	}
	
}
