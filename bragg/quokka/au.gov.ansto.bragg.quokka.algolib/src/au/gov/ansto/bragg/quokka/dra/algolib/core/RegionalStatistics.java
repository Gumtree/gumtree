/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong - initial API and implementation
*    Paul Hathaway - December 2008 refactor/clean
*    TO BE DEPRECATED 
*******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.algolib.core;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.impl.netcdf.NcArray;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IIndex;

import ucar.ma2.MAMath;

public class RegionalStatistics {

	IArray signal = null;
	IArray errorSquare = null;
	IArray centroid = null;
	IArray centroidError = null;
	double rms = 0.;
	double rmsError = 0.;
	IArray totalSum = null;
	IArray totalSumError = null;
	double maxCount = 0.;
	double minCount = 0.;
	double countDuration = 0.;
	IArray rmsWidth = null;
	IArray rmsWidthError = null;
	IArray totalSumFit = null;
	IArray totalSumFitPeak = null;

	SliceSum sliceSum = null;
	SliceSum errorSliceSum = null;
	boolean available = false;

	public RegionalStatistics(){
		super();
	}

	public RegionalStatistics(IArray signal, IArray error){
		this.signal = signal;
		this.errorSquare = findeErrorSquare(error);
	}

	public void findAll() throws InvalidArrayTypeException{
		findCentroid();
		findRMS();
		findMaxCount();
		findMinCount();
	}

	private IArray findeErrorSquare(IArray error) {
		int[] shape = error.getShape();
		int totalNumber = 1;
		for (int i = 0; i < shape.length; i++) {
			totalNumber *= shape[i];
		}
		double[] errorSquareStorage = new double[totalNumber];
		IIndex index = Factory.createIndex(new int[]{totalNumber});
//		Index index = error.getIndex();
		for (int i = 0; i < errorSquareStorage.length; i++) {
			index.set(i);
			double errorValue = error.getDouble(index);
			errorSquareStorage[i] = errorValue * errorValue;
		}
		IArray errorSquare = Factory.createArray(Double.class, error.getShape(), errorSquareStorage);
		return errorSquare;
	}

	public void process() throws InvalidArrayTypeException{
		findCentroid();
	}

