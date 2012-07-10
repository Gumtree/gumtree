/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.core;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.impl.netcdf.NcDataItem;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.cicada.core.Algorithm;
import au.gov.ansto.bragg.cicada.core.Algorithm.AlgorithmStatus;
import au.gov.ansto.bragg.cicada.core.AlgorithmInput;
import au.gov.ansto.bragg.cicada.core.AlgorithmManager;
import au.gov.ansto.bragg.cicada.core.AlgorithmStatusListener;
import au.gov.ansto.bragg.cicada.core.ThreadExceptionHandler;
import au.gov.ansto.bragg.cicada.core.exception.ConfigurationException;
import au.gov.ansto.bragg.cicada.core.exception.FailedToExecuteException;
import au.gov.ansto.bragg.cicada.core.exception.LoadAlgorithmFileFailedException;
import au.gov.ansto.bragg.cicada.core.exception.NoneAlgorithmException;
import au.gov.ansto.bragg.cicada.core.exception.SetTunerException;
import au.gov.ansto.bragg.cicada.core.exception.TransferFailedException;
import au.gov.ansto.bragg.cicada.core.exception.TunerNotReadyException;
import au.gov.ansto.bragg.kakadu.core.data.DataItem;
import au.gov.ansto.bragg.kakadu.core.data.DataType;
import au.gov.ansto.bragg.kakadu.core.data.Operation;
import au.gov.ansto.bragg.kakadu.core.data.OperationDataListener;
import au.gov.ansto.bragg.kakadu.core.data.OperationParameter;
import au.gov.ansto.bragg.kakadu.core.data.OperationParameterType;
import au.gov.ansto.bragg.kakadu.core.data.OperationStatus;
import au.gov.ansto.bragg.process.agent.Agent;
import au.gov.ansto.bragg.process.exception.ProcessorChainException;

public class AlgorithmTask {

	private static int idCounter; 
	private int id;
	private Algorithm algorithm;
	private List<DataItem> dataItems;
	private List<AlgorithmInput> algorithmInputs;
	private List<OperationManager> operationManagers = new ArrayList<OperationManager>();
//	private final RegionManager regionManager = new RegionManager();
	private URI fileUri;
	private int selectedDataItemIndex = 0;
	private final Object locker = new Object();
	private List<AlgorithmTaskStatusListener> statusListenerList;
	private Object regionParameterManager;
	
	public AlgorithmTask() {
		id = idCounter++;
	}
	
	public int getId() {
		return id;
	}

	/**
	 * Loads the AlgorithmTask inner data structures. 
	 * @param algorithm
	 * @param dataItems
	 * @throws LoadAlgorithmFileFailedException
	 * @throws ConfigurationException 
	 */
	public void load(Algorithm algorithm, List<DataItem> dataItems) 
	throws LoadAlgorithmFileFailedException, ConfigurationException {
		this.algorithm = algorithm;
		this.dataItems = dataItems;
		List<IGroup> dataList = new ArrayList<IGroup>();
		for (int i = 0; i < dataItems.size(); i++) {
			final DataItem dataItem = dataItems.get(i);
			dataList.add(dataItem.getDataObject());
			
			//create empty OperationManager for each DataItem
			//loading should be performed before first usage. see #loadOperationManager(index) method.
			operationManagers.add(new OperationManager(id, i));
		}
//		if (dataItems.size() == 0) operationManagers.add(new OperationManager(id, -1));
		//Initialize AlgorithmInput for each dataItem + algorithm to refer to cicada framework
		algorithmInputs = UIAlgorithmManager.getAlgorithmManager().loadAlgorithmOnMultipleData(
				dataList, algorithm);
		for (AlgorithmInput algorithmInput : algorithmInputs){
			algorithmInput.getAlgorithm().addStatusListener(
					new AlgorithmStatusListener(){

						public void onStatusChanged(AlgorithmStatus status) {
							fireAlgorithmStatusChangedAction(status);
						}

						public void setStage(int operationIndex, AlgorithmStatus status) {
							fireAlgorithmStageChangedAction(operationIndex, status);
						}

					});
		}
	}

