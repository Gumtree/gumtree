/******************************************************************************* 
* Copyright (c) 2009 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Template source file for setting up processor block interfaces
* 
* Contributors: 
*    Paul Hathaway - February 2009
*******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.core;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.gumtree.data.interfaces.IArray;

import au.gov.ansto.bragg.quokka.dra.internal.Activator;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.*;
import au.gov.ansto.bragg.datastructures.core.exception.*;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.nexus.NexusUtils;

import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

public class _Template extends ConcreteProcessor {
	
	/* Fields for audit trail support */
	private static final String processClass = "Blank"; 
	private static final String processClassVersion = "1.0"; 
	private static final long processClassID = 2009022010; // yyyymmddnn

	/* Fields to support client-side processing */
	private final static DataStructureType dataStructureType = DataStructureType.plot;
	private final static DataDimensionType dataDimensionType = DataDimensionType.map;
    private Boolean isDebugMode = true;

    /* Input, Output and Option Var Ports*/
	private Plot inPlot;
	private Plot outPlot;
	private Boolean doForceSkip = false;
	private Boolean doForceStop = false;
	
	/* Key strings required to access dictionary values via Activator */
	//eg private final static String KEY_WAVELENGTH = "LambdaA";
	
	/* Var Ports for exposing tunable parameters - must have setters */
	private IArray a;

	public Boolean process() throws Exception {

    	this.outPlot = this.inPlot;
    	
    	stampProcessLog(outPlot);
    	
		if (this.doForceSkip) {
			stampProcessSkip(outPlot);			
		} else {
			/* TODO: Do interesting stuff here */
			/* TODO: customise switch for defined process */
			stampProcessEntry(outPlot, "log entry comment");
        	switch (inPlot.getDimensionType())
        	{
        		case map:
        		case mapset:
        			// is compatible for 2D processes
        			break;
        		case pattern:
        		case patternset:
        			// compatible for 1D processes
        			break;
        		case volume:
        		case volumeset:
        		case extended:
        		case text:
        		case tabular:
        		case undefined:
        		default:
        			throw new IllegalArgumentException(
        					"DataDimensionType of {map|mapset|pattern|patternset} required.");
        	}
			stampProcessEnd(outPlot);
		}
		return doForceStop;
	}

	/* Methods for audit trail support - plan to promote these to super class methods */
	private void stampProcessLog(Plot plot) {
		plot.addProcessingLog("ProcessClass/Version/ID: ["
				+processClass+";"
				+processClassVersion+";"
				+processClassID+"]");		
	}
	
	private void stampProcessSkip(Plot plot) {
		plot.addProcessingLog("["+processClassID+"]:"+"SKIP process");		
	}
	
	private void stampProcessEntry(Plot plot, String logEntry) {
		plot.addProcessingLog("["+processClassID+"]: "+logEntry);		
	}
	
	private void stampProcessEnd(Plot plot) {
		plot.addProcessingLog("["+processClassID+"]:"+"END process");		
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
    
    /* In-Ports -----------------------------------------------------*/

	public void setInPlot(Plot inPlot) {
		this.inPlot = inPlot;
	}

    /* Out-Ports ----------------------------------------------------*/

	public Plot getOutPlot() {
		return this.outPlot;
	}    

	/* Var-Ports (options) ------------------------------------------*/

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

    /* Var-Ports (tuners) -------------------------------------------*/


}