	public IArray findCentroid() throws InvalidArrayTypeException{
		if (centroid != null) return centroid;
//		double[][] sampleData = ConverterLib.get2DDouble(sample);
//		double[] centerData = CenterFinder.getCenterOfMass(sampleData);
		int[] shape = signal.getShape();
		int frameNumber = 0;
		if (shape.length > 3) frameNumber = shape[0];
		else frameNumber = 1;
		double[] totalSumValue = new double[frameNumber];
		double[] totalSumErrorValue = new double[frameNumber];
		double[] centerValues = new double[shape.length];
		double[] centerErrorValues = new double[shape.length];
		double[] rmsWidthValues = new double[shape.length];
		double[] rmsWidthErrorValues = new double[shape.length];
		if (sliceSum == null) sliceSum = findSliceSum(signal);
		if (errorSliceSum == null) errorSliceSum = findSliceSum(errorSquare);
		double errorSquareSum = 0., weightSquareErrorSquareSum = 0.,
		weightErrorSqareSum = 0., weightSquareSum = 0., spaceTotalSum = 0.;
		double[] weightSum = new double[shape.length];

		/**
		 * The below codes find the total_sum and the centroid at the first dimension.
		 */
		for (int i = 0; i < shape[0]; i ++){
			double sliceSumI = sliceSum.getValue(0, i);
			double errorSquareSliceSumI = errorSliceSum.getValue(0, i);
			spaceTotalSum += sliceSumI;
			if (frameNumber > 1) {
				totalSumValue[i] = sliceSumI;
				totalSumErrorValue[i] = Math.sqrt(errorSquareSliceSumI);
			}
			errorSquareSum += errorSquareSliceSumI;
			weightErrorSqareSum += i * errorSquareSliceSumI;
			weightSquareErrorSquareSum += i * i * errorSquareSliceSumI;
			weightSum[0] += i * sliceSumI;
			weightSquareSum += i * i * sliceSumI;
		}
		if (frameNumber == 1) {
			totalSumValue[0] = spaceTotalSum;
			totalSumErrorValue[0] = Math.sqrt(errorSquareSum);
		}
		centerValues[0] = weightSum[0] / spaceTotalSum;
		double sliceSumSquare = spaceTotalSum * spaceTotalSum;
		double sliceSumQuar = sliceSumSquare * sliceSumSquare;
		centerErrorValues[0] = weightSum[0] * weightSum[0] * errorSquareSum / sliceSumQuar;
		centerErrorValues[0] += weightSquareErrorSquareSum / sliceSumSquare;
		centerErrorValues[0] -= 2 * weightSum[0] * weightErrorSqareSum / (sliceSumSquare * spaceTotalSum);
		centerErrorValues[0] = Math.sqrt(centerErrorValues[0]);
		rmsWidthValues[0] = weightSquareSum / spaceTotalSum - centerValues[0] * centerValues[0];
		rmsWidthValues[0] = Math.sqrt(rmsWidthValues[0]);

		/**
		 * The below codes find the centroid at the rest dimensions.
		 */
		for (int i = 1; i<shape.length; i ++){
			weightSum[i] = 0.;
			weightErrorSqareSum = 0.;
			weightSquareErrorSquareSum = 0.;
			weightSquareSum = 0.;
			for (int j = 0; j < shape[i]; j++) {
				double sliceSumI = sliceSum.getValue(i, j);
				double errorSliceSumI = errorSliceSum.getValue(i, j);
				weightErrorSqareSum += j * errorSliceSumI;
				weightSquareErrorSquareSum += j * j * errorSliceSumI;
				weightSum[i] += j * sliceSumI;
				weightSquareSum += j * j * sliceSumI;
			}
			centerValues[i] = weightSum[i] / spaceTotalSum;
			centerErrorValues[i] = weightSum[i] * weightSum[i] * errorSquareSum / sliceSumQuar;
			centerErrorValues[i] += weightSquareErrorSquareSum / sliceSumSquare;
			centerErrorValues[i] -= 2 * weightSum[i] * weightErrorSqareSum / (sliceSumSquare * spaceTotalSum);
			centerErrorValues[i] = Math.sqrt(centerErrorValues[i]);
			rmsWidthValues[i] = weightSquareSum / spaceTotalSum - centerValues[i] * centerValues[i];
			rmsWidthValues[i] = Math.sqrt(rmsWidthValues[i]);
		}

		centroid = Factory.createArray(Double.class, new int[]{centerValues.length}, 
				centerValues);

		centroidError = Factory.createArray(Double.class, new int[]{centerErrorValues.length}, 
				centerErrorValues);

		totalSum = Factory.createArray(Double.class, new int[]{frameNumber}, 
				totalSumValue);
		totalSumError = Factory.createArray(Double.class, new int[]{frameNumber}, 
				totalSumErrorValue);
//		totalSumFit(totalSumValue);
		rmsWidth = Factory.createArray(Double.class, new int[]{rmsWidthValues.length}, 
				rmsWidthValues);
		rmsWidthError = Factory.createArray(Double.class, new int[]{rmsWidthValues.length}, 
				rmsWidthValues);
		return centroid;
	}

	public IArray findRMSWidth() throws InvalidArrayTypeException{
		if (rmsWidth != null) return rmsWidth;
		findCentroid();
		return rmsWidth;
	}

	public IArray getRMSWidthError() throws InvalidArrayTypeException {
		if (rmsWidthError != null) return rmsWidthError;
		findCentroid();
		return rmsWidthError;		
	}

	public IArray getCentroidError() throws InvalidArrayTypeException{
		if (centroidError != null) return centroidError;
		else findCentroid();
		return centroidError;
	}

	public IArray findTotalSum() throws InvalidArrayTypeException{
		if (totalSum != null) return totalSum;
		int[] shape = signal.getShape();
		int frameNumber = 0;
		if (shape.length > 3) frameNumber = shape[0];
		else frameNumber = 1;
		double[] totalSumValue = new double[frameNumber];
		double[] totalSumErrorValue = new double[frameNumber];
		if (sliceSum == null) sliceSum = findSliceSum(signal);
		if (errorSliceSum == null) errorSliceSum = findSliceSum(errorSquare);
		totalSumErrorValue[0] = 0.;
		for (int i = 0; i < shape[0]; i++) {
			if (frameNumber > 1){
				totalSumValue[i] = sliceSum.getValue(0, i);
				totalSumErrorValue[i] = Math.sqrt(errorSliceSum.getValue(0, i));
			}else{
				totalSumValue[0] += sliceSum.getValue(0, i);
				totalSumErrorValue[0] += errorSliceSum.getValue(0, i);
			}
		}
		if (frameNumber == 1) totalSumErrorValue[0] = Math.sqrt(totalSumErrorValue[0]);
		totalSum = Factory.createArray(Double.class, new int[]{frameNumber}, 
				totalSumValue);
		totalSumError = Factory.createArray(Double.class, new int[]{frameNumber}, 
				totalSumErrorValue);
//		totalSumFit(totalSumValue);
//		int totalNumber = 1;
//		int[] shape = signal.getShape();
//		for (int i = 0; i < shape.length; i++) {
//		totalNumber *= shape[i];
//		}
//		Index index = Factory.createIndex(new int[]{totalNumber});
//		totalSum = ;
//		totalSumError = 0.;
//		for (int i = 0; i < totalNumber; i++){
//		index.set(i);
//		double value = signal.getDouble(index);
//		double errorValueSquare = errorSquare.getDouble(index);
//		totalSum += value;
//		totalSumError += errorValueSquare;
//		}
//		totalSumError = Math.sqrt(totalSumError);
		return totalSum;
	}

