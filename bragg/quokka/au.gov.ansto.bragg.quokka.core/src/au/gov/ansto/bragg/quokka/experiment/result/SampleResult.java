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

import au.gov.ansto.bragg.quokka.experiment.util.SampleType;

public class SampleResult {

	private int position;
	
	private String name;
	
	private String description;
	
	private float thickness;
	
	private SampleType type;
	
	// This is null if the experiment is run in controlled environment mode
	@XStreamImplicit
	private List<ExperimentConfig> configs;

	// This is empty if the experiment is run in normal environment mode
	@XStreamImplicit
	private List<ControlledEnvironment> controlledEnvs;
	
	public SampleResult() {
		super();
	}

	public int getPosition() {
		return position;
	}

	protected void setPosition(int position) {
		this.position = position;
	}

	public String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	protected void setDescription(String description) {
		this.description = description;
	}

	public float getThickness() {
		return thickness;
	}

	protected void setThickness(float thickness) {
		this.thickness = thickness;
	}

	public SampleType getType() {
		return type;
	}

	protected void setType(SampleType type) {
		this.type = type;
	}

	public List<ExperimentConfig> getConfigs() {
		if (configs == null) {
			configs = new ArrayList<ExperimentConfig>(2);
		}
		return configs;
	}

	public List<ControlledEnvironment> getControlledEnvs() {
		if (controlledEnvs == null) {
			controlledEnvs = new ArrayList<ControlledEnvironment>(2);
		}
		return controlledEnvs;
	}
	
	
	
}
