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

public class QMaxHWrapper implements ConcreteProcessor {

	private InputParameterWrapper qmaxh_input;
	private Double qmaxh_output;
	
	public Double getQmaxh_output() {
		return qmaxh_output;
	}

	public void setQmaxh_input(InputParameterWrapper qmaxh_input) {
		this.qmaxh_input = qmaxh_input;
	}

	public Boolean process() throws Exception {
		qmaxh_output = QuokkaVectorCalculation.getInstance().getQMaxH(qmaxh_input);
		return false;
	}

}
