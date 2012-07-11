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

package au.gov.ansto.bragg.quokka.experiment.report;

public class ExperimentConfig {

	private String name;
	
	private float lambda; 
	
	private float l1;
	
	private float l2;

	private TransmissionMeasurement transmissionMeasurement;
	
	private ScatteringMeasurement scatteringMeasurement;
	
	// [[GT-207] The following 3 attributes relate to the file association propagation
	private String emptyCellTransmissionRunId;
	
	private String emptyCellScatteringRunId;
	
	private String emptyBeamTransmissionRunId;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getLambda() {
		return lambda;
	}

	public void setLambda(float lambda) {
		this.lambda = lambda;
	}

	public float getL1() {
		return l1;
	}

	public void setL1(float l1) {
		this.l1 = l1;
	}

	public float getL2() {
		return l2;
	}

	public void setL2(float l2) {
		this.l2 = l2;
	}

	public ScatteringMeasurement getScatteringMeasurement() {
		return scatteringMeasurement;
	}

	public void setScatteringMeasurement(ScatteringMeasurement scatteringMeasurement) {
		this.scatteringMeasurement = scatteringMeasurement;
	}

	public TransmissionMeasurement getTransmissionMeasurement() {
		return transmissionMeasurement;
	}

	public void setTransmissionMeasurement(
			TransmissionMeasurement transmissionMeasurement) {
		this.transmissionMeasurement = transmissionMeasurement;
	}

	public String getEmptyCellTransmissionRunId() {
		return emptyCellTransmissionRunId;
	}

	public void setEmptyCellTransmissionRunId(String emptyCellTransmissionRunId) {
		this.emptyCellTransmissionRunId = emptyCellTransmissionRunId;
	}

	public String getEmptyCellScatteringRunId() {
		return emptyCellScatteringRunId;
	}

	public void setEmptyCellScatteringRunId(String emptyCellScatteringRunId) {
		this.emptyCellScatteringRunId = emptyCellScatteringRunId;
	}

	public String getEmptyBeamTransmissionRunId() {
		return emptyBeamTransmissionRunId;
	}

	public void setEmptyBeamTransmissionRunId(String emptyBeamTransmissionRunId) {
		this.emptyBeamTransmissionRunId = emptyBeamTransmissionRunId;
	}
		
}