	private void fireAlgorithmStageChangedAction(
			int operationIndex, AlgorithmStatus status) {
		if (statusListenerList != null)
			for (AlgorithmTaskStatusListener listener : statusListenerList){
				listener.setStage(operationIndex, status);
			}
	}

	protected void fireAlgorithmStatusChangedAction(AlgorithmStatus status) {
		if (statusListenerList != null){
			for (AlgorithmTaskStatusListener listener : statusListenerList){
				listener.onChange(status);
			}
		}
	}

	public void registerExceptionHandler(ThreadExceptionHandler handler) {
//		UIAlgorithmManager.getAlgorithmManager().getCurrentInput().subscribeExceptionHandler(
		for (AlgorithmInput input : algorithmInputs){
			input.subscribeExceptionHandler(handler);
		}
	}

	public void setSelectedDataItem(int index) {
//		if (selectedDataItemIndex == index) {
//			return;
//		}
		AlgorithmManager algorithmManager = UIAlgorithmManager.getAlgorithmManager();
		
		algorithmManager.setCurrentInput(algorithmInputs.get(index));
		
		selectedDataItemIndex = index;
		
		fireSelectedDataItemIndexChangedEvent();
	}
	
	/**
	 * Gets Operation manager for the data item index.
	 * Each OperationManager must be loaded before first use by {@link #loadOperationManager(int)} method.
	 * @param index data item index
	 * @return OpearionManager object
	 * @throws IndexOutOfBounds if Algorithm task does not have the data index in a scope
	 */
	public OperationManager getOperationManager(int index) {
//		if (index >= operationManagers.size()) return null;
		OperationManager operationManager = operationManagers.get(index);
		return operationManager;
			
	}

	/**
	 * Loads an OperationManager by parsing of current algorithm. 
	 * @param index data item index.
	 * @throws ProcessorChainException 
	 * @throws NoneAlgorithmException 
	 */
	public void loadOperationManager(int index) throws ProcessorChainException, NoneAlgorithmException {
		OperationManager operationManager = operationManagers.get(index);
		if (!operationManager.isLoaded()) {
			synchronized (locker) {
				AlgorithmManager algorithmManager = UIAlgorithmManager.getAlgorithmManager();
				final AlgorithmInput currentInput = algorithmManager.getCurrentInput();
				final AlgorithmInput algorithmInput = algorithmInputs.get(index);
				
				if (currentInput != algorithmInput) {
					algorithmManager.setCurrentInput(algorithmInput);
				}
				List<Agent> agentList = algorithmManager.getAgentList();
//				operationManager.loadOperationList(agentList, getRegionManager());
				operationManager.loadOperationList(agentList);
				
				//register Actual data flag updater
				//if data has been received by one of an operation 
				//then all operations with the same name for other data items 
				//must be marked as not Actual  
//				final List<Operation> operations = operationManager.getOperations();
//				for (Operation operation : operations) {
//					operation.addOperationDataListener(actualOperationDataListener);
//				}
				
				if (currentInput != algorithmInput) {
					algorithmManager.setCurrentInput(currentInput);
				}
			}
		}
	}
	
	public List<DataItem> getDataItems() {
		return dataItems;
	}
	
	public int getDataItemsCount() {
		return dataItems.size();
	}

	public int getSelectedDataItemIndex() {
		return selectedDataItemIndex;
	}
	
	/**
	 * Gets operation DataType by detecting data type 
	 * of the same operation but for another Data Item.
	 * This method useful when 
	 * @param operationIndex index of operation (the same as operation Id)
	 * @return specified DataType or DataType.Undefined if data type is undefined.
	 */
	public DataType getOperationDataType(int operationIndex) {
		
		for (OperationManager operationManager : operationManagers) {
			final Operation operation = operationManager.getOperation(operationIndex);
			if (operation.getDataType() != DataType.Undefined) {
				return operation.getDataType();
			}
		}
		return DataType.Undefined;
	}
	
	
	private void setOperationsNotActual(String operationName) {
		for (OperationManager operationManager : operationManagers) {
			final Operation operation = operationManager.getOperation(operationName);
			if (operation != null) {
				operation.setActual(false);
			}
		}
	}

