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

import java.util.HashMap;
import java.util.Map;

public class ControlledAcquisition extends Acquisition {

	private Map<SampleEnvironment, SampleEnvironmentPreset> envSettings;
	
	public ControlledAcquisition(Experiment experiment, Map<SampleEnvironment, SampleEnvironmentPreset> envSettings) {
		super(experiment);
		this.envSettings = envSettings;
	}
	
	public Map<SampleEnvironment, SampleEnvironmentPreset> getEnvSettings() {
		if (envSettings == null) {
			envSettings = new HashMap<SampleEnvironment, SampleEnvironmentPreset>();
		}
		return envSettings;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ControlledAcquisition [envSettings=");
		builder.append(envSettings);
		builder.append("]");
		return builder.toString();
	}
	
}
