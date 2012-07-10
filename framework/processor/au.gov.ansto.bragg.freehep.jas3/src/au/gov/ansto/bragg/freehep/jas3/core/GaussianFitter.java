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
package au.gov.ansto.bragg.freehep.jas3.core;

import hep.aida.ref.histogram.Histogram1D;

import java.io.IOException;

import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;

import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.freehep.jas3.core.StaticField.FunctionType;
import au.gov.ansto.bragg.freehep.jas3.exception.DimensionNotSupportedException;
import au.gov.ansto.bragg.freehep.jas3.exception.FitterException;

/**
 * @author nxi
 * Created on 19/06/2008
 */
public class GaussianFitter extends Fitter {

	private String gaussian1DFuncionText = 
		"background+amplitude*exp(-(x[0]-mean)*(x[0]-mean)/sigma/sigma/2)";
	/**
	 * 
	 */
	public GaussianFitter() {
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
		default:
			break;
		}
//		fitFunction = analysisFactory.createFunctionFactory(tree).createFunctionFromScript(
//				"Gaussian", dimension, getFunctionText(), parameterNamesToString(), 
//				"Gaussian " + dimension + "D fitting");
		createFitFunction();
	}

	public GaussianFitter(Plot plot) throws DimensionNotSupportedException, IOException, 
	InvalidArrayTypeException, PlotFactoryException, FitterException{
		this(plot.findSingal().getRank());
		createHistogram(plot);
	}

	@Override
	public void setParameters() {
		switch (getDimension()) {
		case 1:			
			Histogram1D histogram1D = (Histogram1D) histogram;
			double maxHeight = histogram1D.maxBinHeight();
			double sigma = histogram1D.rms();
			double mean = 0;
			try{
				mean = findMean();
//				histogram1D.reset();
//				reconstructHistogram(histogram1D, mean, sigma);
			}catch (Exception e) {
			}
			setParameterValue("amplitude", histogram1D.maxBinHeight() - histogram1D.minBinHeight());
			setParameterValue("background", histogram1D.minBinHeight());
			setParameterValue("mean", mean);
			setParameterValue("sigma", histogram1D.rms());
//			try {
//					setParameterValue("mean", findMean());
//				} catch (Exception e) {
//					e.printStackTrace();
//				} 
//			setParameterValue("sigma", histogram1D.rms());
			break;

		default:
			break;
		}
	}

	private void reconstructHistogram(Histogram1D histogram1D, double mean,
			double sigma) throws Exception {
		IArray signal = plot.findSignalArray();
		IArray axis = plot.getAxisArrayList().get(0);
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
		IArray signal = plot.findSignalArray();
		IArray axis = plot.getAxisArrayList().get(((NcGroup) plot).getAxesArrayList().size() - 1);
		IArrayIterator signalIterator = signal.getIterator();
		IArrayIterator axisIterator = axis.getIterator();
		double halfIntensity = signal.getArrayMath().getMaximum() / 2;
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
			fitter.fitParameterSettings("amplitude").setBounds(0, histogram.maxBinHeight() * 2);
			fitter.fitParameterSettings("sigma").setBounds(0, ((Histogram1D) histogram).rms() * 2);
			fitter.fitParameterSettings("background").setLowerBound(0);
			break;

		default:
			break;
		}
	}

}
