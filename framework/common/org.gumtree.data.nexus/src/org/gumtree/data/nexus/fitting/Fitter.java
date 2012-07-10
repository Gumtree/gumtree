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
package org.gumtree.data.nexus.fitting;

import hep.aida.IAnalysisFactory;
import hep.aida.IDataPointSet;
import hep.aida.IDataPointSetFactory;
import hep.aida.IFitData;
import hep.aida.IFitFactory;
import hep.aida.IFitResult;
import hep.aida.IFitter;
import hep.aida.IFunction;
import hep.aida.IHistogram;
import hep.aida.ITree;
import hep.aida.ITupleFactory;
import hep.aida.ref.histogram.Histogram1D;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.math.IArrayMath;
import org.gumtree.data.nexus.IAxis;
import org.gumtree.data.nexus.INXdata;
import org.gumtree.data.nexus.ISignal;
import org.gumtree.data.nexus.IVariance;
import org.gumtree.data.nexus.fitting.StaticField.EnginType;
import org.gumtree.data.nexus.fitting.StaticField.FitterType;
import org.gumtree.data.nexus.fitting.StaticField.FunctionType;
import org.gumtree.data.nexus.utils.NexusFactory;


//import com.primalworld.math.MathEvaluator;

/**
 * @author nxi
 * Created on 19/06/2008
 */
public abstract class Fitter {

	private static final String FITTER_PACKAGE_NAME = "org.gumtree.data.nexus.fitting";
	protected static IAnalysisFactory  analysisFactory = IAnalysisFactory.create();
	protected static ITree tree = analysisFactory.createTreeFactory().create();
	protected static IDataPointSetFactory dataPointSetFactory   = analysisFactory.createDataPointSetFactory(tree);
	protected static FitterType fitterType = FitterType.ChiSquared;//FitterType.ChiSquared;
	protected static EnginType enginType = EnginType.jminuit;
	protected static IFitFactory fitFactory = analysisFactory.createFitFactory();
//	protected static IFitter fitter = fitFactory.createFitter(fitterType.getValue(), enginType.name());
	protected IFitter fitter;
	protected static ITupleFactory tupleFactory = analysisFactory.createTupleFactory(tree);
	private static final NexusFactory nexusFactory = new NexusFactory();
	private static double CutRange = 0.2;
	private FunctionType functionType;
	private int dimension;
	private String description;
	private String functionText;
	private Map<String, Double> parameters = new HashMap<String, Double>();
	private Map<String, Double> fitErrors = new HashMap<String, Double>();
	protected IHistogram histogram;
	protected IFunction fitFunction;
	protected IFitResult fitResult;
	private String title;
	protected int resolutionMultiple = 3;
//	protected Array data;
//	protected Array axis;
	protected INXdata data;
//	protected Array resultAxis;
//	protected Array resultCurve;
	protected INXdata resultData;
	protected boolean inverse = false;
	protected double offset;
	
	protected IDataPointSet dataPointSet;
	protected IFitData fitData;
	protected boolean isInverseAllowed = false;
	protected double minXValue = Double.MIN_VALUE;
	protected double maxXValue = Double.MAX_VALUE;
	protected double minYValue = Double.POSITIVE_INFINITY;
	protected double maxYValue = Double.NEGATIVE_INFINITY;
	protected double peakX = Double.NaN;

	public static Fitter getFitter(String name, int dimension) throws FitterException{
		FunctionType functionType = null;
		try {
			functionType = FunctionType.valueOf(name);			
		} catch (Exception e) {
			throw new FitterException("can not get such fitter function type: " + name);
		}
		Fitter fitter = null;
		try{
			fitter = (Fitter) Class.forName(FITTER_PACKAGE_NAME + "." + functionType.getValue() 
					+ "Fitter").getConstructor(Integer.TYPE).newInstance(dimension);
		}catch (Exception e) {
			throw new FitterException("Can not create fitter : " + name);
		}
		return fitter;		
	}
	
