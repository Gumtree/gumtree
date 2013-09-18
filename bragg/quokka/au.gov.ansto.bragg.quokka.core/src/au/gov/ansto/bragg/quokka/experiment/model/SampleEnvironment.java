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
 * Sample environment maps a controller and its scan settings.
 *
 */
public class SampleEnvironment extends AbstractModelObject {
	
	private String controllerId;

	private PropertyList<SampleEnvironmentPreset> presets;
	
	private Experiment experiment;
	
	public SampleEnvironment(Experiment experiment) {
		super();
		this.experiment = experiment;
	}
	
	public String getControllerId() {
		return controllerId;
	}

	public void setControllerId(String controllerId) {
		String oldValue = this.controllerId;
		this.controllerId = controllerId;
		firePropertyChange("controllerId", oldValue, controllerId);
	}
	
	public PropertyList<SampleEnvironmentPreset> getPresets() {
		if (presets == null) {
			presets = new PropertyList<SampleEnvironmentPreset>(this, "presets");
			// Default
//			presets.add(new SampleEnvironmentPreset(100, 1));
		}
		return presets;
	}

	/*************************************************************************
	 * Helper methods 
	 *************************************************************************/
	public void resetPresets(float start, float end, int steps, int waitTime) {
		// Clear previous presets
		getPresets().clear();
		
		// Calculate and set new presets
		if (steps < 2)
			getPresets().add(new SampleEnvironmentPreset(
				1,
				start,
				waitTime));
		else
			for (int i = 0; i < steps; i++)
				getPresets().add(new SampleEnvironmentPreset(
					i + 1,
					round(start + (i / ((float)steps - 1)) * (end - start), 4),
					waitTime));

		// Update
		experiment.updateControlledAcquisition();
		// Fire update since the model has finished the actual update
		firePropertyChange("this", null, this);
	}
	
	private static float round(float val, int place) {
		float p = (float) Math.pow(10, place);
		val = val * p;
		float tmp = Math.round(val);
		return (float) tmp / p;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SampleEnvironment [controllerId=");
		builder.append(controllerId);
		builder.append(", experiment=");
		builder.append(experiment);
		builder.append(", presets=");
		builder.append(presets);
		builder.append("]");
		return builder.toString();
	}
	
}
