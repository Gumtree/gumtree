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
 * Description of a function. The function needs to have analytic descriptions of
 * itself and of its indefinite integral.
 * 
 * @author lwi
 */
public interface DistributionFunction {

	/**
	 * Get the value of the function at a certain point.
	 */
	public double getFunctionValue(double argument);
	
	/**
	 * Get the area under the function's curve between the two
	 * points specified.
	 */
	public double getArea(Interval interval);
	
	/**
	 * Dividing the area under the function curve over some interval into two portions,
	 * this method indicates what proportion of the area falls into each division.
	 * 
	 * The return value gives the proportion of the total area for the interval
	 * that falls to the left of the division.
	 * 
	 * @param intervalStart Left-hand end of the interval to be divided
	 * @param intervalEnd Right-hand end of the interval to be divided
	 * @param division Point about which the interval is divided
	 */
	public double getProportion(double intervalStart, double intervalEnd, double division);
	
	/**
	 * Get the proportion of the area of the entire <code>Interval</code> which 
	 * falls within a smaller <code>Interval</code>.
	 * 
	 * @param completeInterval Complete <code>Interval</code> to be sub-sampled.
	 * @param innerInterval Smaller <code>Interval</code> which must be
	 * contained within the larger one.
	 * @return Ratio of area of <code>innerInterval</code> to area of 
	 * <code>completeInterval</code>.
	 */
	public double getProportion(Interval completeInterval, Interval innerInterval);
}
