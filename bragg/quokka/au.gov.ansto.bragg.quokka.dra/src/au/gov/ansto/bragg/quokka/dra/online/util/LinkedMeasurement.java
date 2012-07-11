/******************************************************************************* 
* Copyright (c) 2009 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Paul Hathaway - July 2009
*******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.online.util;

import au.gov.ansto.bragg.quokka.experiment.result.*;

public class LinkedMeasurement {

	private ExperimentResult mResult;
	private SampleResult mSample;
	private ExperimentConfig mConfig;
	private Measurement mMeasurement;
	
	public LinkedMeasurement(Measurement m) {
		this.mMeasurement = m;
	}
	
	public ExperimentResult getExperimentResult() {
		return mResult;
	}

	public void setExperimentResult(ExperimentResult result) {
		mResult = result;
	}

	public SampleResult getSampleResult() {
		return mSample;
	}

	public void setSampleResult(SampleResult sample) {
		mSample = sample;
	}

	public ExperimentConfig getConfig() {
		return mConfig;
	}

	public void setConfig(ExperimentConfig config) {
		mConfig = config;
	}

	public Measurement getMeasurement() {
		return mMeasurement;
	}
}
