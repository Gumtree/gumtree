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

import java.io.IOException;

import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.freehep.jas3.core.StaticField.FunctionType;
import au.gov.ansto.bragg.freehep.jas3.exception.DimensionNotSupportedException;
import au.gov.ansto.bragg.freehep.jas3.exception.FitterException;

/**
 * @author nxi
 * Created on 24/06/2008
 */
public class CubicFitter extends Fitter {

	private String cubic1DFuncionText = 
		"a*x[0]*x[0]*x[0]+b*x[0]*x[0]+c*x[0]+d";
	/**
	 * 
	 */
	public CubicFitter() {
		// TODO Auto-generated constructor stub
		setFunctionType(FunctionType.Cubic);
	}

	public CubicFitter(int dimension) throws FitterException{
		this();
		setDimension(dimension);
		switch (dimension) {
		case 1:
			setFunctionText(cubic1DFuncionText);
			addParameter("a");
			addParameter("b");
			addParameter("c");
			addParameter("d");
			break;
		default:
			break;
		}
		createFitFunction();
	}
	
	public CubicFitter(Plot plot) throws DimensionNotSupportedException, 
	IOException, FitterException{
		this(plot.findSingal().getRank());
		createHistogram(plot);
	}
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.freehep.jas3.core.Fitter#setParameters()
	 */
	@Override
	public void setParameters() {
		// TODO Auto-generated method stub

	}

}
