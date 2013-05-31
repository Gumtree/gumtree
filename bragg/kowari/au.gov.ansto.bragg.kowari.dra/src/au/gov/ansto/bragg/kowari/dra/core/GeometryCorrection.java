/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package au.gov.ansto.bragg.kowari.dra.core;

import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.interfaces.ISliceIterator;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.util.AxisRecord;
import au.gov.ansto.bragg.kowari.dra.internal.InternalConstants;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 16/12/2008
 */
public class GeometryCorrection extends ConcreteProcessor {

//	private final static double CALCULATE_GAMMA_ON_cosSTTH_BOUND = 0.1;
	private final static double DEGREE_RAD_COEFFICIENT = 180 / Math.PI;
	private Plot geometry_inputPlot;
	private Plot geometry_outputPlot;
	private Boolean geometry_enable = true;
	private Boolean geometry_stop = false;
	private Double geometry_sampleToDetector;
	private Boolean keepTwoTheta = false;
	private double sampleToDetectorSquare;
	
	private IArray twoThetaAxisArray;
	private IArray relocateLeftIndexArray;
	private IArray relocateRightIndexArray;
	private IArray relocateLeftRateArray;
	private IArray relocateRightRateArray;
	private IArray relocateCounterArray;
	
		
	/**
	 * 
	 */
	public GeometryCorrection() {
		super();
		setReprocessable(false);
	}