	/**
	 * Runs current algorithm from the first operation for selected data item.
	 * @throws NoneAlgorithmException
	 */
	public void runAlgorithm() throws NoneAlgorithmException {
		System.out.println(">>Execute current algorithm and current DataItem...");
		
		for (Operation operation : getOperationManager(getSelectedDataItemIndex()).getOperations()) {
//			operation.setStatus(OperationStatus.InProgress);
			setOperationsNotActual(operation.getName());
		}
		
//		UIAlgorithmManager.getAlgorithmManager().execute();
		UIAlgorithmManager.getAlgorithmManager().execute(algorithmInputs.get(
				selectedDataItemIndex).getAlgorithm());
	}

	/**
	 * Runs current algorithm from the operation for selected data item.
	 * @param operation an operation to start
	 * @throws TunerNotReadyException
	 * @throws TransferFailedException
	 */
	public void runAlgorithmFromOperation(Operation operation ) throws TunerNotReadyException, 
	TransferFailedException {
		System.out.println(">>Run algorithm:" + algorithm.getName() + " from Operation: " + operation.getName() + " for DataItem:" + getSelectedDataItemIndex() +":"+dataItems.get(getSelectedDataItemIndex()).getName());

		
		List<Operation> operations = getOperationManager(getSelectedDataItemIndex()).getOperations();
		Operation lastReprocessable = getOperationChainHead(operations);
		final int index = operations.indexOf(operation);
		
//		for (int i = index; i < operations.size(); i++) {
//			final Operation op = operations.get(i);
//			op.setStatus(OperationStatus.InProgress);
////			setOperationsNotActual(op.getName());
//		}
		if (index <= 0) {
			//if the operation is the first in the chain then execute() method must be used to proceed. 
			try {
				UIAlgorithmManager.getAlgorithmManager().execute();
//				runAlgorithm();
			} catch (NoneAlgorithmException e) {
				throw new TransferFailedException("can not find the algorithm" + e);
			}
		} else {
//			System.out.println("************************************From run algorithm from operation.");
			if (! algorithm.isRunning()){
				if (lastReprocessable != null)
					UIAlgorithmManager.getAlgorithmManager().executeFrom(lastReprocessable.getAgent());
				else
					try {
						runAlgorithm();
					} catch (NoneAlgorithmException e) {
						throw new TransferFailedException(e);
					}
			}
			else
				System.out.println("ALGORITHM IS ALREADY RUNNING");
		}
	}
	
	/**
	 * Force to run current algorithm from the operation for selected data item.
	 * @param operation an operation to start
	 * @throws TunerNotReadyException
	 * @throws TransferFailedException
	 */
	public void forceRunAlgorithmFromOperation(Operation operation ) throws TunerNotReadyException, 
	TransferFailedException {
		System.out.println(">>Run algorithm:" + algorithm.getName() + " from Operation: " + operation.getName() + " for DataItem:" + getSelectedDataItemIndex() +":"+dataItems.get(getSelectedDataItemIndex()).getName());

		
		List<Operation> operations = getOperationManager(getSelectedDataItemIndex()).getOperations();
		Operation lastReprocessable = getOperationChainHead(operations);
		final int index = operations.indexOf(operation);
		
//		for (int i = index; i < operations.size(); i++) {
//			final Operation op = operations.get(i);
//			op.setStatus(OperationStatus.InProgress);
////			setOperationsNotActual(op.getName());
//		}
		if (index <= 0) {
			//if the operation is the first in the chain then execute() method must be used to proceed. 
			try {
				UIAlgorithmManager.getAlgorithmManager().execute();
//				runAlgorithm();
			} catch (NoneAlgorithmException e) {
				throw new TransferFailedException("can not find the algorithm" + e);
			}
		} else {
//			System.out.println("************************************From run algorithm from operation.");
			if (! algorithm.isRunning()){
//				if (lastReprocessable != null)
//					UIAlgorithmManager.getAlgorithmManager().executeFrom(lastReprocessable.getAgent());
				if (operation != null)
					UIAlgorithmManager.getAlgorithmManager().executeFrom(operation.getAgent());
				else
					try {
						runAlgorithm();
					} catch (NoneAlgorithmException e) {
						throw new TransferFailedException(e);
					}
			}
			else
				System.out.println("ALGORITHM IS ALREADY RUNNING");
		}
	}

