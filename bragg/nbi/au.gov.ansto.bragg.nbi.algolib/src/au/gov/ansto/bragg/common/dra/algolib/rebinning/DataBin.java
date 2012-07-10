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
 * An <code>Interval</code>	with an associated count. An array of 
 * <code>DataBin</code>s defines a histogram.
 * 
 * @author lwi
 */
public class DataBin {

	private Interval interval;
	private double count;
	
	public DataBin(Interval interval, double count) {
		this.interval = interval;
		this.count = count;
	}
	
	
	// Expose necessary data from interval to conform to
	// "Law of Demeter" design guideline.

	public double getStart() {
		return interval.getStart();
	}
	
	public double getEnd() {
		return interval.getEnd();
	}
	
	public void setCount(double count) {
		this.count = count;
	}
	
	public double getCount() {
		return count;
	}
	
	public double getWidth() {
		return getEnd() - getStart();
	}
	
	public Interval getInterval() {
		return interval;
	}
	
	public boolean contains(double point) {
		return interval.contains(point); // Implemented to satisfy Law of Demeter.
	}
	
	public String toString() {
		return "[" + getStart() + " -> " + getEnd() + "], Count = " + getCount();
	}
}
