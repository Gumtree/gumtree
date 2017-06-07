package org.gumtree.data.nexus.fitting.apache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.math.IArrayMath;
import org.gumtree.data.nexus.IAxis;
import org.gumtree.data.nexus.ISignal;
import org.gumtree.data.nexus.IVariance;

public class GaussianWithBackgroundFitter extends AbstractCommonsFitter{

	private GaussianWithBackgroundFunction function;
	
	public enum Parameter {amplitude, mean, sigma, background};
	
	public GaussianWithBackgroundFitter() {
		setDimension(1);
		setInverse(false);
		setParameterValue(Parameter.amplitude.name(), Double.NaN);
		setParameterValue(Parameter.mean.name(), Double.NaN);
		setParameterValue(Parameter.sigma.name(), Double.NaN);
		setParameterValue(Parameter.background.name(), Double.NaN);
	}

	@Override
	public void fit() throws IOException, InvalidArrayTypeException {
		double norm = getParameterValue(Parameter.amplitude.name());
		double mean = getParameterValue(Parameter.mean.name());
		double sigma = getParameterValue(Parameter.sigma.name());
		double bg = getParameterValue(Parameter.background.name());
		if (Double.isNaN(sigma) && Double.isNaN(norm) && Double.isNaN(mean) && Double.isNaN(bg)) {
			setCurveFitter(ApacheGaussianWithBackgroundFitter.create());
		} else {
			setCurveFitter(ApacheGaussianWithBackgroundFitter.create().withStartPoint(new double[] {norm, mean, sigma, bg}));
		}
		super.fit();
	}
	
	@Override
	protected void createFunction() {
		function = new GaussianWithBackgroundFunction(getParameterValue(Parameter.amplitude.name()), 
				getParameterValue(Parameter.mean.name()), 
				getParameterValue(Parameter.sigma.name()), 
				getParameterValue(Parameter.background.name()));
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
				calculateQuality();
				double[] errors = ((ApacheGaussianWithBackgroundFitter) getCurveFitter()).getParameterErrors();
				getFitErrors().put(Parameter.amplitude.name(), errors[0]);
				getFitErrors().put(Parameter.mean.name(), errors[1]);
				getFitErrors().put(Parameter.sigma.name(), errors[2]);
				getFitErrors().put(Parameter.background.name(), errors[3]);
			}
			break;
		default:
			break;
		}
		
	}

	private void calculateQuality() throws IOException {
		quality = 0;
//		double err = 0;
		IAxis axis0 = data.getAxisList().get(0);
		IArrayIterator axisIter = axis0.getData().getIterator();
		IArrayIterator dataIter = data.getSignal().getData().getIterator();
		IVariance var = data.getVariance();
		boolean hasVariance = false;
		IArrayIterator varIter = null;
		if (var != null && var.getData() != null) {
			hasVariance = true;
			varIter = var.getData().getIterator();
		}
		int idx=0;
		while (axisIter.hasNext() && dataIter.hasNext()) {
			double x = axisIter.getDoubleNext();
			if (x < minXValue || x > maxXValue) {
				dataIter.next();
				if (hasVariance) {
					varIter.next();
				}
				continue;
			}
			double e = function.value(x);
			double o = dataIter.getDoubleNext();
			double od = o - e;
			if (hasVariance) {
				double v = varIter.getDoubleNext();
				if (v < 0) {
					v = -v;
				} else if (v == 0) {
					v = 1;
				}
				quality += od * od / v;
//				err += 1 / v;
			} else {
				if (o < 0) {
					o = -o;
				} else if(o == 0) {
					o = 1;
				}
				quality += od * od / o;
			}
//			double od = dataIter.getDoubleNext() - e;
//			if (e < 0) {
//				e = -e;
//			} else if (e == 0) {
//				e = 1;
//			}
//			quality += od * od / e;
			idx += 1;
		}
		if (idx != 0) {
			quality = quality / idx;
		}
//		if (err != 0) {
//			err = Math.pow(err * quality, -0.5);
//			getFitErrors().put(Parameter.mean.name(), err);
//		}
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		super.reset();
		setParameterValue(Parameter.amplitude.name(), Double.NaN);
		setParameterValue(Parameter.mean.name(), Double.NaN);
		setParameterValue(Parameter.sigma.name(), Double.NaN);
		setParameterValue(Parameter.background.name(), Double.NaN);
	}
	
}
