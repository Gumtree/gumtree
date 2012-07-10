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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Utility which maps one set of bins to another via an intermediary set of bins. This
 * class only sets up correspondences between bins - it does not move any of the data
 * contained in the bins. For a given input bin, we can then get the corresponding
 * intermediate bin and output bin and can map data from the input bin to the
 * output bin via the intermediary.
 * 
 * @author lwi
 */
public class BinMapping {
	
	private List<DataBin> inputBins;
	private List<DataBin> outputBins;
	private List<DataBin> intermediateBins;
	
	private Map<DataBin, DataBin> intermediateToInput;
	private Map<DataBin, DataBin> intermediateToOutput;
	
	public BinMapping(List<DataBin> inputBins, List<DataBin> outputBins) {
		// Expect bins of uniform integer widths. Verify that this is the 
		// case before proceeding.
		
		// Verify input
		if (!binListIsValid(inputBins) || !binListIsValid(outputBins)) {
			String errorMessage ="Expect bins to have uniform integer widths.";
			throw new IllegalArgumentException(errorMessage);
		}
		
		// Find gcd of the two bin widths 
		// and create intermediate set of bins using this as the width.
		int inputBinWidth = (int) inputBins.get(0).getWidth();
		int outputBinWidth = (int) outputBins.get(0).getWidth();
		int intermediateBinWidth = MathUtil.gcd(inputBinWidth, outputBinWidth);
		
		Interval inputDomain = getDomain(inputBins);
		Interval outputDomain = getDomain(outputBins);
		
		if (!inputDomain.contains(outputDomain)) {
			String errorMessage = "Domain of output bins must be a subset of domain of input bins.";
			throw new IllegalArgumentException(errorMessage);
		}
		
		// Intermediate domain is the intersection of the input and output domains.
		Interval intermediateDomain = inputDomain.intersection(outputDomain);
		int numIntermediateBins = (int) intermediateDomain.getSize() / intermediateBinWidth;
		List<DataBin> intermediateBins = new LinkedList<DataBin>();
		for (int i = 0; i < numIntermediateBins; i++) {
			Interval interval = new Interval(i * intermediateBinWidth, (i + 1) * intermediateBinWidth);
			DataBin bin = new DataBin(interval, 0);
			intermediateBins.add(bin);
		}
		
		this.inputBins = inputBins;
		this.outputBins = outputBins;
		this.intermediateBins = intermediateBins;
		
		// Now set up the mappings between the bins.
		// Correspondences are calculated numerically and then verified.
		intermediateToInput = new HashMap<DataBin, DataBin>();
		intermediateToOutput = new HashMap<DataBin, DataBin>();
		
		for (int i = 0; i < intermediateBins.size(); i++) {

			DataBin bin = intermediateBins.get(i);
			double inputModFactor = inputBinWidth / intermediateBinWidth;
			double outputModFactor = outputBinWidth / intermediateBinWidth;
			int inputIndex = (int) (i / inputModFactor);
			int outputIndex = (int) (i / outputModFactor);
			
			intermediateToInput.put(bin, inputBins.get(inputIndex));
//			System.out.println(bin + " maps to " + inputBins.get(inputIndex));
			intermediateToOutput.put(bin, outputBins.get(outputIndex));
		}
		
		// --TEST--
		// Verification
		
		
		// --TEST--
	}
	
	public List<DataBin> getIntermediateBins() {
		return intermediateBins;
	}
	
	private Interval getDomain(List<DataBin> dataBins) {
		double firstPoint = dataBins.get(0).getStart();
		double lastPoint = dataBins.get(dataBins.size() - 1).getEnd();
		return new Interval(firstPoint, lastPoint);
	}
	
	/**
	 * Returns true if all the bins in the list have the same integer width.
	 * The bins are also required to cover some contiguous domain.
	 */
	private boolean binListIsValid(List<DataBin> bins) {
		
		if (bins.size() == 0) {
			return false;
		}
		
		double binWidth = bins.get(0).getWidth();
		
		if ((binWidth % 1) != 0) {
			return false;
		}
		
		for (DataBin bin : bins) {
			if (bin.getWidth() != binWidth) {
				return false;
			}
		}
		
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
		
		return true;
	}
	
	
	
	/**
	 * Get the intermediate <code>DataBin</code> to which the given input bin
	 * maps. 
	 */
	public DataBin getInputBin(DataBin intermediaryBin) {
		return intermediateToInput.get(intermediaryBin);
	}
	
	/**
	 * Get the output <code>DataBin</code> to which the given input bin
	 * ultimately maps. 
	 */
	public DataBin getOutputBin(DataBin intermediaryBin) {
		return intermediateToOutput.get(intermediaryBin);
	}
}
