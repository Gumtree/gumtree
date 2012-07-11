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

public class QMinWrapper implements ConcreteProcessor {

	private InputParameterWrapper qmin_input;
	private Double qmin_output;
	
	public Double getQmin_output() {
		return qmin_output;
	}

	public void setQmin_input(InputParameterWrapper qmin_input) {
		this.qmin_input = qmin_input;
	}

	public Boolean process() throws Exception {
		qmin_output = QuokkaVectorCalculation.getInstance().getQMin(qmin_input);
		return false;
	}

}