	/**
	 * Runs an algorithm for the data item to ensure that the operation data is actual.
	 * @param operationName name of operation which finally must obtain an actual data.
	 * @param dataItemIndex index of a data item to be processed. 
	 * @throws TunerNotReadyException
	 * @throws TransferFailedException
	 * @throws NoneAlgorithmException
	 * @throws FailedToExecuteException
	 */
	public void runAlgorithmForOperation(String operationName, int dataItemIndex ) throws TunerNotReadyException, TransferFailedException, NoneAlgorithmException, FailedToExecuteException {
		final DataItem dataItem = dataItems.get(dataItemIndex);
		System.out.println(">>Run algorithm:" + algorithm.getName() + " from Operation: " + operationName + " for DataItem:" + dataItemIndex +":"+dataItem.getName());

		Operation operationToStart = null;
		int indexToStart = 0;
		List<Operation> operations = getOperationManager(dataItemIndex).getOperations();
		
		for (int i = 0; i < operations.size(); i++) {
			final Operation operation = operations.get(i);
			if (!operation.isActual()
//					operation.getStatus() != OperationStatus.Done 
					&& operation.getStatus() != OperationStatus.InProgress
					) {
				operationToStart = operation;
				indexToStart = i;
				break;
			}
			if (operation.getName().equals(operationName)) {
				//we do not need to check all following operations
				break;
			}
		}
		if (operationToStart == null) {
			System.out.println(">>runAlgorithmForOperation> the operation result is up to date or in progress");
			return;
		}

		operationToStart = AlgorithmTask.getOperationChainHead(operations);

		//set InProgress status for being executed operations
//		for (int i = indexToStart; i < operations.size(); i++) {
//			final Operation operation = operations.get(i);
//			operation.setStatus(OperationStatus.InProgress);
//			setOperationsNotActual(operation.getName());
//		}
		
		if (indexToStart == 0) {
			//if the operation is the first in the chain then execute of whole algorithm must be used to proceed.
			
			UIAlgorithmManager.getAlgorithmManager().executeAll(
					Collections.singletonList(algorithmInputs.get(dataItemIndex)));
		} else {
			//Agent object has an reference to data item, processor and algorithm
			//by the way we do not need to provide AlgorithmManager with information about DataItem or Algorithm
			System.out.println("************************************FOR run algorithm for operation.");
			UIAlgorithmManager.getAlgorithmManager().executeFrom(operationToStart.getAgent());
		}
	}

	public Algorithm getAlgorithm() {
		return algorithm;
	}

	public void applyParameterChangesForAllDataItems(Operation operation) throws Exception {
		for (OperationParameter operationParameter : operation.getParameters()) {
			if (operationParameter.isChanged()) {
				Object value = operationParameter.getValue();
				if (operationParameter.getType() == OperationParameterType.Region) {
					//convert ui region data to Server format
//					value = RegionUtil.convertToServerObject((List<UIRegion>) value);
				}
				UIAlgorithmManager.getAlgorithmManager().setTunerOfAlgorithms(
						algorithmInputs, 
						operationParameter.getName(),
						value);
				reloadOpearationParameter(operationParameter.getName());
//				operation.setActual(false);
				markOperationsActual(operation.getName(), false);
				operation.updateStatus();
			}
		}
		
	}

	public void markOperationsActual(String operationName, boolean isActual) {
		for (int i = 0; i < getDataItemsCount(); i++) {
			final List<Operation> operations = getOperationManager(i).getOperations();
			for (Operation anOperation : operations) {
				if (anOperation.getName().equals(operationName)) {
					anOperation.setActual(isActual);
					break;
				}
			}
		}

	}

