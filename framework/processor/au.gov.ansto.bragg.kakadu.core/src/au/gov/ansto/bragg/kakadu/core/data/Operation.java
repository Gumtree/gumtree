/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *     Norman Xiong - implementation and update
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.core.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.kakadu.core.Util;
import au.gov.ansto.bragg.process.agent.Agent;
import au.gov.ansto.bragg.process.agent.AgentListener;
import au.gov.ansto.bragg.process.agent.ProcessorAgent;
import au.gov.ansto.bragg.process.exception.ProcessFailedException;
import au.gov.ansto.bragg.process.exception.ProcessorChainException;
import au.gov.ansto.bragg.process.processor.Sink;
import au.gov.ansto.bragg.process.processor.SinkListener;

/**
 * The class describes algorithm's operation.
 * Use operation options to control operation execution and 
 * actions after execution. Use operation parameters to specify 
 * data parameters for the operation.
 * 
 * @author Danil Klimontov (dak)
 */
public class Operation {

	private Agent agent;
	private String name;
	private String uiLabel;
	private String description;
	
	private IGroup outputData;
	private DataType dataType = DataType.Undefined;
	
	private boolean isActual = false;

	private final OperationOptions options = new OperationOptions();
	/**
	 * Parameters for the operation. Key - parameterName, Value - OperationParameter object.
	 */
	private final Map<String, OperationParameter> parameters = new LinkedHashMap<String, OperationParameter>();
	
	private OperationStatus operationStatus;
	
	private final List<OperationDataListener> dataListeners = new ArrayList<OperationDataListener>();
	private final List<OperationStatusListener> statusListeners = new ArrayList<OperationStatusListener>();
	private int dataItemIndex;
	private final int id;
	private int algorithmTaskId;
	private boolean operationDataListenerEnabled = true;
	private List<OperationSinkListener> sinkListeners = new ArrayList<OperationSinkListener>();
	
	public Operation(Agent agent, int id) {
		this.agent = agent;
		this.id = id;
	}

	public int getID() {
		return id;
	}


	public Agent getAgent() {
		return agent;
	}

	public boolean addOperationDataListener(OperationDataListener operationDataListener) {
		if (operationDataListener != null) {
			synchronized (dataListeners) {
				return dataListeners.add(operationDataListener);
			}
		}		
		return false;
	}
	
	public boolean removeOperationDataListener(OperationDataListener operationDataListener) {
		synchronized (dataListeners) {
			return dataListeners.remove(operationDataListener);
		}
	}
	
	public Iterator<OperationDataListener> getOperationDataListeners() {
		synchronized (dataListeners) {
			return dataListeners.iterator();
		}
	}
	
	protected void fireOperationDataListeners(IGroup oldData, IGroup newData) {
		synchronized (dataListeners ) {
			for (OperationDataListener operationDataListener : dataListeners) {
				operationDataListener.outputDataUpdated(this, oldData, newData);
			}
		}
	}

	public void dispose(){
		agent = null;
		outputData = null;
//		options = null;
		parameters.clear();
		for (SinkListener listener : sinkListeners)
			((OperationSinkListener) listener).dispose();
		sinkListeners.clear();
		dataListeners.clear();
		statusListeners.clear();
	}
	
	public boolean addOperationStatusListener(OperationStatusListener operationStatusListener) {
		if (operationStatusListener != null) {
			return statusListeners.add(operationStatusListener);
		}		
		return false;
	}
	
	public boolean removeOperationStatusListener(AgentListener operationStatusListener) {
		return statusListeners.remove(operationStatusListener);
	}
	
	public Iterator<OperationStatusListener> getOperationStatusListeners() {
		return statusListeners.iterator();
	}
	
	protected void fireOperationStatusListeners(OperationStatus oldOperationStatus, OperationStatus newOperationStatus) {
		for (OperationStatusListener operationStatusListener : statusListeners) {
			operationStatusListener.statusUpdated(this, oldOperationStatus, newOperationStatus);
//			operationStatusListener.onChange(agent);
		}
	}

	/**
	 * Gets current operation's status.
	 * @return OperationStatus value.
	 */
	public OperationStatus getStatus() {
		return operationStatus;
	}

