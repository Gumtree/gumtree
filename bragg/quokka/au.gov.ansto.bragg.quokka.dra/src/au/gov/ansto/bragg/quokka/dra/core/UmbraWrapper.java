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

public class UmbraWrapper implements ConcreteProcessor {

	private InputParameterWrapper umbra_input;
	private Double umbra_output; 
	
	public Double getUmbra_output() {
		return umbra_output;
	}

	public void setUmbra_input(InputParameterWrapper umbra_input) {
		this.umbra_input = umbra_input;
	}

	public Boolean process() throws Exception {
		umbra_output = QuokkaVectorCalculation.getInstance().getUmbra(umbra_input);
		return false;
	}

}
