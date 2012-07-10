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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Exists to manage the proper re-binning of data in a reduction context.
 * 
 * @author lwi
 */
public class ReductionManager {
	
	/**
	 * Re-bin data from the input <code>Histogram</code> into another histogram
	 * with different bin widths. Note that due to rounding errors, the sum of
	 * all the counts may be changed slightly by this operation.
	 * 
	 * We expect integer bin widths for the input and output histograms, and that 
	 * the output interval is a multiple of the output bin width.
	 */
	public Histogram rebin(Histogram input,
			double outputBinWidth,
			Interval outputInterval) {
		
		// Create the output bins
		if ((outputBinWidth % 1) != 0) {
			String errorMessage = "outputBinWidth should be an integer value.";
			throw new IllegalArgumentException(errorMessage);
		}
		
		double outputIntervalWidth = outputInterval.getSize();
		if ((outputIntervalWidth % outputBinWidth) != 0) {
			String errorMessage = "outputIntervalWidth should be a multiple of outputBinWidth";
			throw new IllegalArgumentException(errorMessage);
		}
		
		int numOutputBins = (int) (outputIntervalWidth / outputBinWidth);
		List<DataBin> outputBins = new LinkedList<DataBin>();
		int outputIntervalStart = (int) outputInterval.getStart();
		for (int i = 0; i < numOutputBins; i++) {
			int intervalStart = (int) (outputIntervalStart + (i * outputBinWidth));
			int intervalEnd = (int) (outputIntervalStart + ((i + 1) * outputBinWidth));
			Interval interval = new Interval(intervalStart, intervalEnd);
			DataBin bin = new DataBin(interval, 0);
			outputBins.add(bin);
		}
		
		// Create the bin mapping between them
		List<DataBin> inputBins = input.getBins();
		BinMapping binMapping = new BinMapping(inputBins, outputBins);
		
		// Populate intermediate bins 
		// Find the difference in the total count between the input bin
		// section and the intermediate bin section and distribute the difference
		// evenly amongst the intermediate bins.
		List<DataBin> intermediateBins = binMapping.getIntermediateBins();
		DistributionFunction distributionFunction = input.getDistributionFunction();
		
		Map<DataBin, List<DataBin>> matchingBins = new HashMap<DataBin, List<DataBin>>();
		for (DataBin intermediateBin : intermediateBins) {
			
			// Maintain a list of all the input bins corresponding to the current
			// intermediate bin.
			// After counts have been transferred, we need to ensure that the total 
			// counts match between the current intermediate bin and the sum of counts
			// in the input bins.
			
			DataBin correspondingInputBin = binMapping.getInputBin(intermediateBin);
			
			Set<DataBin> keys = matchingBins.keySet();
			if (!keys.contains(correspondingInputBin)) {
				matchingBins.put(correspondingInputBin, new LinkedList<DataBin>());
			}
			
			List<DataBin> currentMatches = matchingBins.get(correspondingInputBin);
			currentMatches.add(intermediateBin);
			
			Interval correspondingInputInterval = correspondingInputBin.getInterval(); 
			Interval intermediateInterval = intermediateBin.getInterval();
			if (!correspondingInputInterval.contains(intermediateInterval)) {
				String errorMessage = "Input interval must contain intermediate interval.";
				throw new IllegalArgumentException(errorMessage);
			}
			double proportion = distributionFunction.getProportion(correspondingInputInterval, intermediateInterval);
			double inputCount = correspondingInputBin.getCount();
			double intermediateCount = proportion * inputCount; 
			intermediateBin.setCount(intermediateCount);
		}
		
		// Now search through matching bins and ensure that counts are always matching.
		// This can be extracted into a separate method.
		Set<DataBin> keys = matchingBins.keySet();
		for (DataBin key : keys) {
			List<DataBin> currentMatches = matchingBins.get(key);
			double matchCount = key.getCount();
			double sumCount = 0;
			for (DataBin bin : currentMatches) {
				sumCount += bin.getCount();
			}
			
			// If the counts don't match, the difference has to be divided 
			// evenly amongst the bins.
			double totalCountDifference = matchCount - sumCount;
			if (totalCountDifference != 0) {
				int numBins = keys.size();
				double spreadDifference = totalCountDifference / numBins;
				// Now add this count to each bin.
				for (DataBin bin : currentMatches) {
					bin.setCount(bin.getCount()+ spreadDifference);
				}
			}
		}
		
		// Populate output bins (simple transfer of counts into corresponding bins)
//		...
		// FIXME - Preserve total counts here.
		for (DataBin intermediateBin : intermediateBins) {
			DataBin correspondingOutputBin = binMapping.getOutputBin(intermediateBin);
			double countToAdd = intermediateBin.getCount();
			double currentCount = correspondingOutputBin.getCount();
			double newCount = countToAdd + currentCount;
			correspondingOutputBin.setCount(newCount);
		}
		
		// Finally, create a Histogram from the output bins and return it.
		// FIXME
		// Type of the histogram to return and of the databins should depend
		// on the type of the histogram passed in.
		Histogram result = new Histogram(outputBins, distributionFunction);
		return result;
	}
	
