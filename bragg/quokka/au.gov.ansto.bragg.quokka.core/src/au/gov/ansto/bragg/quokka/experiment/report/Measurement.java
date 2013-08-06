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

import java.util.ArrayList;
import java.util.List;

import au.gov.ansto.bragg.quokka.experiment.model.ScanMode;

public class Measurement {

	private ScanMode mode;
	
	private List<SampleResult> samples;

	public ScanMode getMode() {
		return mode;
	}

	public void setMode(ScanMode mode) {
		this.mode = mode;
	}

	public List<SampleResult> getSamples() {
		if (samples == null) {
			samples = new ArrayList<SampleResult>(2);
		}
		return samples;
	}
	
}