	public static Fitter getFitter(String name, INXdata data) 
	throws FitterException, DimensionNotSupportedException, IOException, InvalidArrayTypeException{
		FunctionType functionType = null;
		try {
			functionType = FunctionType.valueOf(name);			
		} catch (Exception e) {
			throw new FitterException("can not get such fitter function type: " + name);
		}
		Fitter fitter = null;
		try{
			fitter = (Fitter) Class.forName(FITTER_PACKAGE_NAME + "." + functionType.getValue() 
					+ "Fitter").getConstructor(INXdata.class).newInstance(data);
		}catch (Exception e) {
			throw new FitterException("Can not create fitter : " + name);
		}
		return fitter;
//		switch (functionType) {
//		case Gaussian:
//			return new GaussianFitter(plot);
//		case Quadratic:
//			return new QuadraticFitter(plot);
//		case Linear:
//			return new LinearFitter(plot);
//		case Cubic:
//			return new CubicFitter(plot);
//		default:
//			break;
//		}
//		return null;
	}

	public void setDimension(int dimension) throws FitterException{
		if (dimension < 0)
			throw new FitterException("illegal dimension: " + dimension);
		this.dimension = dimension;
	}

	public int getDimension(){
		return dimension;
	}

	public void parse(String functionText){
		this.functionText = functionText;
	}

	protected void addParameter(String parameter){
		parameters.put(parameter, 0.);
		fitErrors.put(parameter, 0.);
	}

