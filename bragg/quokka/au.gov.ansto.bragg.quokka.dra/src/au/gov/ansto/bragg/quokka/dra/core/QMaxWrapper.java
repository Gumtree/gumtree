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

public class QMaxWrapper implements ConcreteProcessor {

	private InputParameterWrapper qmax_input;
	private Double qmax_output;
	
	public Double getQmax_output() {
		return qmax_output;
	}

	public void setQmax_input(InputParameterWrapper qmax_input) {
		this.qmax_input = qmax_input;
	}

	public Boolean process() throws Exception {
		qmax_output = QuokkaVectorCalculation.getInstance().getQMax(qmax_input);
		return false;
	}

}