	public Boolean process() throws Exception {
		if (!geometry_enable){
			geometry_outputPlot = geometry_inputPlot;
			setReprocessable(false);
			return geometry_stop;
		}

//		Array twoThetaAxisArray = null;
		getSampleToDetector();
		IArray newIntensityArray;
		IArray newVarianceArray;
		IArray inputArray = geometry_inputPlot.findSignalArray();
		IArray inputVarianceArray  = geometry_inputPlot.findVarianceArray();
		newIntensityArray = Factory.createArray(Double.TYPE, inputArray.getShape());
		newVarianceArray = Factory.createArray(Double.TYPE, inputArray.getShape());
		boolean isFixedStth = false;
		IArray twoThetaAxisSlice;
		IArray inputSlice;
		IArray varianceSlice;
		IArray newIntensitySlice;
		IArray newVarianceSlice;
		IArray twoThetaDataArray = null;
//		Array relocateLeftIndexArray = null;
//		Array relocateRightIndexArray = null;
//		Array relocateLeftRateArray = null;
//		Array relocateRightRateArray = null;
//		Array relocateCounterArray = null;

		List<Axis> axisList = geometry_inputPlot.getAxisList();
		if (axisList.size() < 2)
			throw new StructureTypeException("no enough axis information");
		AxisRecord xAxis = AxisRecord.createRecord(axisList.get(axisList.size() - 1), axisList.size() - 1, 
				inputArray.getShape());
		AxisRecord yAxis = AxisRecord.createRecord(axisList.get(axisList.size() - 2), axisList.size() - 2, 
				inputArray.getShape());
//		if (!xAxis.getUnits().matches("mm"))
//			throw new StructureTypeException("wrong axis information, units of horizontal dimension not in 'mm'");
		IArray stth = null;
		try{
			stth = geometry_inputPlot.findDataItem("stth").getData();
		}catch (Exception e) {
			try{
				stth = geometry_inputPlot.findDataItem("old_stth").getData();
			}catch (Exception e1) {
				throw new SignalNotAvailableException("can not find stth from the nexus data");
			}
		}
		if (Math.abs(stth.getArrayMath().getMaximum() - stth.getArrayMath().getMinimum()) < 1e-3)
			isFixedStth = true;
		ISliceIterator inputSliceIterator = inputArray.getSliceIterator(2);
		ISliceIterator inputVarianceSliceIterator = inputVarianceArray.getSliceIterator(2);
		ISliceIterator outputSliceIterator = newIntensityArray.getSliceIterator(2);
		ISliceIterator varianceSliceIterator = newVarianceArray.getSliceIterator(2);
		int idx = 0;
		IIndex oneDIndex = stth.getIndex();
		while (inputSliceIterator.hasNext()) {
			inputSlice = inputSliceIterator.getArrayNext();
			varianceSlice = inputVarianceSliceIterator.getArrayNext();
			newIntensitySlice = outputSliceIterator.getArrayNext();
			newVarianceSlice = varianceSliceIterator.getArrayNext();
			
			oneDIndex.set(idx);
			double currentStth = stth.getDouble(oneDIndex) / DEGREE_RAD_COEFFICIENT;
			double sinStth = Math.sin(currentStth);
			double cosStth = Math.cos(currentStth);
//
			if (twoThetaDataArray == null){
				twoThetaDataArray = Factory.createArray(Double.TYPE, new int[]{(int) yAxis.centres().getSize(), (int) xAxis.bounds().getSize()});
				calculateTwoTheta(xAxis.bounds(), cosStth, sinStth, yAxis.centres(), twoThetaDataArray);		
			}else if (!isFixedStth)
				calculateTwoTheta(xAxis.bounds(), cosStth, sinStth, yAxis.centres(), twoThetaDataArray);
					
			if (keepTwoTheta && isFixedStth) {
				if (twoThetaAxisArray == null){
					twoThetaAxisArray = Factory.createArray(Double.TYPE, new int[]{
							(int) xAxis.bounds().getSize()});
					calculateTwoTheta(xAxis.bounds(), cosStth, sinStth, twoThetaAxisArray);
				}
//				twoThetaAxisSlice = twoThetaAxisArray;
				if (relocateLeftIndexArray == null){
					relocateLeftIndexArray = Factory.createArray(Integer.TYPE, inputSlice.getShape());
					relocateRightIndexArray = Factory.createArray(Integer.TYPE, inputSlice.getShape());
					relocateLeftRateArray = Factory.createArray(Double.TYPE, inputSlice.getShape());
					relocateRightRateArray = Factory.createArray(Double.TYPE, inputSlice.getShape());
					relocateCounterArray = Factory.createArray(Double.TYPE, newIntensitySlice.getShape());
					rebinWithTwoTheta(inputSlice, varianceSlice, newIntensitySlice, newVarianceSlice, 
							twoThetaDataArray, twoThetaAxisArray, relocateLeftIndexArray, relocateRightIndexArray, 
							relocateLeftRateArray, relocateRightRateArray, relocateCounterArray, true);
				}else
					rebinWithTwoTheta(inputSlice, varianceSlice, newIntensitySlice, newVarianceSlice, 
							twoThetaDataArray, twoThetaAxisArray, relocateLeftIndexArray, relocateRightIndexArray, 
							relocateLeftRateArray, relocateRightRateArray, relocateCounterArray, false);
			}else{
				if (twoThetaAxisArray == null){
					twoThetaAxisArray = Factory.createArray(Double.TYPE, new int[]{(int) stth.getSize(), 
							(int) xAxis.bounds().getSize()});
				}
				twoThetaAxisSlice = twoThetaAxisArray.getArrayUtils().slice(0, idx).getArray();
				calculateTwoTheta(xAxis.bounds(), cosStth, sinStth, twoThetaAxisSlice);
				rebinWithTwoTheta(inputSlice, varianceSlice, newIntensitySlice, newVarianceSlice, 
						twoThetaDataArray, twoThetaAxisSlice);
				twoThetaAxisArray = null;
			}

			idx ++;
		}
		
		geometry_outputPlot = (Plot) PlotFactory.createPlot(geometry_inputPlot, "geometry_correction", geometry_inputPlot.getDimensionType());
		geometry_outputPlot.addData("geometry_correction_data", newIntensityArray, "Geometry Correction", 
				"counts", newVarianceArray);
//		geometry_outputPlot.addCalculationData("relocateLeftIndex", relocateLeftIndexArray, "Left Index", "", null);
//		geometry_outputPlot.addCalculationData("relocateRightIndex", relocateRightIndexArray, "Right Index", "", null);
//		geometry_outputPlot.addCalculationData("relocateLeftRate", relocateLeftRateArray, "Left Rate", "", null);
//		geometry_outputPlot.addCalculationData("relocateRightRate", relocateRightRateArray, "Right Rate", "", null);
//		Array totalRate = relocateLeftRateArray.toAdd(relocateRightRateArray);
//		geometry_outputPlot.addCalculationData("totalRate", totalRate, "Total Rate", "", null);
		geometry_outputPlot.addProcessingLog("geometry curve correction");
//		int axisIndex = 0;
//		for (axisIndex = 0; axisIndex < axisList.size() - 2; axisIndex ++)
//			geometry_outputPlot.addAxis(axisList.get(axisIndex), axisIndex);
//		geometry_outputPlot.addAxis("gamma", yAxis, "Gamma", "degrees", axisIndex ++);
//		geometry_outputPlot.addAxis(axisList.get(axisList.size() - 2));
//		geometry_outputPlot.addAxis("two_theta", twoThetaAxisArray, "Two Theta", "degrees", axisIndex ++);
//		geometry_outputPlot.addCalculationData("two_theta", twoThetaAxisArray, "Two Theta", "units", null);
		geometry_outputPlot.copyAxes(axisList);
		geometry_inputPlot.getGroupList().clear();
//		long memorySize = geometry_inputPlot.calculateMemorySize();
//		if (memorySize > Register.REPROCESSABLE_THRESHOLD){
////			geometry_inputPlot.findSingal().setCachedData(newIntensityArray, false);
//			geometry_inputPlot.clearData();
//			setReprocessable(false);
//		}else
//			setReprocessable(true);
		return geometry_stop;

	}
//	/* (non-Javadoc)
//	 * @see au.gov.ansto.bragg.process.processor.ConcreteProcessor#process()
//	 */
//	public Boolean oldProcess() throws Exception {
//		if (!geometry_enable){
//			geometry_outputPlot = geometry_inputPlot;
//			return geometry_stop;
//		}
////		getSampleToDetector();
//		Array twoThetaAxisArray = null;
//		Array newIntensityArray;
//		Array newVarianceArray;
//		Array inputArray = geometry_inputPlot.findSignalArray();
//		Array inputVarianceArray  = geometry_inputPlot.findVarianceArray();
//		newIntensityArray = Factory.createArray(Double.TYPE, inputArray.getShape());
//		newVarianceArray = Factory.createArray(Double.TYPE, inputArray.getShape());
//		boolean needGamma = false;
//		boolean isFixedStth = false;
//		Array gammaAxisSlice = null;
//		Array twoThetaAxisSlice;
//		Array inputSlice;
//		Array varianceSlice;
//		Array newIntensitySlice;
//		Array newVarianceSlice;
//		Array twoThetaDataArray = null;
//		Array gammaDataArray = null;
//		
//		List<Axis> axisList = geometry_inputPlot.getAxisList();
//		if (axisList.size() < 2)
//			throw new StructureTypeException("no enough axis information");
//		AxisRecord xAxis = AxisRecord.createRecord(axisList.get(axisList.size() - 1), axisList.size() - 1, 
//				inputArray.getShape());
//		AxisRecord yAxis = AxisRecord.createRecord(axisList.get(axisList.size() - 2), axisList.size() - 2, 
//				inputArray.getShape());
//		if (!xAxis.getUnits().matches("mm") || !yAxis.getUnits().matches("mm"))
//			throw new StructureTypeException("wrong axis information, not physical dimension in mm");
//		stth = geometry_inputPlot.getDataItem("stth").getData();
//		if (stth.getMaximum() == stth.getMinimum())
//			isFixedStth = true;
////		if (isFixedStth)
////			twoThetaAxisArray = Factory.createArray(Double.TYPE, new int[]{(int) xAxis.bounds().getSize()});
////		else
////			twoThetaAxisArray = Factory.createArray(Double.TYPE, new int[]{(int) stth.getSize(), 
////					(int) xAxis.bounds().getSize()});
////		gammaArray = Factory.createArray(Double.TYPE, new int[]{(int) stth.getSize(), 
////				(int) yAxis.bounds().getSize()});
//		SliceIterator inputSliceIterator = inputArray.getSliceIterator(2);
//		SliceIterator inputVarianceSliceIterator = inputVarianceArray.getSliceIterator(2);
//		SliceIterator outputSliceIterator = newIntensityArray.getSliceIterator(2);
//		SliceIterator varianceSliceIterator = newVarianceArray.getSliceIterator(2);
//		int idx = 0;
//		Index oneDIndex = stth.getIndex();
//		while (inputSliceIterator.hasNext()) {
//			inputSlice = inputSliceIterator.getArrayNext();
//			varianceSlice = inputVarianceSliceIterator.getArrayNext();
//			newIntensitySlice = outputSliceIterator.getArrayNext();
//			newVarianceSlice = varianceSliceIterator.getArrayNext();
//			
//			oneDIndex.set(idx);
//			double currentStth = stth.getDouble(oneDIndex) / DEGREE_RAD_COEFFICIENT;
//			double sinStth = Math.sin(currentStth / DEGREE_RAD_COEFFICIENT);
//			double cosStth = Math.cos(currentStth / DEGREE_RAD_COEFFICIENT);
//			
//			if (Math.cos(currentStth) > CALCULATE_GAMMA_ON_cosSTTH_BOUND)
//				needGamma = true;
////			
//			if (isFixedStth) {
//				if (twoThetaAxisArray == null){
//					twoThetaAxisArray = Factory.createArray(Double.TYPE, new int[]{
//							(int) xAxis.bounds().getSize()});
//					calculateTwoTheta(xAxis.bounds(), cosStth, sinStth, 0, twoThetaAxisArray);
//				}
//				twoThetaAxisSlice = twoThetaAxisArray;
//				if (needGamma && gammaAxisSlice == null){
//					gammaAxisSlice = Factory.createArray(Double.TYPE, newIntensitySlice.getShape());		
//					calculateGamma(yAxis.bounds(), currentStth, 0, gammaAxisSlice);
//				}
//				else gammaAxisSlice = yAxis.bounds();
//			}
//			else{
//				if (twoThetaAxisArray == null){
//					twoThetaAxisArray = Factory.createArray(Double.TYPE, new int[]{(int) stth.getSize(), 
//							(int) xAxis.bounds().getSize()});
//				}
//				twoThetaAxisSlice = twoThetaAxisArray.slice(0, idx);
//				calculateTwoTheta(xAxis.bounds(), cosStth, sinStth, 0, twoThetaAxisSlice);
//				if (needGamma){
//					if (gammaAxisSlice == null)
//						gammaAxisSlice = Factory.createArray(Double.TYPE, newIntensitySlice.getShape());
//					calculateGamma(yAxis.bounds(), currentStth, 0, gammaAxisSlice);
//				}
//				else 
//					gammaAxisSlice = yAxis.bounds();
//			}
//
//			if (twoThetaDataArray == null){
//					twoThetaDataArray = Factory.createArray(Double.TYPE, newIntensitySlice.getShape());
//					calculateTwoTheta(xAxis.centres(), cosStth, sinStth, yAxis.centres(), twoThetaDataArray);		
//			}else if (!isFixedStth)
//				calculateTwoTheta(xAxis.centres(), cosStth, sinStth, yAxis.centres(), twoThetaDataArray);
//			
//			if (gammaDataArray == null){
//				if (needGamma){
//					gammaDataArray = Factory.createArray(Double.TYPE, newIntensitySlice.getShape());
//					calculateGamma(yAxis.centres(), currentStth, xAxis.centres(), gammaDataArray);
//				}
//			}else if (needGamma)
//				calculateGamma(yAxis.centres(), currentStth, xAxis.centres(), gammaDataArray);
//			
//			rebinWithTwoTheta(inputSlice, varianceSlice, newIntensitySlice, newVarianceSlice, 
//					twoThetaDataArray, gammaDataArray, twoThetaAxisSlice, gammaAxisSlice, true);
//			idx ++;
//		}
//		geometry_outputPlot = (Plot) PlotFactory.createPlot(geometry_inputPlot, "geometry_correction", DataDimensionType.mapset);
//		geometry_outputPlot.addData("geometry_correction_data", newIntensityArray, "Geometry Correction", 
//				"counts", newVarianceArray);
//		geometry_outputPlot.addProcessingLog("geometry curve correction");
////		int axisIndex = 0;
////		for (axisIndex = 0; axisIndex < axisList.size() - 2; axisIndex ++)
////			geometry_outputPlot.addAxis(axisList.get(axisIndex), axisIndex);
////		geometry_outputPlot.addAxis("gamma", gammaAxisSlice, "Gamma", "degrees", axisIndex ++);
////		geometry_outputPlot.addAxis("two_theta", twoThetaAxisArray, "Two Theta", "degrees", axisIndex ++);
//		geometry_outputPlot.copyAxes(axisList);
//		return geometry_stop;
//	}

