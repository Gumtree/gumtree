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

public class SigmaQMinWrapper implements ConcreteProcessor {
	
	private InputParameterWrapper sigmaqmin_input;
	private Double sigmaqmin_output;

	public Double getSigmaqmin_output() {
		return sigmaqmin_output;
	}

	public void setSigmaqmin_input(InputParameterWrapper sigmaqmin_input) {
		this.sigmaqmin_input = sigmaqmin_input;
	}

	public Boolean process() throws Exception {
		sigmaqmin_output = QuokkaVectorCalculation.getInstance().getSigmaQMin(sigmaqmin_input);
		return false;
	}

}
