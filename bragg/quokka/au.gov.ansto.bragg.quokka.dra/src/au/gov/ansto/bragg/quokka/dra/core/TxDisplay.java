/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Paul Hathaway - February 2009
*******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.core;

import java.io.IOException;

import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IDataItem;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

public class TxDisplay extends ConcreteProcessor {
	
	private static final String processClass = "TxDisplay"; 
	private static final String processClassVersion = "1.0"; 
	private static final long processClassID = 2009020402; 

	private final static DataStructureType dataStructureType = DataStructureType.plot;
	private final static DataDimensionType dataDimensionType = DataDimensionType.map;
    private Boolean isDebugMode = true;

	private Plot inPlot;
	private Plot outPlot;
	private Plot txFieldPlot;
	private Double txFactor = 1.0;
	private Double wavelength = 5.0;
	private Boolean doForceSkip = false;
	private Boolean doForceStop = false;
	
	private final static String KEY_WAVELENGTH = "LambdaA";
	private final static String KEY_TRANSMISSION = "Transmission";    

	public Boolean process() throws Exception {

    	this.outPlot = this.inPlot;
    	stampProcessLog();
    	if(null==txFieldPlot) {
    		this.txFieldPlot = this.inPlot;
    	}
    	
		if (!(this.doForceSkip)) {
			fetchParameters();
			publishParameters();
		} else {
			stampProcessSkip();
		}
		return doForceStop;
	}

	private void stampProcessLog() {
		((NcGroup) outPlot).setLocation(inPlot.getLocation());
		outPlot.addProcessingLog("ProcessClass/Version/ID: ["
				+processClass+";"
				+processClassVersion+";"
				+processClassID+"]");		
	}
	
	private void stampProcessSkip() {
		((NcGroup) outPlot).setLocation(inPlot.getLocation());
		outPlot.addProcessingLog("["+processClassID+"]:"+"SKIP process");		
	}
	
	private void fetchParameters() throws IOException, Exception 
	{
		//TODO: Update txFieldPlot
		
		IDataItem wavelengthItem = (IDataItem) inPlot.getContainer(KEY_WAVELENGTH);
		this.wavelength = (double) wavelengthItem.readScalarFloat();

		IDataItem txFactorItem = (IDataItem) inPlot.getContainer(KEY_TRANSMISSION);
		this.txFactor = (double) txFactorItem.readScalarFloat();

	}
	
	private void publishParameters() {
		//TODO: Update txFieldPlot
		super.informVarValueChange("txFactor", txFactor);
		super.informVarValueChange("wavelength", wavelength);
	}
	
	/* Client Support methods -----------------------------------------*/
	
	public static DataStructureType getDataStructureType() {
		return dataStructureType;
	}

	public static DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}

	public void setIsDebugMode(Boolean isDebugMode) {
		this.isDebugMode = isDebugMode;
	}

	public Boolean getIsDebugMode() {
		return isDebugMode;
	}	
	
	/* Port get/set methods -----------------------------------------*/
    
    /* In-Ports */

	public void setInPlot(Plot inPlot) {
		this.inPlot = inPlot;
	}

	public void setTxFieldPlot(Plot txFieldPlot) {
		this.txFieldPlot = txFieldPlot;
	}

    /* Out-Ports */

	public Plot getOutPlot() {
		return this.outPlot;
	}    

	public Plot getTxFieldPlot() {
		return this.txFieldPlot;
	}

	public Double getTxFactor() {
		return this.txFactor;
	}

	public Double getWavelength() {
		return wavelength;
	}

    /* Var-Ports (tuners) */


	public void setTxFactor(Double txFactor) {
		this.txFactor = txFactor;
	}

	public void setWavelength(Double wavelength) {
		this.wavelength = wavelength;
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
