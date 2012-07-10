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

package au.gov.ansto.bragg.errorpropagation;

/**
 * Encapsulation of a data value with associated measurement variance.
 *  
 * @author lwi
 */
public class ScalarWithVariance {

	private double data;
	private double variance;
	
	public ScalarWithVariance(double data, double error) {
		this.data = data;
		this.variance = error;
	}
	
	public double getData() {
		return data;
	}
	
	public double getVariance() {
		return variance;
	}
}
