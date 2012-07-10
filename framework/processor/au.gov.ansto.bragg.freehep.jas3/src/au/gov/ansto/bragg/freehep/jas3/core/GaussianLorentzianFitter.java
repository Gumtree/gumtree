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

import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.freehep.jas3.core.StaticField.FunctionType;
import au.gov.ansto.bragg.freehep.jas3.exception.DimensionNotSupportedException;
import au.gov.ansto.bragg.freehep.jas3.exception.FitterException;

/**
 * @author nxi
 * Created on 12/09/2008
 */
public class GaussianLorentzianFitter extends Fitter {
	private String gaussianLorentzian1DFuncionText = 
		"p*scale/3.14/((x[0]-mean)*(x[0]-mean)+scale*scale)+" +
		"(1-p)*(amplitude*exp(-(x[0]-mean)*(x[0]-mean)/sigma/sigma/2))+background";
	private static double P_GUESS_VALUE = 0.5;
	/**
	 * 
	 */
	public GaussianLorentzianFitter() {
		// TODO Auto-generated constructor stub
		setFunctionType(FunctionType.GaussianLorentzian);
	}
	
	public GaussianLorentzianFitter(int dimension) throws FitterException{
		this();
		setDimension(dimension);
		setInverseAllowed(true);
		switch (dimension) {
		case 1:
			setFunctionText(gaussianLorentzian1DFuncionText);
			addParameter("mean");
			addParameter("sigma");
			addParameter("amplitude");
			addParameter("p");
			addParameter("scale");
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

	public GaussianLorentzianFitter(Plot plot) throws DimensionNotSupportedException, IOException, 
	InvalidArrayTypeException, PlotFactoryException, FitterException{
		this(plot.findSingal().getRank());
		createHistogram(plot);
	}
	
	@Override
	public void setParameters() {
		// TODO Auto-generated method stub
		switch (getDimension()) {
		case 1:
			Histogram1D histogram1D = (Histogram1D) histogram;
			setParameterValue("p", P_GUESS_VALUE);
			setParameterValue("amplitude", histogram1D.maxBinHeight() * P_GUESS_VALUE);
			setParameterValue("mean", histogram1D.mean());
			setParameterValue("sigma", histogram1D.rms());
			break;

		default:
			break;
		}
	}

	protected void addParameterSetting() {
		switch (getDimension()) {
		case 1:
			fitter.fitParameterSettings("p").setBounds(0, 1);
//			fitter.fitParameterSettings("amplitude").setBounds(0, histogram.maxBinHeight() * 2);
//			fitter.fitParameterSettings("sigma").setBounds(0, ((Histogram1D) histogram).rms() * 4);
//			fitter.fitParameterSettings("scale").setBounds(0, ((Histogram1D) histogram).rms());
			break;

		default:
			break;
		}
	}
}