	private void getSampleToDetector() {
		try{
//			String distanceValue = System.getProperty(
//					InternalConstants.SAMPLE_TO_DETECTOR_DISTANCE_NAME);
//			geometry_sampleToDetector = Double.valueOf(distanceValue);
			geometry_sampleToDetector = CalculateTTh.getSDD(geometry_inputPlot);
			sampleToDetectorSquare = geometry_sampleToDetector * geometry_sampleToDetector;
		}catch (Exception e) {
			LoggerFactory.getLogger(getClass()).info("can not read kowari property: " 
					+ InternalConstants.SAMPLE_TO_DETECTOR_DISTANCE_NAME);
		}
	}

	private void rebinWithTwoTheta(IArray inputIntensity, IArray inputVariance, IArray newIntensity, IArray newVariance,
			IArray twoThetaData, IArray twoThetaAxis) 
	throws ShapeNotMatchException, InvalidRangeException {
		IArrayIterator inputIterator = inputIntensity.getIterator();
		IArrayIterator twoThetaIterator = twoThetaData.getIterator();
		IArrayIterator varianceIterator = inputVariance.getIterator();
		IArray counterArray = Factory.createArray(Double.TYPE, newIntensity.getShape());
//		if (inputIntensity.getSize() != twoThetaData.getSize())
//			throw new ShapeNotMatchException("the data and two theta can not match");
		IIndex intensityIndex = newIntensity.getIndex();
		IIndex varianceIndex = newVariance.getIndex();
		IIndex counterIndex = counterArray.getIndex();
		IIndex twoThetaAxisIndex = twoThetaAxis.getIndex();
		int oneDIndex = -1;
		int yAxisIndex = 0;
		int xAxisSize = inputIntensity.getShape()[1];
		double twoThetaAxisFirstValue = twoThetaAxis.getDouble(twoThetaAxisIndex.set(0));
		double twoThetaAxisLastValue = twoThetaAxis.getDouble(twoThetaAxisIndex.set((int) twoThetaAxisIndex.getSize() - 1));
		boolean isAscending = twoThetaAxisFirstValue <= twoThetaAxisLastValue;
//		if (twoThetaIterator.hasNext())
//			twoThetaIterator.next();
		double leftIntensityRate;
		double rightIntensityRate;
		double twoThetaLeft = 0.0;
		double twoThetaRight = 0.0;
		if (twoThetaIterator.hasNext())
			twoThetaLeft = twoThetaIterator.getDoubleNext();
		while (inputIterator.hasNext() && twoThetaIterator.hasNext()){
			twoThetaRight = twoThetaIterator.getDoubleNext();
			oneDIndex ++; 
			if (oneDIndex == xAxisSize){
				yAxisIndex ++;
				oneDIndex = -1;
				continue;
			}
			int twoThetaIndexLeft = findIndex(twoThetaLeft, twoThetaAxis, oneDIndex, twoThetaAxisFirstValue, 
					twoThetaAxisLastValue, isAscending);
			int twoThetaIndexRight = findIndex(twoThetaRight, twoThetaAxis, oneDIndex, twoThetaAxisFirstValue, 
					twoThetaAxisLastValue, isAscending);
			if (twoThetaIndexLeft > twoThetaIndexRight || twoThetaIndexLeft < 0 && twoThetaIndexRight < 0){
//				continue;
				leftIntensityRate = 0;
				rightIntensityRate = 0;
			}else if (twoThetaIndexLeft < 0 && twoThetaIndexRight >= 0){
				leftIntensityRate = 0;
				rightIntensityRate = (twoThetaRight - twoThetaAxisFirstValue) 
					/ (twoThetaRight - twoThetaLeft);
				if (rightIntensityRate > 1) {
					rightIntensityRate = 1;
				} else if (rightIntensityRate < 0) {
					rightIntensityRate = 0;
				}
			}else if (twoThetaIndexLeft >= 0 && twoThetaIndexRight < 0){
				rightIntensityRate = 0;
				leftIntensityRate = (twoThetaAxisLastValue - twoThetaLeft) 
					/ (twoThetaRight - twoThetaLeft);
				if (leftIntensityRate > 1) {
					leftIntensityRate = 1;
				} else if (leftIntensityRate < 0) {
					leftIntensityRate = 0;
				}
			}else{
				double twoThetaAxisValue = twoThetaAxis.getDouble(twoThetaAxisIndex.set(twoThetaIndexRight));
				leftIntensityRate = (twoThetaAxisValue - twoThetaLeft) 
					/ (twoThetaRight - twoThetaLeft);
				rightIntensityRate = 1 - leftIntensityRate;
				if (leftIntensityRate > 1) {
					leftIntensityRate = 1;
				} else if (leftIntensityRate < 0) {
					leftIntensityRate = 0;
				}
			}
			double inputDataValue = inputIterator.getDoubleNext();
			double inputVarianceValue = varianceIterator.getDoubleNext();
			if (leftIntensityRate != 0){
				intensityIndex.set(yAxisIndex, twoThetaIndexLeft);
				varianceIndex.set(yAxisIndex, twoThetaIndexLeft);
				counterIndex.set(yAxisIndex, twoThetaIndexLeft);
				newIntensity.setDouble(intensityIndex, newIntensity.getDouble(intensityIndex) 
						+ inputDataValue * leftIntensityRate);
				newVariance.setDouble(varianceIndex, newVariance.getDouble(varianceIndex) 
						+ inputVarianceValue * leftIntensityRate);
				counterArray.setDouble(counterIndex, counterArray.getDouble(counterIndex) 
						+ leftIntensityRate);
			}
			if (rightIntensityRate != 0){
				intensityIndex.set(yAxisIndex, twoThetaIndexRight);
				varianceIndex.set(yAxisIndex, twoThetaIndexRight);
				counterIndex.set(yAxisIndex, twoThetaIndexRight);
				newIntensity.setDouble(intensityIndex, newIntensity.getDouble(intensityIndex) 
						+ inputDataValue * rightIntensityRate);
				newVariance.setDouble(varianceIndex, newVariance.getDouble(varianceIndex) 
						+ inputVarianceValue * rightIntensityRate);
				counterArray.setDouble(counterIndex, counterArray.getDouble(counterIndex) 
						+ rightIntensityRate);
			}
			twoThetaLeft = twoThetaRight;
		}

		IArrayIterator intensityIterator = newIntensity.getIterator();
		IArrayIterator counterIterator = counterArray.getIterator();
		IArrayIterator newVarianceIterator = newVariance.getIterator();
		while (intensityIterator.hasNext() && counterIterator.hasNext()){
			double counter = counterIterator.getDoubleNext();
			if (counter != 0){
				intensityIterator.setDoubleCurrent(intensityIterator.getDoubleNext() / counter);
				newVarianceIterator.setDoubleCurrent(newVarianceIterator.getDoubleNext() / (counter * counter));
			}else{
				intensityIterator.next();
				newVarianceIterator.next();
			}
		}
	}
	
