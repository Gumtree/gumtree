package org.gumtree.data.nexus.fitting.apache;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.nexus.IAxis;
import org.gumtree.data.nexus.INXdata;
import org.gumtree.data.nexus.INexusFitter;
import org.gumtree.data.nexus.IVariance;
import org.gumtree.data.nexus.fitting.DimensionNotSupportedException;
import org.gumtree.data.nexus.fitting.FitterException;
import org.gumtree.data.nexus.fitting.StaticField.EnginType;
import org.gumtree.data.nexus.fitting.StaticField.FitterType;
import org.gumtree.data.nexus.fitting.StaticField.FunctionType;
import org.gumtree.data.nexus.utils.NexusFactory;

public abstract class AbstractCommonsFitter implements INexusFitter {

	protected static final NexusFactory nexusFactory = new NexusFactory();
	protected static double CutRange = 0.2;

	private int dimension;
	private String description;
	private String title;
	protected INXdata data;
	protected INXdata resultData;
	protected double quality;
	protected boolean inverse = false;
	private Map<String, Double> parameters = new LinkedHashMap<String, Double>();
	private Map<String, Double> fitErrors = new HashMap<String, Double>();
	protected double offset;
	protected double minXValue = Double.MIN_VALUE;
	protected double maxXValue = Double.MAX_VALUE;
	protected double minYValue = Double.MIN_VALUE;
	protected double maxYValue = Double.MAX_VALUE;
	protected double minZValue = Double.POSITIVE_INFINITY;
	protected double maxZValue = Double.NEGATIVE_INFINITY;
	protected double peakX = Double.NaN;
	protected double peakY = Double.NaN;
	private AbstractCurveFitter curveFitter;
	private WeightedObservedPoints histogram;
	private int resolution = 10;
	
	public AbstractCommonsFitter() {
	}

	@Override
	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

	protected void setCurveFitter(AbstractCurveFitter fitter) {
		this.curveFitter = fitter;
	}
	
	public AbstractCurveFitter getCurveFitter() {
		return curveFitter;
	}
	
	@Override
	public int getDimension() {
		return dimension;
	}

	@Override
	public void parse(String functionText) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;

	}

	@Override
	public FitterType getFitterType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EnginType getEnginType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFunctionText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Double> getParameters() {
		return parameters;
	}

	@Override
	public Map<String, Double> getFitErrors() {
		// TODO Auto-generated method stub
		return fitErrors;
	}

	@Override
	public double getParameterValue(String name) {
		// TODO Auto-generated method stub
		return parameters.get(name);
	}

	@Override
	public double getFitError(String name) {
		// TODO Auto-generated method stub
		return fitErrors.get(name);
	}

	@Override
	public FunctionType getFunctionType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFunctionType(FunctionType functionType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createHistogram(INXdata data) throws IOException,
			FitterException {
		createHistogram(data, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	@Override
	public void createHistogram(INXdata data, double minX, double maxX)
			throws IOException, FitterException {
//		final WeightedObservedPoints histogram = new WeightedObservedPoints();
		if (histogram != null) {
			histogram.clear();
		} else {
			histogram = new WeightedObservedPoints();
		}
		minXValue = minX;
		maxXValue = maxX;
		this.data = data;
		int rank = data.getSignal().getRank();
		if (rank != getDimension())
			throw new FitterException("data dimension does not match with fit function");
//		setDimension(rank);
		if (rank == 1) {
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
			dataIterator = array.getIterator();
			axisIterator = axis.getIterator();
			offset = getOffset(array);
			minYValue = Double.MAX_VALUE;
			maxYValue = Double.MIN_VALUE;
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
//							histogram1D.fill(axisValue, dataValue + offset);
//							histogram1D.setBinError(histogram1D.coordToIndex(axisValue), errorValue);
//							dataPointSet.addPoint();
//							dataPointSet.point(index).coordinate(0).setValue(axisValue);
//							dataPointSet.point(index).coordinate(1).setValue(dataValue);
//							dataPointSet.point(index).coordinate(1).setErrorPlus(dataValue + errorValue);
//							dataPointSet.point(index).coordinate(1).setErrorMinus(dataValue - errorValue);
							histogram.add(axisValue, dataValue + offset);
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
//						histogram1D.fill(axisValue, dataValue + offset);
//						dataPointSet.addPoint();
//						dataPointSet.point(index).coordinate(0).setValue(axisValue);
//						dataPointSet.point(index).coordinate(1).setValue(dataValue);
						histogram.add(axisValue, dataValue + offset);
						index ++;
						//System.out.println(axisValue + " : " + dataValue + offset);
					}
				}
			}
//			fitter.resetParameterSettings();
			setParameters();
			addParameterSetting();
			
//			fitData = fitFactory.createFitData();
//			fitData.create1DConnection(dataPointSet, 0, 1);
		} else {
			throw new FitterException(rank + " dimension not supported");
		}

	}

	protected void addParameterSetting() {}
	
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
	
	@Override
	public void setParameterValue(String name, double value) {
		parameters.put(name, value);
	}

	@Override
	public void setParameters() {
		
	}

	@Override
	public void fit() throws IOException, InvalidArrayTypeException {
		double[] res = curveFitter.fit(histogram.toList());
		int idx = 0;
		for (String key : parameters.keySet()) {
			parameters.put(key, res[idx]);
			fitErrors.put(key, res[idx]);
			idx ++;
		}
		createFunction();
		createPlotResult();
	}

	protected abstract void createFunction();
	
	@Override
	public void setParameterBounds(String name, double lowest, double highest) {
	}

	@Override
	public void setParameterFixed(String name, boolean isFixed) {
		// TODO Auto-generated method stub

	}

	@Override
	public abstract void createPlotResult() throws IOException,
			InvalidArrayTypeException; 
