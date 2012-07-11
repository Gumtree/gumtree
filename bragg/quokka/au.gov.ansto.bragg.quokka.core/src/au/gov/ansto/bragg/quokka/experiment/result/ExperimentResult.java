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

import au.gov.ansto.bragg.quokka.experiment.util.SampleType;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("result")
public class ExperimentResult {

	private boolean isControlledEnvironment;
	
	private String sensitivityFile;

	@XStreamImplicit
	private List<SampleResult> sampleResults;
		
	public ExperimentResult() {
		super();
	}
	
	public List<SampleResult> getSampleResults() {
		if (sampleResults == null) {
			sampleResults = new ArrayList<SampleResult>();
		}
		return sampleResults;
	}

	// TODO: review additional method pvhathaway 16/6/2009
	public List<SampleResult> getSampleResults(SampleType sType) {
		ArrayList<SampleResult> samples = new ArrayList<SampleResult>();		
		for(SampleResult m : sampleResults) {
			if(sType == m.getType()) {
				samples.add(m);
			}
		}
		return samples;
	}

	public boolean isControlledEnvironment() {
		return isControlledEnvironment;
	}

	protected void setControlledEnvironment(boolean isControlledEnvironment) {
		this.isControlledEnvironment = isControlledEnvironment;
	}

	public String getSensitivityFile() {
		return sensitivityFile;
	}

	protected void setSensitivityFile(String sensitivityFile) {
		this.sensitivityFile = sensitivityFile;
	}
	
}