	private void rebinWithTwoTheta(IArray inputIntensity, IArray inputVariance, IArray newIntensity, IArray newVariance,
			IArray twoThetaData, IArray twoThetaAxis, IArray relocateLeftIndexArray, IArray relocateRightIndexArray, 
			IArray relocateLeftRateArray, IArray relocateRightRateArray, IArray relocateCounterArray, boolean init) 
	throws ShapeNotMatchException, InvalidRangeException {
		IArrayIterator inputIterator = inputIntensity.getIterator();
		IArrayIterator twoThetaIterator = twoThetaData.getIterator();
		IArrayIterator varianceIterator = inputVariance.getIterator();
		IIndex intensityIndex = newIntensity.getIndex();
		IIndex varianceIndex = newVariance.getIndex();
		int oneDIndex = -1;
		int yAxisIndex = 0;
		int xAxisSize = inputIntensity.getShape()[1];
		if (init){
//			counterArray = Factory.createArray(Integer.TYPE, newIntensity.getShape());
//			relocateIndexArray = Factory.createArray(Integer.TYPE, newIntensity.getShape());
//			if (inputIntensity.getSize() != twoThetaData.getSize() - 1)
//				throw new ShapeNotMatchException("the data and two theta can not match");
			IIndex counterIndex = relocateCounterArray.getIndex();
			IIndex relocateLeftIndex = relocateLeftIndexArray.getIndex();
			IIndex relocateRightIndex = relocateRightIndexArray.getIndex();
			IIndex relocateLeftRateIndex = relocateLeftRateArray.getIndex();
			IIndex relocateRightRateIndex = relocateRightRateArray.getIndex();
			IIndex twoThetaAxisIndex = twoThetaAxis.getIndex();
			double twoThetaAxisFirstValue = twoThetaAxis.getDouble(twoThetaAxisIndex.set(0));
			double twoThetaAxisLastValue = twoThetaAxis.getDouble(twoThetaAxisIndex.set((int) twoThetaAxisIndex.getSize() - 1));
			boolean isAscending = twoThetaAxisFirstValue <= twoThetaAxisLastValue;
			double leftIntensityRate;
			double rightIntensityRate;
			double twoThetaLeft = 0.0;
			double twoThetaRight = 0.0;
			if (twoThetaIterator.hasNext())
				twoThetaLeft = twoThetaIterator.getDoubleNext();
			while (inputIterator.hasNext() && twoThetaIterator.hasNext()){
				twoThetaRight = twoThetaIterator.getDoubleNext();
				oneDIndex ++; 
				if (oneDIndex == xAxisSize){
					yAxisIndex ++;
					oneDIndex = -1;
					continue;
				}
				int twoThetaIndexLeft = findIndex(twoThetaLeft, twoThetaAxis, oneDIndex, twoThetaAxisFirstValue, 
						twoThetaAxisLastValue, isAscending);
				int twoThetaIndexRight = findIndex(twoThetaRight, twoThetaAxis, oneDIndex, twoThetaAxisFirstValue, 
						twoThetaAxisLastValue, isAscending);
				if (twoThetaIndexLeft > twoThetaIndexRight || (twoThetaIndexLeft < 0 && twoThetaIndexRight < 0)){
//					continue;
					leftIntensityRate = 0;
					rightIntensityRate = 0;
				}else if (twoThetaIndexLeft < 0 && twoThetaIndexRight >= 0){
					leftIntensityRate = 0;
					rightIntensityRate = (twoThetaRight - twoThetaAxisFirstValue) 
						/ (twoThetaRight - twoThetaLeft);
					if (rightIntensityRate > 1) {
						rightIntensityRate = 1;
					} else if (rightIntensityRate < 0) {
						rightIntensityRate = 0;
					}
				}else if (twoThetaIndexLeft >= 0 && twoThetaIndexRight < 0){
					rightIntensityRate = 0;
					leftIntensityRate = (twoThetaAxisLastValue - twoThetaLeft) 
						/ (twoThetaRight - twoThetaLeft);
					if (leftIntensityRate > 1) {
						leftIntensityRate = 1;
					} else if (leftIntensityRate < 0) {
						leftIntensityRate = 0;
					}
				}else{
					double twoThetaAxisValue = twoThetaAxis.getDouble(twoThetaAxisIndex.set(twoThetaIndexRight));
					leftIntensityRate = (twoThetaAxisValue - twoThetaLeft) 
						/ (twoThetaRight - twoThetaLeft);
					if (leftIntensityRate > 1) {
						leftIntensityRate = 1;
					} else if (leftIntensityRate < 0) {
						leftIntensityRate = 0;
					}
					rightIntensityRate = 1 - leftIntensityRate;
				}
				double inputDataValue = inputIterator.getDoubleNext();
				double inputVarianceValue = varianceIterator.getDoubleNext();
				relocateLeftIndex.set(yAxisIndex, oneDIndex);
				relocateLeftIndexArray.setInt(relocateLeftIndex, twoThetaIndexLeft);
				if (leftIntensityRate != 0){
					intensityIndex.set(yAxisIndex, twoThetaIndexLeft);
					varianceIndex.set(yAxisIndex, twoThetaIndexLeft);
					relocateLeftRateIndex.set(yAxisIndex, oneDIndex);
					counterIndex.set(yAxisIndex, twoThetaIndexLeft);
					newIntensity.setDouble(intensityIndex, newIntensity.getDouble(intensityIndex) 
							+ inputDataValue * leftIntensityRate);
					newVariance.setDouble(varianceIndex, newVariance.getDouble(varianceIndex) 
							+ inputVarianceValue * leftIntensityRate);
					relocateLeftRateArray.setDouble(relocateLeftRateIndex, leftIntensityRate);
					relocateCounterArray.setDouble(counterIndex, relocateCounterArray.getDouble(counterIndex) 
							+ leftIntensityRate);
				}
				relocateRightIndex.set(yAxisIndex, oneDIndex);
				relocateRightIndexArray.setInt(relocateRightIndex, twoThetaIndexRight);
				if (rightIntensityRate != 0){
					intensityIndex.set(yAxisIndex, twoThetaIndexRight);
					varianceIndex.set(yAxisIndex, twoThetaIndexRight);
					relocateRightRateIndex.set(yAxisIndex, oneDIndex);
					counterIndex.set(yAxisIndex, twoThetaIndexRight);
					newIntensity.setDouble(intensityIndex, newIntensity.getDouble(intensityIndex) 
							+ inputDataValue * rightIntensityRate);
					newVariance.setDouble(varianceIndex, newVariance.getDouble(varianceIndex) 
							+ inputVarianceValue * rightIntensityRate);
					relocateRightRateArray.setDouble(relocateRightRateIndex, rightIntensityRate);
					relocateCounterArray.setDouble(counterIndex, relocateCounterArray.getDouble(counterIndex) 
							+ rightIntensityRate);
				}
				twoThetaLeft = twoThetaRight;
			}

			IArrayIterator intensityIterator = newIntensity.getIterator();
			IArrayIterator counterIterator = relocateCounterArray.getIterator();
			IArrayIterator newVarianceIterator = newVariance.getIterator();
			while (intensityIterator.hasNext() && counterIterator.hasNext()){
				double counter = counterIterator.getDoubleNext();
				if (counter != 0){
					intensityIterator.setDoubleCurrent(intensityIterator.getDoubleNext() / counter);
					newVarianceIterator.setDoubleCurrent(newVarianceIterator.getDoubleNext() / (counter * counter));
				}else{
					intensityIterator.next();
					newVarianceIterator.next();
				}
			}
		}else{
			IArrayIterator relocateLeftIndexIterator = relocateLeftIndexArray.getIterator();
			IArrayIterator relocateRightIndexIterator = relocateRightIndexArray.getIterator();
			IArrayIterator relocateLeftRateIterator = relocateLeftRateArray.getIterator();
			IArrayIterator relocateRightRateIterator = relocateRightRateArray.getIterator();
			IIndex counterIndex = relocateCounterArray.getIndex();
			oneDIndex = 0;
			while (inputIterator.hasNext() && relocateLeftIndexIterator.hasNext()){
				double inputDataValue = inputIterator.getDoubleNext();
				double inputVarianceValue = varianceIterator.getDoubleNext();
				int twoThetaLeftIndex = relocateLeftIndexIterator.getIntNext();
				int twoThetaRightIndex = relocateRightIndexIterator.getIntNext();
				double relocateLeftRate = relocateLeftRateIterator.getDoubleNext();
				double relocateRightRate = relocateRightRateIterator.getDoubleNext();
				if (relocateLeftRate != 0){
					intensityIndex.set(yAxisIndex, twoThetaLeftIndex);
					varianceIndex.set(yAxisIndex, twoThetaLeftIndex);
					counterIndex.set(yAxisIndex, twoThetaLeftIndex);
					double counter = relocateCounterArray.getDouble(counterIndex);
					newIntensity.setDouble(intensityIndex, newIntensity.getDouble(intensityIndex) + 
							inputDataValue * relocateLeftRate / counter);
					newVariance.setDouble(varianceIndex, newVariance.getDouble(varianceIndex) + 
							inputVarianceValue * relocateLeftRate / (counter * counter));
				}
				if (relocateRightRate != 0){
					intensityIndex.set(yAxisIndex, twoThetaRightIndex);
					varianceIndex.set(yAxisIndex, twoThetaRightIndex);
					counterIndex.set(yAxisIndex, twoThetaRightIndex);
					double counter = relocateCounterArray.getDouble(counterIndex);
					newIntensity.setDouble(intensityIndex, newIntensity.getDouble(intensityIndex) + 
							inputDataValue * relocateRightRate / counter);
					newVariance.setDouble(varianceIndex, newVariance.getDouble(varianceIndex) + 
							inputVarianceValue * relocateRightRate / (counter * counter));
				}
				oneDIndex ++; 
				if (oneDIndex == xAxisSize){
					yAxisIndex ++;
					oneDIndex = 0;
				}
			}
		}
	}
	
//	private void rebinWithTwoTheta(Array inputIntensity, Array inputVariance, Array newIntensity, Array newVariance,
//			Array twoThetaData, Array gammaData, Array twoThetaAxis, 
//			Array gammaAxis) throws ShapeNotMatchException, InvalidRangeException {
//		ArrayIterator inputIterator = inputIntensity.getIterator();
//		ArrayIterator twoThetaIterator = twoThetaData.getIterator();
//		ArrayIterator varianceIterator = inputVariance.getIterator();
//		Array counterArray = Factory.createArray(Double.TYPE, newIntensity.getShape());
//		if (inputIntensity.getSize() != twoThetaData.getSize())
//			throw new ShapeNotMatchException("the data and two theta can not match");
//		Index intensityIndex = newIntensity.getIndex();
//		Index varianceIndex = newVariance.getIndex();
//		Index counterIndex = counterArray.getIndex();
//		if (gammaData != null){
//			ArrayIterator gammaIterator = gammaData.getIterator();
//			while (inputIterator.hasNext() && twoThetaIterator.hasNext() && gammaIterator.hasNext()){
//				double twoTheta = twoThetaIterator.getDoubleNext();
//				int twoThetaIndex = findIndex(twoTheta, twoThetaAxis);
//				if (twoThetaIndex < 0){
//					inputIterator.next();
//					varianceIterator.next();
//					continue;
//				}	
//				double gamma = gammaIterator.getDoubleNext();
//				int gammaIndex = findIndex(gamma, gammaAxis);
//				intensityIndex.set(gammaIndex, twoThetaIndex);
//				varianceIndex.set(gammaIndex, twoThetaIndex);
//				counterIndex.set(gammaIndex, twoThetaIndex);
//				newIntensity.setDouble(intensityIndex, newIntensity.getDouble(intensityIndex) + inputIterator.getDoubleNext());
//				newVariance.setDouble(varianceIndex, newVariance.getDouble(varianceIndex) + varianceIterator.getDoubleNext());
//				counterArray.setDouble(counterIndex, counterArray.getDouble(counterIndex) + 1);
//			}
//		}else {
//			int counter = 0;
//			int gammaAxisIndex = 0;
//			while (inputIterator.hasNext() && twoThetaIterator.hasNext()){
//				double twoTheta = twoThetaIterator.getDoubleNext();
//				int twoThetaIndex = findIndex(twoTheta, twoThetaAxis);
//				if (twoThetaIndex < 0){
//					inputIterator.next();
//					varianceIterator.next();
//					continue;
//				}
//				intensityIndex.set(gammaAxisIndex, twoThetaIndex);
//				varianceIndex.set(gammaAxisIndex, twoThetaIndex);
//				counterIndex.set(gammaAxisIndex, twoThetaIndex);
//				newIntensity.setDouble(intensityIndex, newIntensity.getDouble(intensityIndex) + inputIterator.getDoubleNext());
//				newVariance.setDouble(varianceIndex, newVariance.getDouble(varianceIndex) + varianceIterator.getDoubleNext());
//				counterArray.setDouble(counterIndex, counterArray.getDouble(counterIndex) + 1);
//				counter ++;
//				if (counter == gammaAxis.getSize()){
//					gammaAxisIndex ++;
//					counter = 0;
//				}
//			}
//		}
//		
//		ArrayIterator intensityIterator = newIntensity.getIterator();
//		ArrayIterator counterIterator = counterArray.getIterator();
//		ArrayIterator newVarianceIterator = newVariance.getIterator();
//		while (intensityIterator.hasNext() && counterIterator.hasNext()){
//			int counter = counterIterator.getIntNext();
//			if (counter != 0){
//				intensityIterator.setDoubleCurrent(intensityIterator.getDoubleNext() / counter);
//				newVarianceIterator.setDoubleCurrent(newVarianceIterator.getDoubleNext() / counter / counter);
//			}else{
//				intensityIterator.next();
//				newVarianceIterator.next();
//			}
//		}
//		
//	}

