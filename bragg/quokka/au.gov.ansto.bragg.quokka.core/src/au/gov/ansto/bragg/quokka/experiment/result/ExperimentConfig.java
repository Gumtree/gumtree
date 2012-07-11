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

public class ExperimentConfig {

	private String name;
	
	@XStreamImplicit
	private List<TransmissionMeasurement> transmissionMeasurements;
	
	@XStreamImplicit
	private List<ScatteringMeasurement> scatteringMeasurements;
	
	private String emptyCellTransmission;
	
	private String emptyCellScattering;
	
	private String emptyBeamTransmission;
	
	private String emptyBeamScattering;
	
	private String backgroundTransmission;
	
	private String backgroundScattering;
	
	private float lambda; 
	
	private float l1;
	
	private float l2; 
	
	public String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	public List<TransmissionMeasurement> getTransmissionMeasurements() {
		if (transmissionMeasurements == null) {
			transmissionMeasurements = new ArrayList<TransmissionMeasurement>(2);
		}
		return transmissionMeasurements;
	}

	public List<ScatteringMeasurement> getScatteringMeasurements() {
		if (scatteringMeasurements == null) {
			scatteringMeasurements = new ArrayList<ScatteringMeasurement>(2);
		}
		return scatteringMeasurements;
	}

	public TransmissionMeasurement getFirstTransmissionMeasurement() {
		if (getTransmissionMeasurements().size() > 0) {
			return getTransmissionMeasurements().get(0);
		}
		return null;
	}
	
	public ScatteringMeasurement getFirstScatteringMeasurement() {
		if (getScatteringMeasurements().size() > 0) {
			return getScatteringMeasurements().get(0);
		}
		return null;
	}

	public String getEmptyCellTransmission() {
		return emptyCellTransmission;
	}

	protected void setEmptyCellTransmission(String emptyCellTransmission) {
		this.emptyCellTransmission = emptyCellTransmission;
	}

	public String getEmptyCellScattering() {
		return emptyCellScattering;
	}

	protected void setEmptyCellScattering(String emptyCellScattering) {
		this.emptyCellScattering = emptyCellScattering;
	}

	public String getEmptyBeamTransmission() {
		return emptyBeamTransmission;
	}

	protected void setEmptyBeamTransmission(String emptyBeamTransmission) {
		this.emptyBeamTransmission = emptyBeamTransmission;
	}

	public String getEmptyBeamScattering() {
		return emptyBeamScattering;
	}

	protected void setEmptyBeamScattering(String emptyBeamScattering) {
		this.emptyBeamScattering = emptyBeamScattering;
	}

	public String getBackgroundTransmission() {
		return backgroundTransmission;
	}

	protected void setBackgroundTransmission(String backgroundTransmission) {
		this.backgroundTransmission = backgroundTransmission;
	}

	public String getBackgroundScattering() {
		return backgroundScattering;
	}

	protected void setBackgroundScattering(String backgroundScattering) {
		this.backgroundScattering = backgroundScattering;
	}

	public float getLambda() {
		return lambda;
	}

	protected void setLambda(float lambda) {
		this.lambda = lambda;
	}
	
	public float getL1() {
		return l1;
	}

	protected void setL1(float l1) {
		this.l1 = l1;
	}

	public float getL2() {
		return l2;
	}

	protected void setL2(float l2) {
		this.l2 = l2;
	}
	
}