	private void reloadOpearationParameter(String name) {
		for (OperationManager operationManager : operationManagers) {
			if (operationManager.isLoaded()) {
				for (Operation operation : operationManager.getOperations()) {
					for (OperationParameter operationParameter : operation.getParameters()) {
						if (operationParameter.getName().equals(name)) {
							operationParameter.updateValueFromServer();
						}
					}
				}
			}
		}
	}

	private void reloadOpearationParameters() {
		for (OperationManager operationManager : operationManagers) {
			if (operationManager.isLoaded()) {
				for (Operation operation : operationManager.getOperations()) {
					for (OperationParameter operationParameter : operation.getParameters()) {
						operationParameter.updateValueFromServer();
					}
				}
			}
		}
	}

	public void applyOptionChangesForAllDataItems(String optionName, Object optionValue) throws SetTunerException {
		UIAlgorithmManager.getAlgorithmManager().setTunerOfAlgorithms(
				algorithmInputs, 
				optionName,
				optionValue);
		
		reloadOptions();
	}

	public void applyParameterChangesForAllDataItems(String parameterName, Object parameterValue) throws SetTunerException {
		UIAlgorithmManager.getAlgorithmManager().setTunerOfAlgorithms(
				algorithmInputs, 
				parameterName,
				parameterValue);
		
		reloadOpearationParameter(parameterName);
	}

	private void reloadOptions() {
		for (OperationManager operationManager : operationManagers) {
			if (operationManager.isLoaded()) {
				for (Operation operation : operationManager.getOperations()) {
					operation.getOptions().updateValuesFromServer();
				}
			}
		}
	}

	public Operation getNextOperation(String operationName) {
		OperationManager operationManager = getOperationManager(getSelectedDataItemIndex());
		if (operationManager != null) {
			return operationManager.getNextOperation(operationName);
		}
		return null;
	}

	/**
	 * Gets the region manager for the Algorithm Task.
	 * @return RegionManager instance.
	 */
//	public RegionManager getRegionManager() {
//		return regionManager;
//	}
	
	
	/**
	 * Actual data flag updater. 
	 * If data has been received by one of an operation
	 * then all operations with the same name for other data items
	 * must be marked as not Actual.
	 */
	private final OperationDataListener actualOperationDataListener = new OperationDataListener() {
		public void outputDataUpdated(final Operation operation, IGroup oldData, final IGroup newData) {
//			Display.getDefault().asyncExec(new Runnable() {
//				public void run() {
					//mark the operations for other DataItems as NOT actual
					//each time when current DataItem operation has been updated with new data.
					//The flag will be used to run algorithm from not actual Operation
					//when another DataItem will be selected.
					for (int i = 0; i < getDataItemsCount(); i++) {
						if (i != getSelectedDataItemIndex()) {
							final List<Operation> operations = getOperationManager(i).getOperations();
							for (Operation anOperation : operations) {
								if (anOperation.getName().equals(operation.getName())
										&& operation.isActual()
										) {
									
									anOperation.setActual(false);
									break;
								}
							}
						}
					}
//				}
//			});
		}
	};
	
	
	
	protected final List<AlgorithmTaskListener> algorithmTaskListeners = new ArrayList<AlgorithmTaskListener>();
	
	public void addAlgorithmTaskListener(AlgorithmTaskListener algorithmTaskListener) {
		algorithmTaskListeners.add(algorithmTaskListener);
	}
	public void removeAlgorithmTaskListener(AlgorithmTaskListener algorithmTaskListener) {
		algorithmTaskListeners.remove(algorithmTaskListener);
	}
	public void removeAllAlgorithmTaskListeners() {
		algorithmTaskListeners.clear();
	}
	public List<AlgorithmTaskListener> getAlgorithmTaskListeners() {
		return new ArrayList<AlgorithmTaskListener>(algorithmTaskListeners);
	}
	
	protected void fireSelectedDataItemIndexChangedEvent() {
		for (AlgorithmTaskListener changeListener : algorithmTaskListeners) {
			changeListener.selectedDataItemIndexChanged(this, selectedDataItemIndex);
		}
	}

	public interface AlgorithmTaskListener {
		void selectedDataItemIndexChanged(AlgorithmTask algorithmTask, int newIndex);
		
	}

