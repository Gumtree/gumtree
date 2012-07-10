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

import java.io.IOException;

import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.nexus.INXdata;
import org.gumtree.data.nexus.fitting.StaticField.FunctionType;


/**
 * @author nxi
 * Created on 24/06/2008
 */
public class LinearFitter extends Fitter {
	private String linear1DFuncionText = 
		"a*x[0]+b";
	/**
	 * 
	 */
	public LinearFitter() {
		fitter = fitFactory.createFitter(fitterType.getValue(), enginType.name());
		setFunctionType(FunctionType.Linear);
	}

	public LinearFitter(int dimension) throws FitterException{
		this();
		setDimension(dimension);
		switch (dimension) {
		case 1:
			setFunctionText(linear1DFuncionText);
			addParameter("a");
			addParameter("b");
			break;
		default:
			break;
		}
		createFitFunction();
	}
	
	public LinearFitter(INXdata data)throws DimensionNotSupportedException, IOException, 
	InvalidArrayTypeException, FitterException{
		this(data.getSignal().getRank());
		createHistogram(data);
	}
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.freehep.jas3.core.Fitter#setParameters()
	 */
	@Override
	public void setParameters() {
		switch (getDimension()) {
		case 1:
			double xRange = (maxXValue - minXValue);
			double yRange = (maxYValue - minYValue);
			if (xRange != 0 && yRange < Double.POSITIVE_INFINITY && yRange > Double.NEGATIVE_INFINITY) {
				if (peakX > (maxXValue + minXValue) / 2) { 
					setParameterValue("a", yRange / xRange);
					setParameterValue("b", (maxXValue*minYValue - minXValue*maxYValue)/xRange + offset);
				} else {
					setParameterValue("a", - yRange / xRange);
					setParameterValue("b", (maxXValue*maxYValue - minXValue*minYValue)/xRange + offset);
				}
			}
			break;

		default:
			break;
		}
	}

}