	protected void addParameter(String parameter, double value){
		parameters.put(parameter, value);
		fitErrors.put(parameter, 0.);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public FitterType getFitterType() {
		return fitterType;
	}

	public EnginType getEnginType() {
		return enginType;
	}

	public String getFunctionText() {
		return functionText;
	}

	public Map<String, Double> getParameters() {
		return parameters;
	}
	
	public Map<String, Double> getFitErrors() {
		return fitErrors;
	}
	
	public double getParameterValue(String name){
		return  parameters.get(name);
	}

	public double getFitError(String name){
		return  fitErrors.get(name);
	}

	public FunctionType getFunctionType() {
		return functionType;
	}

	public void setFunctionType(FunctionType functionType) {
		this.functionType = functionType;
	}

	protected void createFitFunction(){
		fitFunction = analysisFactory.createFunctionFactory(tree).createFunctionFromScript(
				getFunctionType().getValue(), dimension, getFunctionText(), 
				parameterNamesToString(), getFunctionType().getValue() + " " + 
				dimension + "D fitting");
	}
	
	public void createHistogram(INXdata data) throws IOException, FitterException{
		createHistogram(data, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
//		this.data = data;
//		int rank = data.getSignal().getRank();
//		if (rank != getDimension())
//			throw new FitterException("data dimension does not match with fit function");
////		setDimension(rank);
//		switch (rank) {
//		case 1:
//			IArray axis;
//			IArray array = data.getSignal().getData();
//			IArray variance = null;
//			IVariance var = data.getVariance();
//			if (var != null) {
//				variance = var.getData();
//			}
//			try {
//				List<IAxis> axisList = data.getAxisList();
//				axis = axisList.get(axisList.size() - 1).getData();
//				histogram = analysisFactory.createHistogramFactory(tree).createHistogram1D(
//						"data", "1D Data", (int) axis.getSize(), GMath.getMinimum(axis), 
//						GMath.getMaximum(axis));
//			} catch (Exception e) {
//				throw new FitterException("import axis failed");
//			} 
//			IHistogram1D histogram1D = (IHistogram1D) histogram;
//			IArrayIterator dataIterator = array.getIterator();
//			IArrayIterator axisIterator = axis.getIterator();
//			offset = getOffset(array);
//			if (variance != null){
//				IArrayIterator varianceIterator = variance.getIterator();
//				while (dataIterator.hasNext() && axisIterator.hasNext()) {
//					double axisValue = axisIterator.getDoubleNext();
//					double dataValue = dataIterator.getDoubleNext();
//					if (!Double.isNaN(axisValue) && !Double.isNaN(dataValue)){
//						if (inverse) dataValue = - dataValue;
//						histogram1D.fill(axisValue, dataValue + offset);
//						((Histogram1D) histogram1D).setBinError(histogram1D.coordToIndex(axisValue), 
//								Math.sqrt(varianceIterator.getDoubleNext()));
//					}
//				}
//			}else{
//				while (dataIterator.hasNext() && axisIterator.hasNext()) {
//					double axisValue = axisIterator.getDoubleNext();
//					double dataValue = dataIterator.getDoubleNext();
//					if (!Double.isNaN(axisValue) && !Double.isNaN(dataValue)){
//						if (inverse) dataValue = - dataValue;
//						histogram1D.fill(axisValue, dataValue + offset);
//					}
//				}
//			}
//			setParameters();
////			double[] err = new double[(int) axis.getSize()];
////			dataPointSet = analysisFactory.createDataPointSetFactory(tree).createXY("dataset",
////					(double[]) axis.getStorage(), (double[]) data.getStorage(), err, err);
//			dataPointSet = analysisFactory.createDataPointSetFactory(tree).create(
//					"dataset", "dataset", 2);
//			dataIterator = array.getIterator();
//			axisIterator = axis.getIterator();
//			while (dataIterator.hasNext() && axisIterator.hasNext()) {
//				Double axisValue = axisIterator.getDoubleNext();
//				Double dataValue = dataIterator.getDoubleNext();
//				if (!axisValue.isNaN() && !dataValue.isNaN()){
//					dataPointSet.addPoint(new DataPoint(new double[]{axisValue.doubleValue(), 
//							dataValue.doubleValue()}));
//				}
//			}
////			System.out.println(dataPointSet.size());
//			
////			fitData = new FitData();
//			fitData = fitFactory.createFitData();
//			fitData.create1DConnection(histogram1D);
//			break;
//		default:
//			throw new FitterException(rank + " dimension not supported");
//		}
	}

	public void createHistogram(INXdata data, double minX, double maxX) 
	throws IOException, FitterException{
		minXValue = minX;
		maxXValue = maxX;
		this.data = data;
		int rank = data.getSignal().getRank();
		if (rank != getDimension())
			throw new FitterException("data dimension does not match with fit function");
//		setDimension(rank);
		switch (rank) {
		case 1:
			List<IAxis> axisList = null;
			axisList = data.getAxisList();
			IArray axis = axisList.get(axisList.size() - 1).getData();
			IArray array = data.getSignal().getData();
			IArray variance = null;
			IVariance var = data.getVariance();
			if (var != null) {
				variance = var.getData();
			}
			boolean hasVariance = variance != null;
			IArrayIterator axisIterator = axis.getIterator();
			IArrayIterator dataIterator = array.getIterator();
			int axisSize = 0;
			double minAxisValue = axis.getArrayMath().getMinimum();
			if (minXValue < minAxisValue) {
				minXValue = minAxisValue;
			}
			double maxAxisValue = axis.getArrayMath().getMaximum();
			if (maxXValue > maxAxisValue) {
				maxXValue = maxAxisValue;
			}
			double realMin = Double.POSITIVE_INFINITY;
			double realMax = Double.NEGATIVE_INFINITY;
			while(axisIterator.hasNext() && dataIterator.hasNext()){
				double axisValue = axisIterator.getDoubleNext();
				double dataValue = dataIterator.getDoubleNext();
				if (!Double.isNaN(axisValue) && axisValue >= minXValue && axisValue <= maxXValue && !Double.isNaN(dataValue)){
					if (realMin > axisValue)
						realMin = axisValue;
					if (realMax < axisValue)
						realMax = axisValue;
					axisSize ++;
				}
			}
			try {
				histogram = analysisFactory.createHistogramFactory(tree).createHistogram1D(
						"data", "1D Data", axisSize, realMin, realMax);
				dataPointSet = dataPointSetFactory.create("dataPointSet","one dimensional IDataPointSet", 2);
			} catch (Exception e) {
				throw new FitterException("import axis failed");
			} 
			Histogram1D histogram1D = (Histogram1D) histogram;
			dataIterator = array.getIterator();
			axisIterator = axis.getIterator();
			offset = getOffset(array);
			if (hasVariance) {
				IArrayIterator varianceIterator = variance.getIterator();
				int index = 0;
				while (dataIterator.hasNext() && axisIterator.hasNext()) {
					double axisValue = axisIterator.getDoubleNext();
					double dataValue = dataIterator.getDoubleNext();
					double errorValue = Math.sqrt(varianceIterator.getDoubleNext());
					if (!Double.isNaN(axisValue) && axisValue >= minXValue && axisValue <= maxXValue && !Double.isNaN(dataValue)){
						try {
							if (minYValue > dataValue) {
								minYValue = dataValue;
							}
							if (maxYValue < dataValue) {
								maxYValue = dataValue;
								peakX = axisValue;
							}
							if (inverse) dataValue = - dataValue;
							histogram1D.fill(axisValue, dataValue + offset);
							histogram1D.setBinError(histogram1D.coordToIndex(axisValue), errorValue);
							dataPointSet.addPoint();
							dataPointSet.point(index).coordinate(0).setValue(axisValue);
							dataPointSet.point(index).coordinate(1).setValue(dataValue);
							dataPointSet.point(index).coordinate(1).setErrorPlus(dataValue + errorValue);
							dataPointSet.point(index).coordinate(1).setErrorMinus(dataValue - errorValue);
							index++;
						} catch (Exception e) {
						}
						//System.out.println(axisValue + " : " + dataValue + offset);
					}
				}
			}else{
				int index = 0;
				while (dataIterator.hasNext() && axisIterator.hasNext()) {
					double axisValue = axisIterator.getDoubleNext();
					double dataValue = dataIterator.getDoubleNext();
					if (!Double.isNaN(axisValue) && axisValue >= minXValue && axisValue <= maxXValue && !Double.isNaN(dataValue)){
						if (minYValue > dataValue) {
							minYValue = dataValue;
						}
						if (maxYValue < dataValue) {
							maxYValue = dataValue;
							peakX = axisValue;
						}
						if (inverse) dataValue = - dataValue;
						histogram1D.fill(axisValue, dataValue + offset);
						dataPointSet.addPoint();
						dataPointSet.point(index).coordinate(0).setValue(axisValue);
						dataPointSet.point(index).coordinate(1).setValue(dataValue);
						index ++;
						//System.out.println(axisValue + " : " + dataValue + offset);
					}
				}
			}
//			fitter.resetParameterSettings();
			setParameters();
//			double[] err = new double[(int) axis.getSize()];
//			dataPointSet = analysisFactory.createDataPointSetFactory(tree).createXY("dataset",
//					(double[]) axis.getStorage(), (double[]) data.getStorage(), err, err);
//			dataIterator = array.getIterator();
//			axisIterator = axis.getIterator();
//			while (dataIterator.hasNext() && axisIterator.hasNext()) {
//				Double axisValue = axisIterator.getDoubleNext();
//				Double dataValue = dataIterator.getDoubleNext();
//				if (!axisValue.isNaN() && axisValue >= minXValue && axisValue <= maxXValue && !dataValue.isNaN()){
//					dataPointSet.addPoint(new DataPoint(new double[]{axisValue.doubleValue(), 
//							dataValue.doubleValue()}));
//				}
//			}
//			System.out.println(dataPointSet.size());
			
			fitData = fitFactory.createFitData();
//			fitData.create1DConnection(histogram1D);
			fitData.create1DConnection(dataPointSet, 0, 1);
			break;
		default:
			throw new FitterException(rank + " dimension not supported");
		}
	}

	private double getOffset(IArray data) {
		if (! inverse){
			double min = data.getArrayMath().getMinimum();
			if (min < 0)
				return - min; 
			return 0;
		}else{
			double max = data.getArrayMath().getMaximum();
			if (max > 0)
				return max;
			return 0;
		}
	}

	protected void setFunctionText(String functionText) {
		this.functionText = functionText;
	}

	public void setParameterValue(String name, double value){
//		parameters.remove(name);
		parameters.put(name, value);
		fitErrors.put(name, 0.);
		fitFunction.setParameter(name, value);
	}

	public abstract void setParameters();

	protected String parameterNamesToString(){
		String result = "";
		Set<String> names = parameters.keySet();
		for (Iterator<String> iterator = names.iterator(); iterator.hasNext();) {
			String name = iterator.next();
			result += name;
			if (iterator.hasNext())
				result += ",";
		}
		return result;
	}

	public void fit() throws IOException, InvalidArrayTypeException{
		try{
			int status = 0;
			int count = 0;
			while (status != 3 && count < 10) {
				fitResult = fitter.fit(fitData, fitFunction);
				updateParameters();
				count ++;
				status = fitResult.fitStatus();
			}
			System.out.println("Chi2 = " + fitResult.quality());
		}catch (Exception e) {
			e.printStackTrace();
		}
		createPlotResult();
	}

	protected void updateParameters(){
		for (Entry<String, Double> entry : parameters.entrySet()){
			fitFunction.setParameter(entry.getKey(), 
					fitResult.fittedParameter(entry.getKey()));
		}
	}
	
	protected void addParameterSetting(){}

	protected void createPlotResult() 
	throws IOException, InvalidArrayTypeException {
		if (fitResult == null)
			return;
		for (Entry<String, Double> entry : parameters.entrySet())
			//			setParameterValue(entry.getKey(), fitResult.fittedParameter(entry.getKey()));
			entry.setValue(fitResult.fittedParameter(entry.getKey()));
		String[] parameterNames = fitResult.fittedParameterNames();
		double[] fitterErrors = fitResult.errors();
		for (int i = 0; i < parameterNames.length; i ++){
			fitErrors.put(parameterNames[i], fitterErrors[i]);
		}
		switch (dimension) {
		case 1:
//			String preEvaluationString = getPreEvaluationString();
//			MathEvaluator evaluator = new MathEvaluator(preEvaluationString);
			IFunction resultFunctino = fitResult.fittedFunction();
			IAxis axis0 = data.getAxisList().get(0);
			double[] resultAxisStorage = new double[(int) axis0.getSize() 
			                                        * resolutionMultiple + 1];
			double[] resultDataStorage = new double[resultAxisStorage.length];
//			ArrayIterator axisIterator = plot.getAxis(0).getData().getIterator();
			IIndex axisIndex = axis0.getData().getIndex();
			boolean isAscending = true;
			try{
				isAscending = axis0.getData().getDouble(axisIndex.set(0)) 
						< axis0.getData().getDouble(axisIndex.set(1)); 
			}catch (Exception e) {
			}
			double minAxis = axis0.getData().getArrayMath().getMinimum();
			double maxAxis = axis0.getData().getArrayMath().getMaximum();
			double step = (maxAxis - minAxis) / (resultAxisStorage.length - 1);
			IArrayMath amath = data.getSignal().getData().getArrayMath();
			double maxIntensity = amath.getMaximum();
			double minIntensity = amath.getMinimum();
			double intensityWith = maxIntensity - minIntensity;
			maxIntensity = maxIntensity + intensityWith * CutRange;
			minIntensity = minIntensity - intensityWith * CutRange;
//			int index = 0;
//			while (axisIterator.hasNext()){
//			for (int i = 0; i < resolutionMultiple; i++) {
//			int thisIndex = (index++) * resolutionMultiple + i; 
//			resultAxisStorage[thisIndex] = axisIterator.getDoubleNext();
////			resultDataStorage[thisIndex] = evaluate(preEvaluationString, 
////			resultAxisStorage[thisIndex]);
//			resultDataStorage[thisIndex] = evaluate(evaluator, resultAxisStorage[thisIndex]);
//			}
//			}
			for (int i = 0; i < resultDataStorage.length; i++) {
				if (isAscending)
					resultAxisStorage[i] = minAxis + step * i;
				else
					resultAxisStorage[i] = maxAxis - step * i;
//				resultDataStorage[i] = evaluate(evaluator, resultAxisStorage[i]) - offset;
//				if (inverse) resultDataStorage[i] = - resultDataStorage[i];
				resultDataStorage[i] = resultFunctino.value(new double[]{resultAxisStorage[i]}) 
					- offset;
				if (inverse) resultDataStorage[i] = - resultDataStorage[i];
				if (resultDataStorage[i] > maxIntensity)
					resultDataStorage[i] = maxIntensity;
				if (resultDataStorage[i] < minIntensity)
					resultDataStorage[i] = minIntensity;

			}
			IArray resultAxis = Factory.createArray(Double.TYPE, 
					new int[]{resultAxisStorage.length}, resultAxisStorage);
			IArray resultCurve = Factory.createArray(Double.TYPE, 
					new int[]{resultDataStorage.length}, resultDataStorage);
			String newTitle = "_fitting";
			if (data.getTitle() != null) {
				newTitle = data.getTitle() + newTitle;
			}
			resultData = nexusFactory.createNXdata(null, newTitle);
			ISignal signal = nexusFactory.createNXsignal(resultData, "fitting_signal", resultCurve);
			resultData.setSignal(signal);
			IAxis axis = nexusFactory.createNXaxis(resultData, axis0.getTitle(), resultAxis);
			List<IAxis> axes = new ArrayList<IAxis>();
			axes.add(axis);
			resultData.setAxes(axes);
			IDataItem chi2Item = Factory.createDataItem(resultData, "quality", 
					Factory.createArray(new double[]{fitResult.quality()}));
			resultData.addDataItem(chi2Item);
			for (Entry<String, Double> entry : parameters.entrySet())
				chi2Item.addStringAttribute(entry.getKey(), String.valueOf(entry.getValue()));
			break;

		default:
			break;
		}
	}

	public INXdata getResult() throws IOException{
		return resultData;
	}

	public int getResolutionMultiple() {
		return resolutionMultiple;
	}

	public void setResolutionMultiple(int resolutionMultiple) {
		this.resolutionMultiple = resolutionMultiple;
	}

	private String getPreEvaluationString(){
		String evaluationString = functionText;
//		Set<Entry<String, Double>> parameterSet = parameters.entrySet();
//		for (Entry<String, Double> entry : parameterSet){
//			evaluationString = evaluationString.replace(entry.getKey(), String.valueOf("(" + 
//					entry.getValue() + ")"));
//		}
		Set<String> names = parameters.keySet();
		List<String> nameList = new ArrayList<String>();
		for (String name : names){
			addNameToSortedListOnLength(name, nameList);
		}
		for (String name : nameList){
			double value = parameters.get(name);
			if (Math.abs(value) < 0.00000001)
				value = 0;
			evaluationString = evaluationString.replace(name, "(" +String.valueOf(value) + ")");
		}
//		functionText
		return evaluationString;
	}

	private void addNameToSortedListOnLength(String name, List<String> nameList) {
		Iterator<String> iter = nameList.iterator();
		int index = 0;
		while (iter.hasNext()){
			String oldName = iter.next();
			if (name.length() > oldName.length()){
				break;
			}
			index ++;
		}
		nameList.add(index, name);
	}

//	private double evaluate(String preEvaluationString, double ...values){
//		for (int i = 0; i < values.length; i++) {
//			String name = "x[" + i + "]";
//			preEvaluationString = preEvaluationString.replace(name, "(" + String.valueOf(values[i]) + ")");
//		}		 
////		Evaluator evaluator = new Evaluator(preEvaluationString);
//
////		evaluator.initialize(tuple);
//
////		IEvaluator evaluator = tupleFactory.createEvaluator(preEvaluationString);
//		return 0;
//	}

//	private double evaluate(MathEvaluator evaluator, double ...values) {
//		for (int i = 0; i < values.length; i++) {
//			String name = "x[" + i + "]";
////			preEvaluationString = preEvaluationString.replace(name, "(" + String.valueOf(values[i]) + ")");
//			evaluator.addVariable(name, values[i]);
//		}		 
//		return evaluator.getValue();
//	}

	public double getQuality(){
		if (fitResult == null)
			return Double.NaN;
		return fitResult.quality() / histogram.entries();
	}

	public boolean isInverse() {
//		if (isInverseAllowed)
//			return inverse;
//		else{
//			return false;
		return isInverseAllowed && inverse;
	}

	public void setInverse(boolean inverse) throws DimensionNotSupportedException, 
	IOException, FitterException {
		if (this.inverse != inverse){
			this.inverse = inverse;
			createHistogram(data);
		}
	}
	
	public boolean isInverseAllowed(){
		return isInverseAllowed;
	}
	
	protected void setInverseAllowed(boolean isAllowed){
		isInverseAllowed = isAllowed;
	}
	
	public IGroup toGDMGroup(){
		return null;
	}
	
	public void reset(){
		if (fitter != null){
			fitter.resetConstraints();
			fitter.resetParameterSettings();
		}
	}
	
	public IFitter getRawFitter() {
		return fitter;
	}
}