	/**
	 * Sets operation status.
	 * All regestred <code>OperationStatusListener</code>s will be fired with new status.
	 * @param operationStatus new operation status.
	 */
	public void setStatus(OperationStatus operationStatus) {
		if (getOptions().isSkipped() 
				&& operationStatus == OperationStatus.InProgress) {
			
			return;
		}
		OperationStatus oldStatus = this.operationStatus;
		this.operationStatus = operationStatus;
		fireOperationStatusListeners(oldStatus, operationStatus);
	}

	/**
	 * Gets current output data.
	 * @return output data
	 */
	public IGroup getOutputData() {
		return outputData;
	}

	/**
	 * Sets new output data for the operation. 
	 * All registered <code>OperationDataListener</code>s will be fired with new data object.
	 * @param outputData new output data object.
	 */
	public void setOutputData(final IGroup outputData) {
		System.out.println(">>Output data for operation '" + getName() + "' updated.");
		
		final IGroup oldData = this.outputData;
		this.outputData = outputData;

		if (dataType.equals(DataType.Undefined)) {
			//TODO get DataType from cicada instead of parsing the data object
			try{
				dataType = Util.getDataType(outputData);
			}catch (Exception e) {
				dataType = DataType.Undefined;
			}
		}
		if (dataType != DataType.Undefined){
			if (operationDataListenerEnabled)
				fireOperationDataListeners(oldData, outputData);
			setActual(true);
			updateStatus();
		}
	}
	
	public DataType getDataType() {
		return dataType;
	}
	
	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	public Iterator<String> getParameterNames() {
		return parameters.keySet().iterator();
	}

	public List<OperationParameter> getParameters() {
		return new ArrayList<OperationParameter>(parameters.values());
	}
	
//	public List<OperationParameter> getAllTuners(){
//		
//	}
	public OperationParameter getOperationParameter(String parameterName) {
		return parameters.get(parameterName);
	}

//	public void setParameterValue(String name, Object value) throws ParameterNotFoundException {
//		OperationParameter parameter = this.parameters.get(name);
//		if (parameter == null) {
//			throw new ParameterNotFoundException("Parameter '" + name + "' is not defined for '" +
//					this.name + "' operation.");
//		}
//		
//		Object oldValue = parameter.getValue();
//		boolean oldValueIsNull = oldValue == null;
//		boolean newValueIsNull = value == null;
//		if (oldValueIsNull && !newValueIsNull ||
//				!oldValueIsNull && newValueIsNull ||
//				!oldValueIsNull && !newValueIsNull && !oldValue.equals(value)) {
//			parameter.setValue(value);
//			setStatus(OperationStatus.Modified);
//		}		
//	}
//	
//	public Object getParameterValue(String name) throws ParameterNotFoundException {
//		IOperationParameter operationParameter = parameters.get(name);
//		if (operationParameter == null) {
//			throw new ParameterNotFoundException("Parameter '" + name + "' is not defined for '" +
//					this.name + "' operation.");
//		}
//		return operationParameter.getValue();
//	}
//	
//	public Object getDefaultParameterValue(String name) throws ParameterNotFoundException {
//		IOperationParameter operationParameter = parameters.get(name);
//		if (operationParameter == null) {
//			throw new ParameterNotFoundException("Parameter '" + name + "' is not defined for '" +
//					this.name + "' operation.");
//		}
//		return operationParameter.getDefaultValue();
//	}
	
	public OperationParameter addParameter(OperationParameter parameter) {
		return parameters.put(parameter.getName(), parameter);
	}

	public void resetParametersToDefault() {
		for (OperationParameter parameter : parameters.values()) {
			parameter.loadDefaultValue();
		}
	}
	
	public void informParametersChanged(){
		for (OperationParameter parameter : parameters.values()) {
			parameter.resetDefault();
		}
	}
	
	public void revertParametersChanges() {
		for (OperationParameter parameter : parameters.values()) {
			parameter.revertChanges();
		}
	}
	
	public void saveParametersChanges() 
	throws ProcessorChainException, ProcessFailedException, IllegalAccessException {
		for (OperationParameter parameter : parameters.values()) {
			parameter.saveChanges();
		}
	}
	
	public boolean isParametersChanged() {
		for (OperationParameter parameter : parameters.values()) {
			if (parameter.isChanged()) {
				return true;
			}
		}
		return false;
	}

	public boolean isDefaultParametersLoaded() {
		for (OperationParameter parameter : parameters.values()) {
			if (!Util.areEqual(parameter.getDefaultValue(), parameter.getValue())) {
				return false;
			}
		}
		return true;
	}
	

