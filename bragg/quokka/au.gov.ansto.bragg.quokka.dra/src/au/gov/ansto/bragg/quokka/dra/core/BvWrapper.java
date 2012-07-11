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

public class BvWrapper implements ConcreteProcessor {

	private InputParameterWrapper bv_input;
	private Double bv_output;
	
	public Double getBv_output() {
		return bv_output;
	}

	public void setBv_input(InputParameterWrapper bv_input) {
		this.bv_input = bv_input;
	}

	public Boolean process() throws Exception {
		bv_output = QuokkaVectorCalculation.getInstance().getBv(bv_input);
		return false;
	}

}