	private int findIndex(double value, IArray axis, int currentIndex, double axisFirstValue,
			double axisLastValue, boolean isAscending) {
		IIndex axisIndex = axis.getIndex();
		int axisSize = (int) axis.getSize();
		if (isAscending){
			if (value < axisFirstValue || value > axisLastValue)
				return -1;
			if (value >= axis.getDouble(axisIndex.set(currentIndex)))
				while(++currentIndex < axisSize){
					if (value < axis.getDouble(axisIndex.set(currentIndex)))
						return currentIndex - 1;
			}else
				while(--currentIndex >= 0)
					if (value > axis.getDouble(axisIndex.set(currentIndex)))
						return currentIndex;
		}else{
			if (value > axisFirstValue || value < axisLastValue)
				return -1;
			if (value <= axis.getDouble(axisIndex.set(currentIndex))){
				while(++currentIndex < axisSize)
					if (value > axis.getDouble(axisIndex.set(currentIndex)))
						return currentIndex - 1;
			}else
				while(--currentIndex > 0)
					if (value < axis.getDouble(axisIndex.set(currentIndex)))
						return currentIndex;
		}
		return -1;
	}

	private int findIndex(double value, IArray axis) {
		int index = -1;
		IArrayIterator axisIterator = axis.getIterator();
		IIndex axisIndex = axis.getIndex();
		double axisFirstValue = axis.getDouble(axisIndex.set(0));
		double axisLastValue = axis.getDouble(axisIndex.set((int) axis.getSize() - 1));
		if (axisFirstValue < axisLastValue){
			if (value < axisFirstValue || value > axisLastValue)
				return -1;
			int counter = 0;
			while (axisIterator.hasNext()){
				if (value < axisIterator.getDoubleNext()){
					index = counter - 1;
					break;
				}
				counter ++;
			}
		}else{
			if (value > axisFirstValue || value < axisLastValue)
				return -1;
			int counter = 0;
			while (axisIterator.hasNext()){
				if (value > axisIterator.getDoubleNext()){
					index = counter - 1;
					break;
				}
				counter ++;
			}
		}
		return index;
	}

