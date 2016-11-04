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

import hep.aida.ref.histogram.Histogram1D;
import hep.aida.ref.histogram.Histogram2D;

import java.io.IOException;
import java.util.Map.Entry;

import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.nexus.INXdata;
import org.gumtree.data.nexus.fitting.StaticField.FunctionType;


/**
 * @author nxi
 * Created on 19/06/2008
 */
public class GaussianFitter extends Fitter {

	private String gaussian1DFuncionText = 
			"background+amplitude*exp(-(x[0]-mean)*(x[0]-mean)/sigma/sigma/2)";
	private String gaussian2DFuncionText = 
			"b+a*exp(-(x[0]-m1)*(x[0]-m1)/s1/s1/2-(x[1]-m2)*(x[1]-m2)/s2/s2/2)";
	/**
	 * 
	 */
	public GaussianFitter() {
		fitter = fitFactory.createFitter(fitterType.getValue(), enginType.name());
		fitter.setUseFunctionGradient(true);
		setFunctionType(FunctionType.Gaussian);
	}
	
	public GaussianFitter(int dimension) throws FitterException{
		this();
		setDimension(dimension);
		setInverseAllowed(true);
		switch (dimension) {
		case 1:
			setFunctionText(gaussian1DFuncionText);
			addParameter("amplitude");
			addParameter("mean");
			addParameter("sigma");
			addParameter("background");
			break;
		case 2:
			setFunctionText(gaussian2DFuncionText);
			addParameter("a");
			addParameter("b");
			addParameter("m1");
			addParameter("s1");
			addParameter("m2");
			addParameter("s2");
		default:
			break;
		}
//		fitFunction = analysisFactory.createFunctionFactory(tree).createFunctionFromScript(
//				"Gaussian", dimension, getFunctionText(), parameterNamesToString(), 
//				"Gaussian " + dimension + "D fitting");
		createFitFunction();
	}

	public GaussianFitter(INXdata data) throws DimensionNotSupportedException, IOException, 
	InvalidArrayTypeException, FitterException{
		this(data.getSignal().getRank());
		createHistogram(data);
	}

	@Override
	public void setParameters() {
		switch (getDimension()) {
		case 1:			
			Histogram1D histogram1D = (Histogram1D) histogram;
			double mean = 0;
			try{
				mean = findMean();
			}catch (Exception e) {
			}
			if (minYValue < Double.POSITIVE_INFINITY && maxYValue > minYValue) {
				setParameterValue("amplitude", maxYValue - minYValue);
			} else {
				setParameterValue("amplitude", histogram1D.maxBinHeight() - histogram1D.minBinHeight());
			}
			if (minYValue < Double.POSITIVE_INFINITY) {
				setParameterValue("background", minYValue);
			} else {
				setParameterValue("background", histogram1D.minBinHeight());
			}
			if (peakX > minXValue && peakX < maxXValue) {
				setParameterValue("mean", peakX);
			} else {
				setParameterValue("mean", mean);
			}
			setParameterValue("sigma", histogram1D.rms() / 2);
			break;
		case 2:
			Histogram2D histogram2D = (Histogram2D) histogram;
//			mean = 0;
//			try{
//				mean = findMean();
//			}catch (Exception e) {
//			}
			if (minZValue < Double.POSITIVE_INFINITY && maxZValue > minZValue) {
				setParameterValue("a", maxZValue - minZValue);
			} else {
				setParameterValue("a", histogram2D.maxBinHeight() - histogram2D.minBinHeight());
			}
			if (minZValue < Double.POSITIVE_INFINITY) {
				setParameterValue("b", minZValue);
			} else {
				setParameterValue("b", histogram2D.minBinHeight());
			}
//			if (peakX > minXValue && peakX < maxXValue) {
//				setParameterValue("mean", peakX);
//			} else {
//				setParameterValue("mean", mean);
//			}
//			setParameterValue("sigma", histogram1D.rms() / 2);
			break;
		default:
			break;
		}
	}

	protected void updateParameters(){
		super.updateParameters();
		switch (getDimension()) {
		case 1:
			double rms = ((Histogram1D) histogram).rms();
			setParameterValue("sigma", rms / 2);
			break;
		case 2:
			setParameterValue("s2", ((Histogram2D) histogram).rmsX());
			setParameterValue("s1", ((Histogram2D) histogram).rmsY());
			break;
		default:
			break;
		}
	}
	
	private void reconstructHistogram(Histogram1D histogram1D, double mean,
			double sigma) throws Exception {
		IArray signal = data.getSignal().getData();
		IArray axis = data.getAxisList().get(0).getData();
		IArrayIterator signalIterator = signal.getIterator();
		IArrayIterator axisIterator = axis.getIterator();
		while (signalIterator.hasNext()){
			double axisValue = axisIterator.getDoubleNext();
			double signalValue = signalIterator.getDoubleNext();
			if (axisValue > mean - sigma && axisValue < mean + sigma)
				histogram1D.fill(axisValue, signalValue + offset);
		}
	}

	private double findMean() throws IOException, SignalNotAvailableException {
		IArray signal = data.getSignal().getData();
		IArray axis = data.getAxisList().get(data.getAxisList().size() - 1).getData();
		IArrayIterator signalIterator = signal.getIterator();
		IArrayIterator axisIterator = axis.getIterator();
//		double halfIntensity = signal.getArrayMath().getMaximum() / 2;
		double halfIntensity = maxYValue / 2;
		double weightIntensity = 0;
		double intensitySum = 0;
		while (signalIterator.hasNext()){
			double intensity = signalIterator.getDoubleNext();
			double axisValue = axisIterator.getDoubleNext();
			if (!Double.isNaN(axisValue) && axisValue >= minXValue && axisValue <= maxXValue 
					&& !Double.isNaN(intensity))
				if (intensity >= halfIntensity){
					weightIntensity += intensity * axisValue;
					intensitySum += intensity;
			}
		}
		if (intensitySum == 0)
			return 0;
		return weightIntensity / intensitySum;
	}

	
	protected void addParameterSetting() {
		switch (getDimension()) {
		case 1:
//			fitter.fitParameterSettings("amplitude").setBounds(0, histogram.maxBinHeight() * 2);
//			fitter.fitParameterSettings("sigma").setBounds(0, ((Histogram1D) histogram).rms() * 2);
//			fitter.fitParameterSettings("background").setLowerBound(0);
//			fitter.fitParameterSettings("sigma").setLowerBound(0);
//			fitter.fitParameterSettings("sigma").setBounds(0, 10000);
//			fitter.fitParameterSettings("mean").setBounds(15000, 35000);
//			fitter.setConstraint("sigma>0");
			break;
		case 2:
//			fitter.fitParameterSettings("s1").setLowerBound(0);
//			fitter.fitParameterSettings("s2").setLowerBound(0);
			break;
		default:
			break;
		}
	}

}
