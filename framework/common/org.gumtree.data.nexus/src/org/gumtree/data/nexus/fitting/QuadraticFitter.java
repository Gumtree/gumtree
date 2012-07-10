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

import java.io.IOException;

import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.nexus.INXdata;
import org.gumtree.data.nexus.fitting.StaticField.FunctionType;

/**
 * @author nxi
 * Created on 20/06/2008
 */
public class QuadraticFitter extends Fitter {

	private String parabora1DFuncionText = 
		"a*x[0]*x[0]+b*x[0]+c";
	/**
	 * 
	 */
	public QuadraticFitter() {
		fitter = fitFactory.createFitter(fitterType.getValue(), enginType.name());
		setFunctionType(FunctionType.Quadratic);
	}

	public QuadraticFitter(int dimension) throws FitterException{
		this();
		setDimension(dimension);
		switch (dimension) {
		case 1:
			setFunctionText(parabora1DFuncionText);
			addParameter("a");
			addParameter("b");
			addParameter("c");
			break;
		default:
			break;
		}
		createFitFunction();
	}
	
	public QuadraticFitter(INXdata data) throws DimensionNotSupportedException, IOException, 
	InvalidArrayTypeException, FitterException{
		this(data.getSignal().getRank());
		createHistogram(data);
	}
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.freehep.jas3.core.Fitter#setParameters()
	 */
	@Override
	public void setParameters() {
		// TODO Auto-generated method stub
		switch (getDimension()) {
		case 1:
			Histogram1D histogram1D = (Histogram1D) histogram;
			setParameterValue("a", histogram1D.rms());
			setParameterValue("b", histogram1D.mean());
			setParameterValue("c", histogram1D.maxBinHeight());
			break;

		default:
			break;
		}
	}

}
