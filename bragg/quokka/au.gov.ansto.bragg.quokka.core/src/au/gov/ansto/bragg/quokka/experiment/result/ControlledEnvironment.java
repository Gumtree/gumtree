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

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class ControlledEnvironment {
	
	@XStreamImplicit
	private List<EnvironmentSetting> settings;

	@XStreamImplicit
	private List<ExperimentConfig> configs;

	
	public ControlledEnvironment() {
		super();
	}
	
	public List<ExperimentConfig> getConfigs() {
		if (configs == null) {
			configs = new ArrayList<ExperimentConfig>(2);
		}
		return configs;
	}
	
	public List<EnvironmentSetting> getEnvironments() {
		if (settings == null) {
			settings = new ArrayList<EnvironmentSetting>(2);
		}
		return settings;
	}
	
}