	private void calculateGamma(IArray yPositionArray, double stth,
			IArray xPositionArray, IArray result) throws ShapeNotMatchException, InvalidRangeException {
		int[] shape = result.getShape();
		if (shape.length != 2)
			throw new ShapeNotMatchException("expecting 2 dimensional array");
		IArrayIterator xPositionIterator = xPositionArray.getIterator();
		for (int i = 0; i < shape[1]; i ++){
			calculateGamma(yPositionArray, stth, xPositionIterator.getDoubleNext(), 
					result.getArrayUtils().slice(1, i).getArray());
		}
	}

	private void calculateTwoTheta(IArray xPositionArray, double cosStth, double sinStth,
			IArray yPositionArray, IArray result) throws ShapeNotMatchException, InvalidRangeException {
		IArrayIterator yPositionIterator = yPositionArray.getIterator();
		ISliceIterator resultSliceIterator = result.getSliceIterator(1);
		while (yPositionIterator.hasNext() && resultSliceIterator.hasNext()){
			calculateTwoTheta(xPositionArray, cosStth, sinStth, yPositionIterator.getDoubleNext(), 
					resultSliceIterator.getArrayNext());
		}
	}

	private void calculateGamma(IArray yPositionArray, double stth, double xPosition,
			IArray result) {
		double R = Math.tan(stth) * geometry_sampleToDetector;
		IArrayIterator inputIterator = yPositionArray.getIterator();
		IArrayIterator resultIterator = result.getIterator();
		while (inputIterator.hasNext() && resultIterator.hasNext()){
			resultIterator.next().setDoubleCurrent(Math.atan(inputIterator.getDoubleNext() / R));
		}
	}

