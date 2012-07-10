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
 * Created on 09/01/2009
 */
public class PowerFitter extends Fitter {

	private String power1DFuncionText = 
		"a*pow(x[0],b)";
	/**
	 * 
	 */
	public PowerFitter() {
		// TODO Auto-generated constructor stub
		setFunctionType(FunctionType.Power);
	}

	public PowerFitter(int dimension) throws FitterException{
		this();
		setDimension(dimension);
		switch (dimension) {
		case 1:
			setFunctionText(power1DFuncionText);
			addParameter("a");
			addParameter("b");
			break;
		default:
			break;
		}
		createFitFunction();
	}
	
	public PowerFitter(Plot plot) throws DimensionNotSupportedException, IOException, 
	InvalidArrayTypeException, PlotFactoryException, FitterException{
		this(plot.findSingal().getRank());
		createHistogram(plot);
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
//			setParameterValue("amplitude", histogram1D.maxBinHeight() - histogram1D.minBinHeight());
			break;

		default:
			break;
		}
	}

}