	public IArray getTotalSumError() throws InvalidArrayTypeException{
		if (totalSumError != null) return totalSumError;
		else findTotalSum();
		return totalSumError;
	}

	public double findRMS(){
		if (rms != 0.) return rms;
		int totalNumber = 1;
		int[] shape = signal.getShape();
		for (int i = 0; i < shape.length; i++) {
			totalNumber *= shape[i];
		}
		IIndex index = Factory.createIndex(new int[]{totalNumber});
//		Index index = signal.getIndex();
		double squareSum = 0., squareError = 0.;
		for (int i = 0; i < totalNumber; i++){
			index.set(i);
			double value = signal.getDouble(index);
			double errorValueSquare = errorSquare.getDouble(index);
			squareSum += value * value;
			squareError += value * value * errorValueSquare;
		}
		rms = Math.sqrt(squareSum / totalNumber);
		rmsError = Math.sqrt(squareError / (totalNumber * squareSum));
		return rms;
	}

	public double getRMSError(){
		if (rmsError != 0.) return rmsError;
		else findRMS();
		return rmsError;
	}
	public double findMaxCount() throws InvalidArrayTypeException{
		if (maxCount > 0.) return maxCount;
		maxCount = signal.getArrayMath().getMaximum();
		return maxCount;
	}

	public double findMinCount() throws InvalidArrayTypeException{
		if (minCount > 0.) return minCount;
		minCount = signal.getArrayMath().getMinimum();
		return minCount;
	}

	private SliceSum findSliceSum(IArray sample) throws InvalidArrayTypeException{
		int[] shape = sample.getShape();
		int dimensionSize = 0;
		for (int i = 0; i < shape.length; i++) {
			dimensionSize += shape[i];
		}
		double[] storage = new double[dimensionSize];
		int dimension = 0; 
		int itemNumber = 0;
		int location = 0;
		while(dimension < shape.length){
			storage[location] = sliceSum(sample, dimension, itemNumber);
			itemNumber ++;
			location ++;
			if (itemNumber == shape[dimension]){
				dimension ++;
				itemNumber = 0;
			}
		}
		return new SliceSum(storage, shape);
	}


	private static double sliceSum(IArray array, int dimension, int itemNumber) throws InvalidArrayTypeException{
		int[] shape = array.getShape();
		if (dimension >= shape.length) return 0.;
		IArray slice = array.getArrayUtils().slice(dimension, itemNumber).getArray();
		return sumDouble(slice);
	}

	protected class SliceSum{
		double[] storage = null;
		int[] shape = null;
		public SliceSum(double[] storage, int[] shape){
			this.storage = storage;
			this.shape = shape;
		}

		public void setValue(int dimension, int itemNumber, double value){
			int location = 0;
			for (int i = 0; i < dimension; i++) {
				location += shape[i];
			}
			location += itemNumber;
			storage[location] = value;
		}

		public double getValue(int dimension, int itemNumber){
			int location = 0;
			for (int i = 0; i < dimension; i++) {
				location += shape[i];
			}
			location += itemNumber;
			return storage[location];
		}

		public double getWeightSumValue(int dimension, int itemNumber){
			return getValue(dimension, itemNumber) * itemNumber;
		}

		public double getWeightSumSquare(int dimension, int itemNumber){
			double baseValue = getValue(dimension, itemNumber);
			return baseValue * baseValue * itemNumber;
		}

	}

	public IArray getTotalSumFit() {
		return totalSumFit;
	}

	public IArray getTotalSumFitPeak() {
		return totalSumFitPeak;
	}

	// [ANSTO][Tony][2011-02-15] Copied from ArrayMath v1.5.x
	public static double sumDouble(IArray array) throws InvalidArrayTypeException{
		if (array instanceof NcArray){
			return (MAMath.sumDouble(((NcArray) array).getArray()));
		}else
			throw new InvalidArrayTypeException("Can not do the sum calculation");
//		return 0.;
	}
	
}
