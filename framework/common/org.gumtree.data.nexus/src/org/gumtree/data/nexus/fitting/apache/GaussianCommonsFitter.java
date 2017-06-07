package org.gumtree.data.nexus.fitting.apache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.math.IArrayMath;
import org.gumtree.data.nexus.IAxis;
import org.gumtree.data.nexus.ISignal;

public class GaussianCommonsFitter extends AbstractCommonsFitter{

	private Gaussian function;
	
	public enum Parameter {norm, mean, sigma};
	
	public GaussianCommonsFitter() {
		setDimension(1);
		setInverse(false);
		setParameterValue(Parameter.norm.name(), Double.NaN);
		setParameterValue(Parameter.mean.name(), Double.NaN);
		setParameterValue(Parameter.sigma.name(), Double.NaN);
	}

	@Override
	public void fit() throws IOException, InvalidArrayTypeException {
		double norm = getParameterValue(Parameter.norm.name());
		double mean = getParameterValue(Parameter.mean.name());
		double sigma = getParameterValue(Parameter.sigma.name());
		if (Double.isNaN(sigma) || Double.isNaN(norm) || Double.isNaN(mean)) {
			setCurveFitter(GaussianCurveFitter.create());
		} else {
			setCurveFitter(GaussianCurveFitter.create().withStartPoint(new double[] {norm, mean, sigma}));
		}
		super.fit();
	}
	
	@Override
	protected void createFunction() {
		function = new Gaussian(getParameterValue(Parameter.norm.name()), 
				getParameterValue(Parameter.mean.name()), 
				getParameterValue(Parameter.sigma.name()));
	}
	
	@Override
	public void createPlotResult() throws IOException,
			InvalidArrayTypeException {
		switch (getDimension()) {
		case 1:
			if (function != null) {
				
				IAxis axis0 = data.getAxisList().get(0);
				double[] resultAxisStorage = new double[(int) axis0.getSize() 
				                                        * getResolutionMultiple() + 1];
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
				for (int i = 0; i < resultDataStorage.length; i++) {
					if (isAscending)
						resultAxisStorage[i] = minAxis + step * i;
					else
						resultAxisStorage[i] = maxAxis - step * i;
					resultDataStorage[i] = function.value(resultAxisStorage[i]) - offset;
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
						Factory.createArray(new double[]{getQuality()}));
				resultData.addDataItem(chi2Item);
				for (Entry<String, Double> entry : getParameters().entrySet())
					chi2Item.addStringAttribute(entry.getKey(), String.valueOf(entry.getValue()));
			}
			break;
		default:
			break;
		}
		
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		super.reset();
		setParameterValue(Parameter.norm.name(), Double.NaN);
		setParameterValue(Parameter.mean.name(), Double.NaN);
		setParameterValue(Parameter.sigma.name(), Double.NaN);
	}
//	@Override
//	public void updatePlotResult() throws IOException,
//			InvalidArrayTypeException {
//		if (fitResult == null)
//			return;
////		for (Entry<String, Double> entry : parameters.entrySet())
//			//			setParameterValue(entry.getKey(), fitResult.fittedParameter(entry.getKey()));
////			entry.setValue(fitResult.fittedParameter(entry.getKey()));
////		String[] parameterNames = fitResult.fittedParameterNames();
//		switch (dimension) {
//		case 1:
//			if (function != null) {
//				for (Entry<String, Double> entry : parameters.entrySet()) {
//					resultFunction.setParameter(entry.getKey(), entry.getValue());
//				}
//				IAxis axis0 = data.getAxisList().get(0);
//				double[] resultAxisStorage = new double[(int) axis0.getSize() 
//				                                        * resolutionMultiple + 1];
//				double[] resultDataStorage = new double[resultAxisStorage.length];
//				//			ArrayIterator axisIterator = plot.getAxis(0).getData().getIterator();
//				IIndex axisIndex = axis0.getData().getIndex();
//				boolean isAscending = true;
//				try{
//					isAscending = axis0.getData().getDouble(axisIndex.set(0)) 
//							< axis0.getData().getDouble(axisIndex.set(1)); 
//				}catch (Exception e) {
//				}
//				double minAxis = axis0.getData().getArrayMath().getMinimum();
//				double maxAxis = axis0.getData().getArrayMath().getMaximum();
//				double step = (maxAxis - minAxis) / (resultAxisStorage.length - 1);
//				IArrayMath amath = data.getSignal().getData().getArrayMath();
//				double maxIntensity = amath.getMaximum();
//				double minIntensity = amath.getMinimum();
//				double intensityWith = maxIntensity - minIntensity;
//				maxIntensity = maxIntensity + intensityWith * CutRange;
//				minIntensity = minIntensity - intensityWith * CutRange;
//				//			int index = 0;
//				//			while (axisIterator.hasNext()){
//				//			for (int i = 0; i < resolutionMultiple; i++) {
//				//			int thisIndex = (index++) * resolutionMultiple + i; 
//				//			resultAxisStorage[thisIndex] = axisIterator.getDoubleNext();
//				////			resultDataStorage[thisIndex] = evaluate(preEvaluationString, 
//				////			resultAxisStorage[thisIndex]);
//				//			resultDataStorage[thisIndex] = evaluate(evaluator, resultAxisStorage[thisIndex]);
//				//			}
//				//			}
//				for (int i = 0; i < resultDataStorage.length; i++) {
//					if (isAscending)
//						resultAxisStorage[i] = minAxis + step * i;
//					else
//						resultAxisStorage[i] = maxAxis - step * i;
//					//				resultDataStorage[i] = evaluate(evaluator, resultAxisStorage[i]) - offset;
//					//				if (inverse) resultDataStorage[i] = - resultDataStorage[i];
//					resultDataStorage[i] = resultFunction.value(new double[]{resultAxisStorage[i]}) 
//							- offset;
//					if (inverse) resultDataStorage[i] = - resultDataStorage[i];
//					if (resultDataStorage[i] > maxIntensity)
//						resultDataStorage[i] = maxIntensity;
//					if (resultDataStorage[i] < minIntensity)
//						resultDataStorage[i] = minIntensity;
//
//				}
//				IArray resultAxis = Factory.createArray(Double.TYPE, 
//						new int[]{resultAxisStorage.length}, resultAxisStorage);
//				IArray resultCurve = Factory.createArray(Double.TYPE, 
//						new int[]{resultDataStorage.length}, resultDataStorage);
//				String newTitle = "_fitting";
//				if (data.getTitle() != null) {
//					newTitle = data.getTitle() + newTitle;
//				}
//				resultData = nexusFactory.createNXdata(null, newTitle);
//				ISignal signal = nexusFactory.createNXsignal(resultData, "fitting_signal", resultCurve);
//				resultData.setSignal(signal);
//				IAxis axis = nexusFactory.createNXaxis(resultData, axis0.getTitle(), resultAxis);
//				List<IAxis> axes = new ArrayList<IAxis>();
//				axes.add(axis);
//				resultData.setAxes(axes);
//				IDataItem chi2Item = Factory.createDataItem(resultData, "quality", 
//						Factory.createArray(new double[]{fitResult.quality()}));
//				resultData.addDataItem(chi2Item);
//				for (Entry<String, Double> entry : parameters.entrySet())
//					chi2Item.addStringAttribute(entry.getKey(), String.valueOf(entry.getValue()));
//			}
//			break;
//		default:
//			break;
//		}
//	}
}