	public void updateStatus() {
		if (getOptions().isSkipped()) {
			setStatus(OperationStatus.Skipped);
		} else if (isParametersChanged()) {
			setStatus(OperationStatus.Modified);
		} else if (outputData != null
//				&& isActual
				) {
			setStatus(OperationStatus.Done);
		} else {
			setStatus(OperationStatus.Ready);
		}
	}

	/**
	 * Gets description of the operation.
	 * @return operation's description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets description of the operation.
	 * @param description operation's description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets name of the operation.
	 * @return operation's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets name of the operation.
	 * @param name operation's name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	
	/**
	 * Gets UI label for the operation.
	 * @return text to represent the object on UI
	 */
	public String getUILabel() {
		return uiLabel;
	}

	/**
	 * Sets UI label for the operation.
	 * @param uiLabel text to represent the object on UI
	 */
	public void setUILabel(String uiLabel) {
		this.uiLabel = uiLabel;
	}

	public OperationOptions getOptions() {
		return options;
	}
	
	/**
	 * Detects whether masking is enabled for the operation or not.
	 * Mask is enabled if the operation has any parameters with <code>Region</code> type. 
	 * @return true if mask is enabled or false otherwise.
	 */
	public boolean isMaskEnabed() {
		for (OperationParameter operationParameter : parameters.values()) {
			if (operationParameter.getType() == OperationParameterType.Region) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Sets actual flag for the operation.
	 * The operation can be NOT actual if output data was updated 
	 * for the operation with the same name on another data item.
	 * If the operation is NOT actual then algorithm should be executed again to obtain actual data.
	 * 
	 * @param isActual true if actual or false otherwise.
	 */
	public void setActual(boolean isActual) {
		this.isActual = isActual;
	}

	/**
	 * Gets <code>actual</code> flag for the operation.
	 * @return true if actual or false otherwise.
	 * @see #setActual(boolean)
	 */
	public boolean isActual() {
		return isActual;
	}

	/**
	 * Sets data item index for the operation.
	 * @param dataItemIndex data item index.
	 */
	public void setDataItemIndex(int dataItemIndex) {
		this.dataItemIndex = dataItemIndex;
	}

	public int getDataItemIndex() {
		return dataItemIndex;
	}

	public void setAlgorithmTaskId(int algorithmTaskId) {
		this.algorithmTaskId = algorithmTaskId;
	}
	
	public int getAlgorithmTaskId() {
		return algorithmTaskId;
	}
	
	public void updateParameters(){
		for (OperationParameter parameter : parameters.values()) {
			parameter.updateValue();
		}
	}
	
	public boolean hasAutoPlotSink(){
		List<Sink> autoPlotSinkList = ((ProcessorAgent) agent).getAutoPlotSinkList(); 
		if (autoPlotSinkList != null && autoPlotSinkList.size() > 0)
			return true;
		return false;
	}
	
	public boolean isReprocessable(){
		return ((ProcessorAgent) agent).isReprocessable();
	}

	/**
	 * @return the operationDataListenerEnabled
	 */
	public boolean isOperationDataListenerEnabled() {
		return operationDataListenerEnabled;
	}

	/**
	 * @param operationDataListenerEnabled the operationDataListenerEnabled to set
	 */
	public void setOperationDataListenerEnabled(boolean operationDataListenerEnabled) {
		System.err.println("enable plot " + getName());
		this.operationDataListenerEnabled = operationDataListenerEnabled;
		if (operationDataListenerEnabled)
			fireOperationDataListeners(null, outputData);
	}

	public void createSinkListener(Sink outputSink) {
		OperationSinkListener listener = new OperationSinkListener(outputSink);
		outputSink.subscribe(listener);
		sinkListeners.add(listener);
	}
	
	public class OperationSinkListener implements SinkListener{

		private Sink sink;
		public OperationSinkListener(Sink sink){
			this.sink = sink;
		}
		
		public void onChange() {
			System.out.println("Sink '" + sink.getName() + "' signal changed");
			setActual(true);
			if (sink.getSignal() instanceof IGroup)
				setOutputData((IGroup)sink.getSignal());
		}
		
		public void dispose(){
			sink.unsubscribe(this);
		}
	}
	
}
