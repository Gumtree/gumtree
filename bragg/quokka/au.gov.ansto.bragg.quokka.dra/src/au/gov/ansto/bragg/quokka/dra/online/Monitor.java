/******************************************************************************* 
* Copyright (c) 2009 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Paul Hathaway - June 2009
*******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.online;

import org.gumtree.core.service.ServiceUtils;
import org.gumtree.service.directory.IDirectoryService;
import org.gumtree.util.messaging.EventHandler;
import org.osgi.service.event.Event;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;
import au.gov.ansto.bragg.quokka.experiment.result.ExperimentResult;

public class Monitor extends ConcreteProcessor {
	
	private static final String processClass = "Monitor"; 
	private static final String processClassVersion = "1.0"; 
	private static final long processClassID = 2009061601; 

	private final static DataStructureType dataStructureType = DataStructureType.undefined;
	private final static DataDimensionType dataDimensionType = DataDimensionType.undefined;
    private Boolean isDebugMode = true;

    private final static long MAXTIMEOUT = 5 * 24 * 3600 * 1000L; // 5 days
    private final static String COUNTER = "experimentparser.counter";     
    
	private Boolean inLoop = true;
	private Boolean outLoop = true;
	private Boolean doForceStop = false;

	private Boolean isServiced = false;
	private Boolean hasModel = false;
	private Boolean isModelNew = false;
	private Boolean isTimeout = false;
	private EventHandler eventHandler;
	
	private Long updateInterval = 1000L; // milliseconds
	private Long modelTimeout   = 5 * 60 * 1000L; //  5min in milliseconds
	private Long pollTimeout   = 60 * 60 * 1000L; // 60min in milliseconds
	private Long counter       = 0L;
	
	IDirectoryService service;
//	IDirectoryServiceListener listener; // listener to ExperimentResult
	ExperimentResult model;
	
	public Monitor() {
		super();
		this.setProcessClass(processClass);
		this.setProcessClassVersion(processClassVersion);
		this.setProcessClassID(processClassID);
	}
	
	public Boolean process() throws Exception {
		isTimeout = false;
		if (this.doForceStop  || !(this.inLoop)) {
			//stampProcessSkip(outPlot);			
		} else {
			if (null==eventHandler) {
				isServiced = findService();
				if (isDebugMode) { 
					System.out.println("Monitor: Directory Service found?: "+isServiced);
				}
			} 
			
			if (!isServiced) {
				doForceStop = true;
			} else {
				// if model not initialised, attempt to fetch model now
				// TODO: Decision: Can use modelTimeout to wait for model acquisition
				//       or delegate watch dog to higher module
				if (!hasModel) {
					model = service.lookup(ExperimentResult.class.getName(),ExperimentResult.class);
					hasModel = (null!=model);
					isModelNew = hasModel;
				}
				// poll Directory at updateInterval
				counter = 0L;
				Long interval = 0L;
				while (!(isModelNew || isTimeout)) {
					try{
						Thread.sleep(updateInterval);
						interval += updateInterval;
						isTimeout = (interval>pollTimeout) || (MAXTIMEOUT<interval);
						counter++;
					}catch (InterruptedException e) {
						if (isDebugMode) {
							System.out.println("Monitor: Cannot monitor Directory Service");
						}
						doForceStop = true;
					}
				}
			}
			//stampProcessEnd(outPlot);
		}
    	this.outLoop = this.inLoop && !doForceStop;    	
		return doForceStop;
	}
	
	private Boolean findService() {
		// Obtains the directory service
		this.service = ServiceUtils.getService(IDirectoryService.class);
		Boolean isServiceOK = (null!=service);
		this.hasModel = false;
		this.isModelNew = false;

		if (isServiceOK) {
			eventHandler = new EventHandler(IDirectoryService.EVENT_TOPIC_BIND) {
				@Override
				public void handleEvent(Event event) {
					if (event.getProperty(IDirectoryService.EVENT_PROP_NAME).equals(ExperimentResult.class.getName())) {
						model = (ExperimentResult) event.getProperty(IDirectoryService.EVENT_PROP_OBJECT);
						hasModel = (null!=model);
						isModelNew = hasModel;
					}
				}
			}.activate();
		}		
		return isServiceOK;
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

	public void setInLoop(Boolean loop) {
		this.inLoop = loop;
	}

    /* Out-Ports ----------------------------------------------------*/

	public Boolean getOutLoop() {
		return this.outLoop;
	}    

	public Boolean getHasModel() {
		return hasModel;
	}

	public Boolean getIsModelNew() {
		return isModelNew;
	}

	public Boolean getIsTimeout() {
		return isTimeout;
	}
	
	public ExperimentResult getModel() {
		return model;
	}

	/* Var-Ports (options) ------------------------------------------*/
	
	public Boolean getStop() {
		return doForceStop;
	}

	public void setStop(Boolean doForceStop) {
		this.doForceStop = doForceStop;
	}

    /* Var-Ports (tuners) -------------------------------------------*/
	
	public void setInterval(Long updateInterval) {
		this.updateInterval = updateInterval;
	}

	public void setTimeout(Long timeout) {
		this.pollTimeout = timeout;
	}
}