	public Histogram rebinx(Histogram input, 
			double outputBinWidth, 
			Interval outputInterval) {
		
		Interval inputInterval = input.getDomain();
		
		if (!inputInterval.contains(outputInterval)) {
			// Output interval must be contained within the input interval,
			// otherwise data will not be available for populating output
			// bins falling outside of the input data domain.
			String errorMessage = "Output interval must be a subset of the input interval.";
			throw new IllegalArgumentException(errorMessage);
		}
		if ((outputInterval.getSize() % outputBinWidth) != 0) {
			String errorMessage = "Output interval size should be a multiple of the output bin width";
			throw new IllegalArgumentException(errorMessage);
		}
		
		DistributionFunction distributionFunction = input.getDistributionFunction();
		int numOutputBins = (int) (outputInterval.getSize() / outputBinWidth);
		List<DataBin> outputBins = new LinkedList<DataBin>();
		List<Double> outputDivisions = new LinkedList<Double>();
		for (int i = 0; i < numOutputBins; i++) {
			Interval currentInterval = new Interval(i * outputBinWidth, (i + 1) * outputBinWidth);
			DataBin currentBin = new DataBin(currentInterval, 0);
			outputBins.add(currentBin);
			outputDivisions.add(currentBin.getStart());
		}
		
		Histogram result = new Histogram(outputBins, distributionFunction);
		
		// 1. Merge bin boundaries
		
		List<DataBin> intermediateBins = new LinkedList<DataBin>();

		// FIXME 
		// This is an inefficient way of doing this and may lead to problems
		// with large histograms. Whether or not this is an issue will depend
		// on the size of the histograms we are working with.
		
		List<DataBin> inputBins = input.getBins();
		for (DataBin inputBin : inputBins) {
			// Find points which fall inside this bin and make divisions at these points,
			// otherwise just add a copy of the current interval.
			Interval currentInterval = inputBin.getInterval();
			List<Double> divisionList = new LinkedList<Double>(); 
			for (double point : outputDivisions) {
				if (currentInterval.contains(point)) {
					divisionList.add(point);
				}
			}
			
//			Math.
//			
//			// Now split the interval at every division.
//			List<Interval> splitIntervalList = new LinkedList<Interval>();
			
			// Now turn all these splits into bins and add them to the 
			// intermediate bin list.
		}
		
		BinCollection intermediateBinCollection 
		= new BinCollection(intermediateBins, distributionFunction);
		
		// 2. Calculate re-sampled counts
		
		// 3. Create output bins
		
		// 4. Transfer counts to output bins.
		
		return result;
	}
	
