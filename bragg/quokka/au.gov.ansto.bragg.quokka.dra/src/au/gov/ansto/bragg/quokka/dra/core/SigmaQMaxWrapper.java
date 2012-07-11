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

public class SigmaQMaxWrapper implements ConcreteProcessor {

	private InputParameterWrapper sigmaqmax_input;
	private Double sigmaqmax_output;
	
	public Double getSigmaqmax_output() {
		return sigmaqmax_output;
	}

	public void setSigmaqmax_input(InputParameterWrapper sigmaqmax_input) {
		this.sigmaqmax_input = sigmaqmax_input;
	}

	public Boolean process() throws Exception {
		sigmaqmax_output = QuokkaVectorCalculation.getInstance().getSigmaQMax(sigmaqmax_input);
		return false;
	}

}
