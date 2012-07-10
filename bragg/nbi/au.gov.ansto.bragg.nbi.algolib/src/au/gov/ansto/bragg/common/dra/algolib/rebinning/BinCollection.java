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
 * A collection of <code>DataBin</code>s, similar to a <code>Histogram</code>, but
 * not requiring bins to be of equal widths.
 *  
 * @author lwi
 */
public class BinCollection {

	protected List<DataBin> dataBins;
	protected DistributionFunction distributionFunction;
	
	public BinCollection(List<DataBin> bins, 
			DistributionFunction distributionFunction) {
		// Confirm that the bins supplied cover a contiguous region, 
		// and reject them if they do not.
		// Union of all intervals should be the domain, intersection
		// should be empty.
		
		// Sum of lengths of all intersections should be 0.
		Collections.sort(bins, new DataBinStartPointComparator());
		
		DataBin previousBin = null;
		for (DataBin currentBin : bins) {
			
			if (previousBin != null) {
				if (previousBin.getEnd() != currentBin.getStart()) {
					String errorMessage = "Bins must be mutually exclusive and cover the domain";
					throw new IllegalArgumentException(errorMessage);
				}
			}
			previousBin = currentBin;
		}
		
		// FIXME
		// Should we make a deep copy here? But the copy would have to be
		// very deep - we would also have to deep copy the individual bins.
		dataBins = bins;
		this.distributionFunction = distributionFunction;
	}
	
	public List<DataBin> getBins() {
		return dataBins;
	}

	/**
	 * Get the entire domain over which this <code>Histogram</code>
	 * is defined.
	 */
	public Interval getDomain() {
		double firstPoint = dataBins.get(0).getStart();
		double lastPoint = dataBins.get(dataBins.size() - 1).getEnd();
		return new Interval(firstPoint, lastPoint);
	}

	public int getNumBins() {
		return dataBins.size();
	}

	/**
	 * Set count in bin i to <code>count</code>.
	 * @param index
	 * @param count
	 */
	public void setCount(int index, int count) {
		dataBins.get(index).setCount(count);
	}

	/**
	 * Get the count held in bin i.
	 */
	public double getCount(int index) {
		return dataBins.get(index).getCount();
	}

	public DistributionFunction getDistributionFunction() {
		return distributionFunction;
	}

}
