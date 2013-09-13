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

package au.gov.ansto.bragg.quokka.experiment.model;

/**
 * Sample environment preset holds individual preset record of a sample
 * environment setting.
 * 
 */
public class SampleEnvironmentPreset extends AbstractModelObject {
	// fields
	private int number;
	private float preset;
	private int waitTime;

	// constructors
	public SampleEnvironmentPreset(int number) {
		super();
		this.number = number;
	}
	public SampleEnvironmentPreset(int number, float preset, int waitTime) {
		super();
		this.number = number;
		this.preset = preset;
		this.waitTime = waitTime;
	}
	
	// properties
	public int getNumber() {
		return number;
	}
	public float getPreset() {
		return preset;
	}
	public void setPreset(float preset) {
		float oldValue = this.preset;
		this.preset = preset;
		firePropertyChange("preset", oldValue, preset);
	}
	public int getWaitTime() {
		return waitTime;
	}
	public void setWaitTime(int waitTime) {
		int oldValue = this.waitTime;
		this.waitTime = waitTime;
		firePropertyChange("waitTime", oldValue, waitTime);
	}

	// equals
	public boolean equals(Object obj) {
		if (obj instanceof SampleEnvironmentPreset) {
			SampleEnvironmentPreset sampleEnvPreset = (SampleEnvironmentPreset) obj;
			return
				(number == sampleEnvPreset.number) &&
				(preset == sampleEnvPreset.preset) &&
				(waitTime == sampleEnvPreset.waitTime);
		}
		return false;
	}
	public int hashCode() {
		return number;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SampleEnvironmentPreset");
		builder.append(" [number=").append(number);
		builder.append(", preset=").append(preset);
		builder.append(", waitTime=").append(waitTime);
		builder.append("]");
		return builder.toString();
	}

}