	/**
	 * @return the algorithmInputs
	 */
	public List<AlgorithmInput> getAlgorithmInputs() {
		return algorithmInputs;
	}

	public void updateOperationParameters() {
		for (OperationManager operationManager : operationManagers){
			for (Operation operation : operationManager.getOperations())
				operation.updateParameters();
		}
	}

	public AlgorithmInput getSelectedAlgorithmInput(){
		return algorithmInputs.get(selectedDataItemIndex);
	}

	public URI getFileUri() {
		return fileUri;
	}

	public void setFileUri(URI fileUri) {
		this.fileUri = fileUri;
	}

	public void interrupt() {
		for (AlgorithmInput input : algorithmInputs){
			input.getAlgorithm().interrupt();
		}
	}
	
	public void addStatusListener(AlgorithmTaskStatusListener listener){
		if (statusListenerList == null) 
			statusListenerList = new ArrayList<AlgorithmTaskStatusListener>();
		statusListenerList.add(listener);
	}
	
	public void removeStatusListener(AlgorithmTaskStatusListener listener){
		if (statusListenerList != null)
			statusListenerList.remove(listener);
	}
	
	public void addDataItem(DataItem item) throws ProcessorChainException, NoneAlgorithmException {
		if (!dataItems.contains(item)){
			dataItems.add(item);
			operationManagers.add(new OperationManager(id, dataItems.indexOf(item)));
			AlgorithmInput algorithmInput = new AlgorithmInput(item.getDataObject(), algorithm.clone());
			algorithmInputs.add(algorithmInput);
//			algorithmInput.getAlgorithm().addStatusListener(
//					new AlgorithmStatusListener(){
//
//						public void onStatusChanged(AlgorithmStatus status) {
//							fireAlgorithmStatusChangedAction(status);
//						}
//
//						public void setStage(int operationIndex, AlgorithmStatus status) {
//							fireAlgorithmStageChangedAction(operationIndex, status);
//						}
//
//					});
			loadOperationManager(dataItems.indexOf(item));
		}
	}
	
	public void setSelectedDataItem(DataItem item) throws ProcessorChainException, NoneAlgorithmException{
		addDataItem(item);
		setSelectedDataItem(dataItems.indexOf(item));
	}
	
	public void changeAlgorithmInput(DataItem item){
		dataItems.set(0, item);
		algorithmInputs.get(0).setDatabag(item.getDataObject());
	}

	public void clear() {
		for (OperationManager operationManager : operationManagers){
			List<Operation> operations = operationManager.getOperations();
			for (Operation operation : operations){
				try {
//					operation.resetParametersToDefault();
//					operation.informParametersChanged();
					if (operation.getOutputData() != null)
						operation.setOutputData(Factory.createGroup("emptyData"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void dispose(){
		algorithm = null;
		dataItems.clear();
		for (AlgorithmInput algorithmInput : algorithmInputs){
			algorithmInput.dispose();
		}
		algorithmInputs.clear();
//		operationManagers.dispose();
		if (operationManagers != null){
			for (OperationManager manager : operationManagers)
				manager.dispose();
			operationManagers.clear();
		}
//		statusListenerList.clear();
	}
	
	public static Operation getOperationChainHead(List<Operation> operations){
		Operation lastReprocessableOperation = null;
		boolean hasUnActualOperation = false;
		for (Operation operation : operations) {
			//find first operation with not actual data
			if (operation.isReprocessable())
				lastReprocessableOperation = operation;
			if (!operation.isActual() && operation.getStatus() != OperationStatus.InProgress) {
				hasUnActualOperation = true;
				break;
			}
		}
		return hasUnActualOperation ? lastReprocessableOperation : null;
	}

	/**
	 * @param regionParameterManager the regionParameterManager to set
	 */
	public void setRegionParameterManager(Object regionParameterManager) {
		this.regionParameterManager = regionParameterManager;
	}

	/**
	 * @return the regionParameterManager
	 */
	public Object getRegionParameterManager() {
		return regionParameterManager;
	}
}