	private void calculateTwoTheta(IArray xPositionArray, double cosStth, double sinStth,
			IArray result) {
		IArrayIterator inputIterator = xPositionArray.getIterator();
		IArrayIterator resultIterator = result.getIterator();
		while(inputIterator.hasNext() && resultIterator.hasNext()){
			resultIterator.next().setDoubleCurrent(calculateTwoTheata(inputIterator.getDoubleNext(), cosStth, sinStth));
		}
	}

	private void calculateTwoTheta(IArray xPositionArray, double cosStth, double sinStth, double height,
			IArray result) {
		IArrayIterator inputIterator = xPositionArray.getIterator();
		IArrayIterator resultIterator = result.getIterator();
		while(inputIterator.hasNext() && resultIterator.hasNext()){
			resultIterator.next().setDoubleCurrent(calculateTwoTheata(inputIterator.getDoubleNext(), height, cosStth, sinStth));
		}
	}

	private double calculateTwoTheata(double xPosition, double cosStth, double sinStth) {
		return Math.acos((geometry_sampleToDetector * cosStth - xPosition * sinStth) / Math.sqrt(
				sampleToDetectorSquare + xPosition * xPosition)) * DEGREE_RAD_COEFFICIENT;
	}
	
	private double calculateTwoTheata(double xPosition, double height, double cosStth, double sinStth) {
		return Math.acos((geometry_sampleToDetector * cosStth - xPosition * sinStth) / Math.sqrt(
				sampleToDetectorSquare + xPosition * xPosition + height * height)) * DEGREE_RAD_COEFFICIENT;
	}
	
