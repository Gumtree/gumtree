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

import java.util.Collections;
import java.util.List;

/**
 * A set of bins with associated counts, over some domain.
 * 
 * @author lwi
 */
public class Histogram extends BinCollection {

	private double binWidth;
	
	public Histogram(List<DataBin> bins, 
			DistributionFunction distributionFunction) {
		
		super(bins, distributionFunction);
		
		DataBin bin0 = bins.get(0);
		if (bin0 != null) {
			binWidth = bin0.getWidth();
		}
		
		for (DataBin currentBin : bins) {
			if (currentBin.getWidth() != binWidth) {
				String errorMessage = "Bins must all have the same size.";
				throw new IllegalArgumentException(errorMessage);
			}
		}
	}
	
	public double getBinWidth() {
		return binWidth;
	}
	
	/**
	 * Return the <code>DataBin</code> which contains the given point.
	 * 
	 * The point must lie within the domain of definition of the histogram.
	 */
	public DataBin getBinContaining(double point) {
		Interval domain = getDomain();
		if (!domain.contains(point)) {
			String errorMessage = "Test point must be within the domain of definition of the histogram";
			throw new IllegalArgumentException(errorMessage);
		}
		
		List<DataBin> bins = getBins();
		for (DataBin bin : bins) {
			if (bin.contains(point)) {
				return bin;
			}
		}
		
		return null; // But this should never happen!
	}
	
	/**
	 * Get the sum of counts in all bins.
	 */
	public double getTotalCount() {
		double result = 0;
		for (DataBin bin : getBins()) {
			result += bin.getCount();
		}
		return result;
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		for (DataBin bin : getBins()) {
			result.append(bin.toString() + "\n");
		}
		
		return result.toString();
	}
}
