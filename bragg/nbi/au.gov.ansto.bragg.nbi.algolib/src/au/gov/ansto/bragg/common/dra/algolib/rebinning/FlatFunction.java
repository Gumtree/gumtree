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

package au.gov.ansto.bragg.common.dra.algolib.rebinning;

/**
 * Implementation of a <code>DistributionFunction</code> which is just a flat
 * line.
 * 
 * @author lwi
 */
public class FlatFunction extends AbstractDistributionFunction implements DistributionFunction {
	
	private double height; // Height of the line above the x-axis.
	
	public FlatFunction(double height) {
		this.height = height;
	}

	public double getArea(Interval interval) {
		return height * (interval.getEnd() - interval.getStart());
	}

	public double getFunctionValue(double argument) {
		return height;
	}

}
