/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Paul Hathaway - June 2009
*******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.online;

import au.gov.ansto.bragg.quokka.dra.online.util.ResultRecord;
import au.gov.ansto.bragg.quokka.experiment.result.ExperimentResult;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.*;

import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

public class Parser extends ConcreteProcessor {
	
	private static final String processClass = "Parser"; 
	private static final String processClassVersion = "1.0"; 
	private static final long processClassID = 2009061701; 

	private final static DataStructureType dataStructureType = DataStructureType.undefined;
	private final static DataDimensionType dataDimensionType = DataDimensionType.undefined;
    private Boolean isDebugMode = true;

	private Boolean inLoop = true;
	private Boolean outLoop = true;
	private Boolean doForceStop = false;
	
	private Boolean hasModel = false;
	private Boolean isModelNew = false;
	
	ExperimentResult model;
	ResultRecord result;
	
	public Boolean process() throws Exception {

    	this.outLoop = isVarsSet();
    	
		if (!outLoop) {
			//stampProcessSkip(outPlot);			
		} else { // hasModel and isModelNew = true
			outLoop = processModel();
			isModelNew = false;  // reset for next model change
			if (isDebugMode) {
				System.out.println("Model change processed: "+isModelNew);
			}
			//stampProcessEnd(outPlot);
		}
		return doForceStop;
	}
	
	/** 
	 * @return true if required inputs are set
	 */
	private Boolean isVarsSet() {
		Boolean ok = hasModel && isModelNew;
		return ok;
	}	
	

	private boolean processModel() {
		/**
		 * Use Case 1: Process a single measurement for a sample at one configuration 
		 */		
		ResultRecord result = new ResultRecord(model);		
		return (null!=result);
	}
	
	
	/* Client Support methods -----------------------------------------*/
	
	public static DataStructureType getDataStructureType() {
		return dataStructureType;
	}

	public static DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}

	/* Port get/set methods -----------------------------------------*/
    
    /* In-Ports -----------------------------------------------------*/

	public void setInLoop(Boolean loop) {
		this.inLoop = loop;
	}

	public void setHasModel(Boolean hasModel) {
		this.hasModel = hasModel;
	}

	public void setIsModelNew(Boolean isModelNew) {
		this.isModelNew = isModelNew;
	}

	public void setModel(ExperimentResult model) {
		this.model = model;
	}

    /* Out-Ports ----------------------------------------------------*/

	public Boolean getOutLoop() {
		return this.outLoop;
	}    

	public ExperimentResult getModel() {
		return model;
	}

	public ResultRecord getResult() {
		return result;
	}
	
	/* Var-Ports (options) ------------------------------------------*/

	public Boolean getStop() {
		return doForceStop;
	}

	public void setStop(Boolean doForceStop) {
		this.doForceStop = doForceStop;
	}

    /* Var-Ports (tuners) -------------------------------------------*/
	
}
