/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *     Norman Xiong - debug and update
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.core;

import java.util.ArrayList;
import java.util.List;

import au.gov.ansto.bragg.kakadu.core.data.Operation;
import au.gov.ansto.bragg.kakadu.core.data.Operation.OperationSinkListener;
import au.gov.ansto.bragg.kakadu.core.data.OperationOptions;
import au.gov.ansto.bragg.kakadu.core.data.OperationParameter;
import au.gov.ansto.bragg.kakadu.core.data.OperationStatus;
import au.gov.ansto.bragg.kakadu.core.data.region.RegionOperationParameter;
import au.gov.ansto.bragg.process.agent.Agent;
import au.gov.ansto.bragg.process.agent.AgentListener;
import au.gov.ansto.bragg.process.agent.ProcessorAgent;
import au.gov.ansto.bragg.process.exception.ProcessorChainException;
import au.gov.ansto.bragg.process.port.Tuner;
import au.gov.ansto.bragg.process.processor.Processor_.ProcessorStatus;
import au.gov.ansto.bragg.process.processor.Sink;

/**
 * The class manages operations for an Algorithm.
 * 
 * @author Danil Klimontov (dak)
 */
public class OperationManager {

	private final List<Operation> operations = new ArrayList<Operation>();
	
	private boolean isLoaded = false;
	
	private final int dataItemIndex;

	private final int algorithmTaskId;
	
	public OperationManager(int algorithmTaskId, int dataItemIndex) {
		this.algorithmTaskId = algorithmTaskId;
		this.dataItemIndex = dataItemIndex;
	}

//	public void loadOperationList(List<Agent> agentList, RegionManager regionManager)
	public void loadOperationList(List<Agent> agentList)
	throws ProcessorChainException {
		if (isLoaded) {
			return;
		}
		
		int operationId = 0;
		for (Agent agent : agentList) {
			if (agent instanceof ProcessorAgent) {
				//create operation for each PprocessorAgent
				ProcessorAgent processorAgent = (ProcessorAgent) agent;
				
				final Operation operation = new Operation(processorAgent, operationId++);
				operation.setName(processorAgent.getName());
//				operation.setUILabel("O" + operation.getID() + " " + processorAgent.getLabel());
				operation.setUILabel(processorAgent.getLabel());
				operation.setStatus(OperationStatus.Ready);
				operation.setDataItemIndex(dataItemIndex);
				operation.setAlgorithmTaskId(algorithmTaskId);
				
				List<Sink> sinkList = processorAgent.getSinkList();
				if (sinkList != null && sinkList.size() > 0) {
					//use last sink to subscribe for final value
					Sink outputSink = sinkList.get(sinkList.size() - 1);
					
					operation.setDataType(Util.getDataType(outputSink));
//					outputSink.subscribe(new DataThread(outputSink, operation));
//					outputSink.subscribe(new OperationSinkListener(outputSink, operation));
					operation.createSinkListener(outputSink);
				}
				else{
					processorAgent.subscribe(new AgentListener(){

						public void onChange(Agent agent) {
							if (((ProcessorAgent) agent).getProcessorStatus() == ProcessorStatus.Done){
								operation.setActual(true);
								System.out.println("Processor status is done");
							}
						}});
				}

				List<Tuner> perameters = processorAgent.getParameters();
				for (Tuner tuner : perameters) {
					operation.addParameter(new OperationParameter(tuner));
				}
				
				List<Tuner> regions = processorAgent.getRegionTuners();
//				final RegionChangedListener regionChangedListener = new RegionChangedListener(operation);
				for (Tuner tuner : regions) {
//					final ParameterRegionManager parameterRegionManager = regionManager.getParameterRegionManager(operation.getName(), tuner.getName());
//					parameterRegionManager.setOperationUILabel(operation.getUILabel());
//					parameterRegionManager.setParameterUILabel(tuner.getLabel());
//					parameterRegionManager.setAlgorithmTaskId(algorithmTaskId);
					operation.addParameter(new RegionOperationParameter(tuner));
//					parameterRegionManager.addRegionListener(regionChangedListener);
				}
				
				final List<Tuner> options = processorAgent.getOptions();
				final OperationOptions operationOptions = operation.getOptions();
				for (Tuner tuner : options) {
					final String tunerName = tuner.getName().toLowerCase();
					if (tunerName.endsWith("skip") ) {
						operationOptions.setSkipTuner(tuner);
					} else if (tunerName.endsWith("enable")) {
						operationOptions.setEnableTuner(tuner);
					} else if (tunerName.endsWith("stop")) {
						operationOptions.setStopAfterCompleteTuner(tuner);
					}
				}
				
				
				operations.add(operation);
			}
			
		}
		
		isLoaded = true;
	}
	
	public boolean isLoaded() {
		return isLoaded;
	}
	
	public List<Operation> getOperations() {
		return new ArrayList<Operation>(operations);
	}
	
//	private final class RegionChangedListener implements RegionListener {
//		private Operation operation;
//		
//		public RegionChangedListener(Operation operation) {
//			this.operation = operation;
//		}
//
//		public void regionAdded(UIRegion region) {
//			operation.updateStatus();
//		}
//
//		public void regionRemoved(UIRegion region) {
//			operation.updateStatus();
//		}
//
//		public void regionUpdated(UIRegion region) {
//			operation.updateStatus();
//		}
//	}

	/**
	 * 
	 * @author nxi
	 * Created on 22/09/2008
	 * @deprecated use {@link OperationSinkListener instead
	 */
//	public static class DataThread extends Thread {
//
//		private Operation operation;
//		private Sink sink;
//		
//		public DataThread(Sink sink, Operation operation) {
//			this.sink = sink;
//			this.operation = operation;
//		}
//
//		public synchronized void run(){
//			while (true) {
//				System.out.println("Data thread for sink '" + sink.getName() + "' is waiting for data");
//
//				try {
//					this.wait();
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}				
//
//				System.out.println("Data thread for sink '" + sink.getName() + "' thread awakened");
//				if (sink.getSignal() instanceof Group)
//					operation.setOutputData((Group)sink.getSignal());
//			}
//		}
//	}

	public Operation getNextOperation(String operationName) {
		final int currentIndex = getOperationIndex(operationName);
		if (currentIndex >= 0 && operations.size() - 1 > currentIndex) {
			return operations.get(currentIndex + 1);
		}
		return null;
	}

	public int getOperationIndex(String operationName) {
		for (int i = 0; i < operations.size(); i++) {
			Operation operation = operations.get(i);
			if (operation.getName().equals(operationName)) {
				return i;
			}
			
		}
		return -1;
	}

	public int getOperationIndex(Operation operation) {
		return operations.indexOf(operation);
	}

	public Operation getPreviousOperation(String operationName) {
		final int currentIndex = getOperationIndex(operationName);
		if (currentIndex >= 1) {
			return operations.get(currentIndex - 1);
		}
		return null;
	}

	public Operation getOperation(String operationName) {
		for (Operation operation : operations) {
			if (operation.getName().equals(operationName)) {
				return operation;
			}
		}
		return null;
	}

	public Operation getOperation(int operationIndex) {
		return operations.get(operationIndex);
	}

	public void dispose(){
		if (operations != null){
			for (Operation operation : operations)
				operation.dispose();
			operations.clear();
		}
	}
}
