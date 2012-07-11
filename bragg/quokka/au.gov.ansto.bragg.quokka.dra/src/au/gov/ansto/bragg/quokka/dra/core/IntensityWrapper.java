/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lindsay Winkler (Bragg Institute) - initial implementation
 *******************************************************************************/

package au.gov.ansto.bragg.quokka.dra.core;

import au.gov.ansto.bragg.quokka.dra.algolib.core.InputParameterWrapper;
import au.gov.ansto.bragg.quokka.dra.algolib.core.QuokkaVectorCalculation;
import au.gov.ansto.bragg.quokka.dra.core.internal.ConcreteProcessor;

public class IntensityWrapper implements ConcreteProcessor {

	private InputParameterWrapper intensity_input;
	private Double intensity_output;
	
	public Boolean process() throws Exception {
		intensity_output = QuokkaVectorCalculation.getInstance().getIntensity(intensity_input);
		return false;
	}

	public Double getIntensity_output() {
		return intensity_output;
	}

	public void setIntensity_input(InputParameterWrapper intensity_input) {
		this.intensity_input = intensity_input;
	}

	

}
