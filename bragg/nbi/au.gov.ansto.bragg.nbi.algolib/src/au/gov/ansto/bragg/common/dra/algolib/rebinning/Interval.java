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

/**
 * Encapsulation of the concept of a mathematical interval.
 */
package au.gov.ansto.bragg.common.dra.algolib.rebinning;

import java.util.List;

public class Interval {

	private double start;
	private double end;
	
	public Interval(double start, double end) {
		if (start >= end) {
			throw new IllegalArgumentException("Require start < end.");
		}
	
		this.start = start;
		this.end = end;
	}
	
	public double getStart() {
		return start;
	}
	
	public double getEnd() {
		return end;
	}
	
	public double getSize() {
		return end - start;
	}
	
	/**
	 * Returns <code>true</code> if the interval passed as a parameter is a subset
	 * of this one, <code>false</code> otherwise. 
	 */
	public boolean contains(Interval testInterval) {
		if (testInterval.getStart() < start || testInterval.getEnd() > end) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Check whether a particular point falls within a given interval.
	 */
	public boolean contains(double testPoint) {
		if (testPoint < start || testPoint > end) {
			return false;
		}
		return true;
	}
	
	/**
	 * Compute the union of this <code>Interval</code> with another one.
	 * The interval passed as an argument must not be mutually exclusive
	 * with this one. This means that the returned result will be a 
	 * single interval.
	 */
	public Interval union(Interval interval) {
		// Do not accept mutually exclusive intervals as arguments.
		if (intersection(interval) == null) {
			String errorMessage = "Intervals must not be mutually exclusive.";
			throw new IllegalArgumentException(errorMessage);
		}
		
		Interval interval0;
		Interval interval1;
		
		if (getStart() < interval.getStart()) {
			interval0 = this;
			interval1 = interval;
		} else {
			interval0 = interval;
			interval1 = this;
		}
		
		// Now the union will be the interval between the start of the
		// first interval and the end of the second.
		return new Interval(interval0.getStart(), interval1.getEnd());
	}
	
	/**
	 * Compute the intersection of this <code>Interval</code> with 
	 * another one. The intersection is potentially empty, in which
	 * case <code>null</code> will be returned. 
	 */
	public Interval intersection(Interval interval) {
		
		Interval interval0;
		Interval interval1;
		
		if (getStart() < interval.getStart()) {
			interval0 = this;
			interval1 = interval;
		} else {
			interval0 = interval;
			interval1 = this;
		}
		
		if (interval0.getEnd() < interval1.getStart()) {
			return null;
		}
		
		// Otherwise, intersection will be the interval between
		// the start of the second interval and the end of the first.
		return new Interval(interval1.getStart(), interval0.getEnd());
	}
	
	public boolean equals(Object arg) {
		// Equal intervals have the same start and end points.
		if (!(arg instanceof Interval)) {
			return false;
		}
		
		Interval intervalToCompare = (Interval) arg;
		
		if (intervalToCompare.getStart() == getStart()
			&& intervalToCompare.getEnd() == getEnd()) {
			
			return true;
		}
		
		return false;
	}
	
	public int hashCode() {
		return (int) (getStart() + getEnd());
	}
}
