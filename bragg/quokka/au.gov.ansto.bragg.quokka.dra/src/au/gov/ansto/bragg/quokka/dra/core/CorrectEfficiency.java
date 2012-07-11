/******************************************************************************* 
 * Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong - initial API and implementation
 *    Paul Hathaway - refactor for Plot math and additional error propagation
 *******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.core;

import java.net.URI;

import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.nexus.NexusUtils;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

public class CorrectEfficiency extends ConcreteProcessor{

	private final static DataStructureType dataStructureType = DataStructureType.plot;
	private final static DataDimensionType dataDimensionType = DataDimensionType.map;

    private Plot inPlot;
    private Plot outPlot;
    private Plot uirPlot;
    private URI sensitivityURI;
    private Boolean doForceSkip = true;
    private Boolean doForceStop = false;
    
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

        	this.uirPlot = (Plot) PlotFactory.copyToPlot(
        			NexusUtils.getNexusData(sensitivityURI),
        			"uir",
        			DataDimensionType.map);

        	// TODO: Check size compatibility of uirPlot and input plot

        	this.uirPlot = normalise(this.uirPlot);
        	this.outPlot = this.inPlot.eltMultiply(this.uirPlot);                    
        } 
        return this.doForceStop;
    }
        
    private Plot normalise(Plot target) throws Exception {
        Double mean;
        Double mean_var;
        
        IArray uir = NexusUtils.getNexusSignal(target).getData().getArrayUtils().reduceTo(1).getArray();
        
        long   dat_cnt = 0; //count valid bins in uir data
        IArrayIterator uir_itr = uir.getIterator();
        while(uir_itr.hasNext()) {
        	if (!(Double.isNaN(uir_itr.getDoubleNext()))) {
        		dat_cnt++;
        	}
        }

        if (dat_cnt < 1) { // all elements are NaN 
        	throw new IllegalArgumentException();
        } else {
        	mean = uir.getArrayMath().sum() / dat_cnt;            	
        	IArray uir_var = target.getDataItem("variance").getData().getArrayUtils().reduceTo(1).getArray();
        	if (null==uir_var) { // no variance element, use the source data
        		mean_var = mean;
        	} else {
        		mean_var = uir_var.getArrayMath().sum() / dat_cnt;            	
        	}
        }
        return target.scale(1.0/mean,1.0/mean_var);
    }

    /* Support for Kakadu */
    
	public static DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}

	public static DataStructureType getDataStructureType() {
		return dataStructureType;
	}

	/* Port get/set methods -----------------------------------------*/
    
    /* In-Ports */
    
    public void setInPlot(Plot input) {
        inPlot = input;
        outPlot = inPlot;
    }

    /* Var-Ports (tuners) */
    
    public void setSkip(Boolean skip) {
        doForceSkip = skip;
    }

    public Boolean getSkip() {
        return doForceSkip;
    }
    
    public void setStop(Boolean stop) {
        doForceStop = stop;
    }
    
    public Boolean getStop() {
        return doForceStop;
    }

    public URI getSensitivityURI() {
        return sensitivityURI;
    }

    public void setSensitivityURI(URI sensitivityURI) {
        this.sensitivityURI = sensitivityURI;
    }

    /* Out-Ports */

    public Plot getOutPlot() {
        return outPlot;
    }

    public Plot getUirPlot() {
        return uirPlot;
    }

}

