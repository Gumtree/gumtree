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

public class QMaxVWrapper implements ConcreteProcessor {

	private InputParameterWrapper qmaxv_input;
	private Double qmaxv_output; 
	
	public Double getQmaxv_output() {
		return qmaxv_output;
	}

	public void setQmaxv_input(InputParameterWrapper qmaxv_input) {
		this.qmaxv_input = qmaxv_input;
	}

	public Boolean process() throws Exception {
		qmaxv_output = QuokkaVectorCalculation.getInstance().getQMaxV(qmaxv_input);
		return false;
	}

}
