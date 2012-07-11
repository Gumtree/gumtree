/******************************************************************************* 
* Copyright (c) 2009 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Paul Hathaway - April 2009
*    pvh modified July 2009
*    
*    Originally a prototype for monitoring and parsing the workflow model.
*    Planned to be a composite of the "Monitor" and "Parser" processor classes.
*    
*******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.core;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;
import au.gov.ansto.bragg.quokka.dra.online.Monitor;
import au.gov.ansto.bragg.quokka.dra.online.Parser;
import au.gov.ansto.bragg.quokka.dra.online.util.ResultRecord;
import au.gov.ansto.bragg.quokka.experiment.result.ExperimentResult;

public class ExperimentParser extends ConcreteProcessor {
	
	private static final String myProcessClass = "ExperimentParser"; 
	private static final String myProcessClassVersion = "2.0"; 
	private static final long   myProcessClassID = 2009070802; 
	
	private static final String INTERVAL_FIELD = "Interval";
	private static final String TIMEOUT_FIELD  = "Timeout";
	private static final String COUNTER  = "Counter";

	private final static DataStructureType dataStructureType = DataStructureType.undefined;
	private final static DataDimensionType dataDimensionType = DataDimensionType.undefined;

	private Boolean doForceStop = false;
    private Boolean isDebugMode = true;

    private Boolean inLoop = true;
    private Monitor monitor;
    private Parser  parser;
    private Long    counter = 0L;
    private Long    loopCounter = 0L;
    
	private ExperimentResult emptyER = new ExperimentResult();
	private ResultRecord     emptyRec= new ResultRecord(emptyER);
	
    private ResultRecord result;
	private Long updateInterval = 1000L; // milliseconds
	private Long pollTimeout   = 60 * 60 * 1000L; // 60min in milliseconds
	
	public ExperimentParser() {
		this.setProcessClass(myProcessClass);
		this.setProcessClassVersion(myProcessClassVersion);
		this.setProcessClassID(myProcessClassID);
		this.monitor = new Monitor();
		this.parser = new Parser();
		this.setReprocessable(true);
	}
	
	public Boolean process() throws Exception {

		if (false) {
			//stampProcessSkip(outPlot);			
		} else {
	    	/* do we need lazy initialisation? */
	    	if (null==monitor) { monitor = new Monitor(); }
	    	if (null==parser)  { parser  = new Parser(); }
	    	
	    	emptyER = new ExperimentResult();
	    	emptyRec= new ResultRecord(emptyER);
	    	
	    	/* prep Monitor */
	    	monitor.setInLoop(true);
	    	monitor.setIsDebugMode(this.getIsDebugMode());
	    	
	    	/* prep parser */
	    	parser.setStop(doForceStop);
	    	parser.setIsDebugMode(this.getIsDebugMode());
	    	
	    	loopCounter++;
	    	boolean doLoop = !(doForceStop);
	    	counter = 0L;
	    	
	    	while (doLoop) {
	    		counter++;
	    		super.informVarValueChange(COUNTER,counter);
	    		monitor.setStop(doForceStop);
		    	monitor.setInterval(this.updateInterval);
		    	monitor.setTimeout(this.pollTimeout);
		    	if (isDebugMode) {
		    		System.out.println("> Run Monitor ["+counter+"] for "+pollTimeout+"ms");
		    	}
		    	this.doForceStop = monitor.process();
	    		
		    	/* monitor process will time out or return with model */
		    	/* parse on no errors, no manual stop and new model available */ 
		    	
		    	this.doForceStop = this.doForceStop 
		    						|| !(monitor.getOutLoop());	    	
		    	boolean doParse = !(this.doForceStop) 
		    						&& monitor.getHasModel() 
		    						&& monitor.getIsModelNew(); 
		    	
		    	if (doParse) {
		    		parser.setStop(false);
		    		parser.setHasModel(monitor.getHasModel());
		    		parser.setIsModelNew(monitor.getIsModelNew());
		    		parser.setModel(monitor.getModel());
		    		parser.process();		    		
		    		result = parser.getResult();
		    	}		    	
		    	
		    	// set stop=true for error condition (outLoop=false)
	    		this.doForceStop = this.doForceStop || !(parser.getOutLoop());
	    		// exit loop on error condition, good result to process, or timeout
	    		doLoop = !(doForceStop) && (null==result) && !(monitor.getIsTimeout());
	    	}
		}
		return doForceStop;
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
	
	public void setInLoop(Boolean inLoop) {
		this.inLoop = inLoop;
	}
	
    /* Out-Ports ----------------------------------------------------*/

	public ResultRecord getResult() {
		if (null==this.result) {
			result = this.emptyRec;
		}
		return this.result;
	}    

	public ExperimentResult getModel() {
		ExperimentResult er = emptyER;
		if (null!=this.parser) {
			er = this.parser.getModel();
			if (null==er) {
				er = emptyER;
			}
		}
		return er;
	}
	
	/* Var-Ports (options) ------------------------------------------*/

	public void setCounter(Long ctr) {
		this.counter = ctr;
		super.informVarValueChange(COUNTER, counter);
	}
	
	public void setStop(Boolean doForceStop) {
		this.doForceStop = doForceStop;
	}

    /* Var-Ports (tuners) -------------------------------------------*/

	public void setInterval(Long updateInterval) {
		this.updateInterval = updateInterval;
		super.informVarValueChange(INTERVAL_FIELD, updateInterval);
	}
	
	public void setTimeout(Long pollTimeout) {
		this.pollTimeout = pollTimeout;
		super.informVarValueChange(TIMEOUT_FIELD, pollTimeout);
	}	
}