	private double oldCalculateTwoTheata(double xPosition, double height, double stth) {
		double r;
		double twoTheta;
		if (height == 0){
			r = xPosition;
			twoTheta = stth + Math.atan(r / geometry_sampleToDetector) * 180 / Math.PI;
		}
		else{
			double heightSquare = height * height;
			double xSquare = xPosition * xPosition;
//			r = (xPosition < 0 ? -1 : 1) * Math.sqrt(xSquare + heightSquare);
			r = xPosition;
			double arg = 180 / Math.PI;
			double directTwoTheata = stth / arg + Math.atan(r / geometry_sampleToDetector);
			double DX = geometry_sampleToDetector * geometry_sampleToDetector + xSquare;
//			twoTheta = stth + Math.sqrt(DX / (DX + heightSquare)) * directTwoTheata;
			twoTheta = Math.acos(Math.sqrt(DX / (DX + heightSquare)) * Math.cos(directTwoTheata)) * arg;
//			System.out.println(twoTheta);
//			twoTheta = directTwoTheata;
		}
		return twoTheta;
	}

	/**
	 * @return the geometry_outputPlot
	 */
	public Plot getGeometry_outputPlot() {
		return geometry_outputPlot;
	}

	/**
	 * @param geometry_inputPlot the geometry_inputPlot to set
	 */
	public void setGeometry_inputPlot(Plot geometry_inputPlot) {
		this.geometry_inputPlot = geometry_inputPlot;
	}

	/**
	 * @param geometry_enable the geometry_enable to set
	 */
	public void setGeometry_enable(Boolean geometry_enable) {
		this.geometry_enable = geometry_enable;
	}

	/**
	 * @param geometry_stop the geometry_stop to set
	 */
	public void setGeometry_stop(Boolean geometry_stop) {
		this.geometry_stop = geometry_stop;
	}

	/**
	 * @param geometry_sampleToDetector the geometry_sampleToDetector to set
	 */
	public void setGeometry_sampleToDetector(Double geometry_sampleToDetector) {
		this.geometry_sampleToDetector = geometry_sampleToDetector;
		sampleToDetectorSquare = geometry_sampleToDetector * geometry_sampleToDetector;
	}

	public DataStructureType getDataStructureType() {
		return DataStructureType.plot;
	}
	public DataDimensionType getDataDimensionType() {
		return DataDimensionType.mapset;
	}

	/**
	 * @param keepTwoTheta the keepTwoTheta to set
	 */
	public void setKeepTwoTheta(Boolean keepTwoTheta) {
		this.keepTwoTheta = keepTwoTheta;
		if (!keepTwoTheta){
			twoThetaAxisArray = null;
			relocateLeftIndexArray = null;
			relocateRightIndexArray = null;
			relocateLeftRateArray = null;
			relocateRightRateArray = null;
			relocateCounterArray = null;
		}
	}
	
	
}
