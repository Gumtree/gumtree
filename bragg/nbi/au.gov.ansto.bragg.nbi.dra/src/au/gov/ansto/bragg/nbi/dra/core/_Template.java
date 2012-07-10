/******************************************************************************* 
 * Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Paul Hathaway
 *******************************************************************************/
package au.gov.ansto.bragg.nbi.dra.core;

import java.net.URI;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;

public class _Template implements ConcreteProcessor {

	private final static DataStructureType dataStructureType = DataStructureType.plot;
	private final static DataDimensionType dataDimensionType = DataDimensionType.map;

    private Plot inPlot;
	private Plot outPlot;
    private Boolean doForceSkip = true;
    private Boolean doForceStop = false;

    private Double lambda = 0.0;
    
	public Boolean process() throws Exception {
        if (this.doForceSkip) {
        	this.outPlot = this.inPlot;
        } else {
        	switch (inPlot.getDimensionType())
        	{
        		case map:
        		case mapset:
        			// is compatible
        			break;
        		default:
        			throw new IllegalArgumentException(
        					"DataDimensionType of {map|mapset} required.");
        	}
        } 
        return this.doForceStop;
	}

	/* Client Support methods -----------------------------------------*/
	
	public static DataStructureType getDataStructureType() {
		return dataStructureType;
	}

	public static DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}

	/* Port get/set methods -----------------------------------------*/
    
    /* In-Ports */

	public void setInPlot(Plot inPlot) {
		this.inPlot = inPlot;
	}

    /* Out-Ports */

	public Plot getOutPlot() {
		return outPlot;
	}    

    /* Var-Ports (tuners) */

	/**
	 * @param lambda the wavelength
	 */
	public void set_lambda(Double lambda) {
		this.lambda = lambda;
	}

	public Double get_lambda() {
		return this.lambda;
	}

	/* Var-Ports (options) */

    public Boolean getSkip() {
		return doForceSkip;
	}

	public void setSkip(Boolean doForceSkip) {
		this.doForceSkip = doForceSkip;
	}
	
	public Boolean getStop() {
		return doForceStop;
	}

	public void setStop(Boolean doForceStop) {
		this.doForceStop = doForceStop;
	}

}