//			{
//		switch (dimension) {
//		case 1:
////			String preEvaluationString = getPreEvaluationString();
////			MathEvaluator evaluator = new MathEvaluator(preEvaluationString);
//			IFunction resultFunction = fitResult.fittedFunction();
//			IAxis axis0 = data.getAxisList().get(0);
//			double[] resultAxisStorage = new double[(int) axis0.getSize() 
//			                                        * resolutionMultiple + 1];
//			double[] resultDataStorage = new double[resultAxisStorage.length];
////			ArrayIterator axisIterator = plot.getAxis(0).getData().getIterator();
//			IIndex axisIndex = axis0.getData().getIndex();
//			boolean isAscending = true;
//			try{
//				isAscending = axis0.getData().getDouble(axisIndex.set(0)) 
//						< axis0.getData().getDouble(axisIndex.set(1)); 
//			}catch (Exception e) {
//			}
//			double minAxis = axis0.getData().getArrayMath().getMinimum();
//			double maxAxis = axis0.getData().getArrayMath().getMaximum();
//			double step = (maxAxis - minAxis) / (resultAxisStorage.length - 1);
//			IArrayMath amath = data.getSignal().getData().getArrayMath();
//			double maxIntensity = amath.getMaximum();
//			double minIntensity = amath.getMinimum();
//			double intensityWith = maxIntensity - minIntensity;
//			maxIntensity = maxIntensity + intensityWith * CutRange;
//			minIntensity = minIntensity - intensityWith * CutRange;
////			int index = 0;
////			while (axisIterator.hasNext()){
////			for (int i = 0; i < resolutionMultiple; i++) {
////			int thisIndex = (index++) * resolutionMultiple + i; 
////			resultAxisStorage[thisIndex] = axisIterator.getDoubleNext();
//////			resultDataStorage[thisIndex] = evaluate(preEvaluationString, 
//////			resultAxisStorage[thisIndex]);
////			resultDataStorage[thisIndex] = evaluate(evaluator, resultAxisStorage[thisIndex]);
////			}
////			}
//			for (int i = 0; i < resultDataStorage.length; i++) {
//				if (isAscending)
//					resultAxisStorage[i] = minAxis + step * i;
//				else
//					resultAxisStorage[i] = maxAxis - step * i;
////				resultDataStorage[i] = evaluate(evaluator, resultAxisStorage[i]) - offset;
////				if (inverse) resultDataStorage[i] = - resultDataStorage[i];
//				resultDataStorage[i] = resultFunction.value(new double[]{resultAxisStorage[i]}) 
//					- offset;
//				if (inverse) resultDataStorage[i] = - resultDataStorage[i];
//				if (resultDataStorage[i] > maxIntensity)
//					resultDataStorage[i] = maxIntensity;
//				if (resultDataStorage[i] < minIntensity)
//					resultDataStorage[i] = minIntensity;
//
//			}
//			IArray resultAxis = Factory.createArray(Double.TYPE, 
//					new int[]{resultAxisStorage.length}, resultAxisStorage);
//			IArray resultCurve = Factory.createArray(Double.TYPE, 
//					new int[]{resultDataStorage.length}, resultDataStorage);
//			String newTitle = "_fitting";
//			if (data.getTitle() != null) {
//				newTitle = data.getTitle() + newTitle;
//			}
//			resultData = nexusFactory.createNXdata(null, newTitle);
//			ISignal signal = nexusFactory.createNXsignal(resultData, "fitting_signal", resultCurve);
//			resultData.setSignal(signal);
//			IAxis axis = nexusFactory.createNXaxis(resultData, axis0.getTitle(), resultAxis);
//			List<IAxis> axes = new ArrayList<IAxis>();
//			axes.add(axis);
//			resultData.setAxes(axes);
//			IDataItem chi2Item = Factory.createDataItem(resultData, "quality", 
//					Factory.createArray(new double[]{fitResult.quality()}));
//			resultData.addDataItem(chi2Item);
//			for (Entry<String, Double> entry : parameters.entrySet())
//				chi2Item.addStringAttribute(entry.getKey(), String.valueOf(entry.getValue()));
//			break;
//		case 2:
//			resultFunction = fitResult.fittedFunction();
//			for (Entry<String, Double> entry : parameters.entrySet()) {
//				resultFunction.setParameter(entry.getKey(), entry.getValue());
//			}
//			IAxis yaxis = data.getAxisList().get(0);
//			IAxis xaxis = data.getAxisList().get(1);
//			double[] resultXAxisStorage = new double[(int) xaxis.getSize() * resolutionMultiple];
//			double[] resultYAxisStorage = new double[(int) yaxis.getSize() * resolutionMultiple];
//			double[] resultZDataStorage = new double[resultYAxisStorage.length * resultXAxisStorage.length];
//			IIndex xAxisIndex = xaxis.getData().getIndex();
//			IIndex yAxisIndex = yaxis.getData().getIndex();
//			boolean isXAscending = true;
//			try{
//				isXAscending = xaxis.getData().getDouble(xAxisIndex.set(0)) 
//						< xaxis.getData().getDouble(xAxisIndex.set(1)); 
//			}catch (Exception e) {
//			}
//			boolean isYAscending = true;
//			try{
//				isYAscending = yaxis.getData().getDouble(yAxisIndex.set(0)) 
//						< yaxis.getData().getDouble(yAxisIndex.set(1)); 
//			}catch (Exception e) {
//			}
//			double minXAxis = xaxis.getData().getArrayMath().getMinimum();
//			double maxXAxis = xaxis.getData().getArrayMath().getMaximum();
//			double xstep = (maxXAxis - minXAxis) / (resultXAxisStorage.length - 1);
//			double minYAxis = yaxis.getData().getArrayMath().getMinimum();
//			double maxYAxis = yaxis.getData().getArrayMath().getMaximum();
//			double ystep = (maxYAxis - minYAxis) / (resultYAxisStorage.length - 1);
//			IArrayMath dmath = data.getSignal().getData().getArrayMath();
//			double dmax = dmath.getMaximum();
//			double dmin = dmath.getMinimum();
//			double dWith = dmax - dmin;
//			dmax = dmax + dWith * CutRange;
//			dmin = dmin - dWith * CutRange;
//			for (int i = 0; i < resultYAxisStorage.length; i++) {
//				if (isYAscending)
//					resultYAxisStorage[i] = minYAxis + ystep * i;
//				else
//					resultYAxisStorage[i] = maxYAxis - ystep * i;
//				for (int j = 0; j < resultXAxisStorage.length; j++) {
//					if (isXAscending)
//						resultXAxisStorage[j] = minXAxis + xstep * j;
//					else
//						resultXAxisStorage[j] = maxXAxis - xstep * j;
//					double zValue = resultFunction.value(new double[]{resultYAxisStorage[i], resultXAxisStorage[j]}) 
//							- offset;
//					if (inverse) zValue = - zValue;
//					if (zValue > dmax)
//						zValue = dmax;
//					if (zValue < dmin)
//						zValue = dmin;
//					resultZDataStorage[i * resultXAxisStorage.length + j] = zValue;
//				}
//			}
//			IArray resultYAxis = nexusFactory.createArray(Double.TYPE, 
//					new int[]{resultYAxisStorage.length}, resultYAxisStorage);
//			IArray resultXAxis = nexusFactory.createArray(Double.TYPE, 
//					new int[]{resultXAxisStorage.length}, resultXAxisStorage);
//			IArray resultMap = nexusFactory.createArray(Double.TYPE, 
//					new int[]{resultYAxisStorage.length, resultXAxisStorage.length}, resultZDataStorage);
//			String fitTitle = "_fitting";
//			if (data.getTitle() != null) {
//				fitTitle = data.getTitle() + fitTitle;
//			}
//			resultData = nexusFactory.createNXdata(null, fitTitle);
//			ISignal fitSignal = nexusFactory.createNXsignal(resultData, "fitting_signal", resultMap);
//			resultData.setSignal(fitSignal);
//			IAxis xAxis = nexusFactory.createNXaxis(resultData, xaxis.getTitle(), resultXAxis);
//			IAxis yAxis = nexusFactory.createNXaxis(resultData, yaxis.getTitle(), resultYAxis);
//			List<IAxis> axesList = new ArrayList<IAxis>();
//			axesList.add(yAxis);
//			axesList.add(xAxis);
//			resultData.setAxes(axesList);
//			chi2Item = nexusFactory.createDataItem(resultData, "quality", 
//					Factory.createArray(new double[]{fitResult.quality()}));
//			resultData.addDataItem(chi2Item);
//			for (Entry<String, Double> entry : parameters.entrySet())
//				chi2Item.addStringAttribute(entry.getKey(), String.valueOf(entry.getValue()));
//			break;
//		default:
//			break;
//		}
//	}

	@Override
	public INXdata getResult() throws IOException {
		// TODO Auto-generated method stub
		return resultData;
	}

	@Override
	public int getResolutionMultiple() {
		return resolution;
	}

	@Override
	public void setResolutionMultiple(int resolutionMultiple) {
		this.resolution = resolutionMultiple;
	}

	@Override
	public double getQuality() {
		return quality;
	}

	@Override
	public boolean isInverse() {
		// TODO Auto-generated method stub
		return inverse;
	}

	@Override
	public void setInverse(boolean inverse) {
		this.inverse = inverse;
	}

	@Override
	public boolean isInverseAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IGroup toGDMGroup() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

}