	// FIXME
	// Create an overloaded method to enable re-binning over a 
	// subset of the domain.
	
	public int [] rebin(int [] input, 
			DistributionFunction distributionFunction, 
			Interval inputInterval,
			Interval outputInterval,
			double inputBinWidth,
			double outputBinWidth) {

		if (!inputInterval.contains(outputInterval)) {
			String errorMessage = "Output interval must be a subset of the input interval.";
			throw new IllegalArgumentException(errorMessage);
		}
		
		int numInputBins = input.length;
		// Sanity check for input:
		double inputIntervalStart = inputInterval.getStart();
		double inputIntervalEnd = inputInterval.getEnd();
		double outputIntervalStart = outputInterval.getStart();
		double outputIntervalEnd = outputInterval.getEnd();
		
		double inputIntervalSize = inputInterval.getSize();
		double outputIntervalSize = outputInterval.getSize();
		int numOutputBins = (int) (outputIntervalSize / outputBinWidth);
		
		if (inputIntervalStart + (numInputBins * inputBinWidth) < inputIntervalEnd
				|| inputIntervalStart + (numInputBins * inputBinWidth) > inputIntervalEnd) {
			
			String errorMessage 
				= "Require intervalStart + (numInputBins * inputBinWidth) = intervalEnd";
			throw new IllegalArgumentException(errorMessage);
		}
		
		if (outputIntervalStart + (numOutputBins * outputBinWidth) < outputIntervalEnd
				|| outputIntervalStart + (numOutputBins * outputBinWidth) > outputIntervalEnd) {
			
			String errorMessage 
				= "Require intervalStart + (numOutBins * outputBinWidth) = intervalEnd";
			throw new IllegalArgumentException(errorMessage);
		}

		// Create axis labels to enable merging of bin boundaries.
		double [] inputAxisLabels = new double[numInputBins + 1];
		inputAxisLabels[0] = inputIntervalStart;
		
		double currentLabel = inputIntervalStart + inputBinWidth;
		for (int i = 1; i < inputAxisLabels.length; i++) {
			inputAxisLabels[i] = currentLabel;
			currentLabel += inputBinWidth;
		}
		
		currentLabel = outputIntervalStart + outputBinWidth;
		double [] outputAxisLabels = new double[numOutputBins + 1];
		outputAxisLabels[0] = outputIntervalStart; 
		for (int i = 1; i < outputAxisLabels.length; i++) {
			outputAxisLabels[i] = currentLabel;
			currentLabel += outputBinWidth;
		}
		
		// To find the combined divisions, we need to take the input
		// divisions, and add any output divisions which do not line
		// up with input divisions. A dynamic list of divisions will be
		// built up.
		List<Double> combinedDivisions = new LinkedList<Double>();
		for (int i = 0; i < inputAxisLabels.length; i++) {
			combinedDivisions.add(inputAxisLabels[i]);
		}
		
		for (int i = 0; i < outputAxisLabels.length; i++) {
			double currentDivision = outputAxisLabels[i];
			if (!combinedDivisions.contains(currentDivision)) {
				combinedDivisions.add(currentDivision);
			}
		}
		Collections.sort(combinedDivisions);
		
//		for (Double division : combinedDivisions) {
//			System.out.println(division);
//		}
		
		// Need to determine which smaller intervals fall within which larger
		// intervals.
		// Iterate through the combined intervals. See where they fall in 
		// terms of the larger intervals. Compute proportions and multiply by
		// source count to populate output bin.
		Interval [] inputIntervals = new Interval[input.length];
		for (int i = 0; i < inputAxisLabels.length - 1; i++) {
			inputIntervals[i] = new Interval(inputAxisLabels[i], inputAxisLabels[i + 1]);
		}
		Interval [] combinedIntervals = new Interval[combinedDivisions.size() - 1];
		for (int i = 0; i < combinedIntervals.length - 1; i++) {
			combinedIntervals[i] = new Interval(combinedDivisions.get(i), combinedDivisions.get(i + 1));
		}
		Interval [] outputIntervals = new Interval[numOutputBins];
		for (int i = 0; i < outputAxisLabels.length - 1; i++ ) {
			outputIntervals[i] = new Interval(outputAxisLabels[i], outputAxisLabels[i + 1]);
		}
		
		// Data in interval i will match data in bin i.
		
		// FIXME
		// Here we are assuming that the source intervals are smaller than 
		// the destination intervals, which is the expected situation, and the only
		// one which makes sense but 
		// there is no requirement. The requirement for downsampling must be
		// written into the code.
		
		// Go through the combined intervals and re-sample count data into them.
		int [] intermediateCounts = new int[combinedDivisions.size() - 1];
		for (int i = 0; i < intermediateCounts.length; i++) {
			// Get the matching source count. This means: get current intermediate
			// interval. Find out which source interval it is contained in. Use 
			// that index to look up the source bin, and then extract the source count
			// from that bin.
			
			Interval intermediateInterval = combinedIntervals[i];
			Interval sourceInterval = null;
			for (int j = 0; j < inputIntervals.length; j++) {
				Interval testInterval = inputIntervals[j];
				if (testInterval.contains(intermediateInterval)) {
					sourceInterval = testInterval;
					break;
				}
			}
			
			// FIXME - It should not be possible for sourceInterval to be 
			// null at this point, but it would obviously be a problem if
			// it is.
			
			double proportion = distributionFunction.getProportion(sourceInterval, intermediateInterval);
			// FIXME
			// How do we handle rounding here?
//			int sourceCount = 
			
			// Calculate proportion of that source count which falls into
			// the current bin.
//			int resampledCount;
//			intermediateCounts[i] = resampledCount;
		}
		
		// Finally, use a similar mechanism to transfer re-sampled counts
		// to the correct output bins.
		
		
		
		int [] result = new int[numOutputBins];
		// Now sample input into these new bins using the combined divisions
		// and the distribution function.
		
		///////////////////////////////////////////////////
		///////////////////////////////////////////////////
		
		// Use DistributionFunction to apportion each division correctly
		// into the appropriate output division.
		Iterator<Double> iterator = combinedDivisions.iterator();
		double firstDivision = (Double) iterator.next();
		while (iterator.hasNext()) {
			// Create interval with current and previous divisions.
			double nextDivision = (Double) iterator.next();
			
			// If either of the divisions is an output division and not an 
			// input division, it means that the interval was split and
			// we need to distribute the input bin's contents.
			// If not, we just transfer the contents directly to the 
			// output bin.
			
//			if (outputAxisLabels.contains(firstDivision) || outputAxisLabels.contains(nextDivision)) {
//				
//			} else {
//				// We need to be able to determine which input and 
//				// output bins we are working with. Which bin is the 
//				// source and which is the destination.
//			}
			
			// Dividing an interval at its edges should just result 
			// in transferring the entire interval.
			firstDivision = nextDivision;
		}
		
		return result;
	}

	public static void main(String[] args) {
		
		ReductionManager reductionManager = new ReductionManager();
		
		DistributionFunction distributionFunction = new FlatFunction(1d);
		List<DataBin> bins = new LinkedList<DataBin>();
		Histogram input = new Histogram(bins, distributionFunction);
		
		double outputBinWidth = 2;
//		reductionManager.rebin(input, outputBinWidth);
	}
	
	
	
	
	
	
	public static void old_main(String[] args) {
		ReductionManager manager = new ReductionManager();
		// Input will have 20 bins.
		int [] input = new int[20];
		for (int i = 0; i < input.length; i++) {
			input[i] = 100;
		}
		
		manager.rebin(input, 
				new FlatFunction(1d), 
				new Interval(0, 10), 
				new Interval(2, 8), 
				0.5, 
				2);
	}
}
