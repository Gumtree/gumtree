/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package au.gov.ansto.bragg.wombat.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.vis.listener.XYChartMouseEvent;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;

import au.gov.ansto.bragg.cicada.core.exception.SetTunerException;
import au.gov.ansto.bragg.cicada.core.exception.TransferFailedException;
import au.gov.ansto.bragg.cicada.core.exception.TunerNotReadyException;
import au.gov.ansto.bragg.datastructures.core.plot.Position;
import au.gov.ansto.bragg.kakadu.core.AlgorithmTask;
import au.gov.ansto.bragg.kakadu.core.OperationManager;
import au.gov.ansto.bragg.kakadu.core.data.Operation;
import au.gov.ansto.bragg.kakadu.core.data.OperationParameter;
import au.gov.ansto.bragg.kakadu.core.data.OperationParameter.OperationParameterListener;
import au.gov.ansto.bragg.kakadu.ui.editors.BooleanOperationParameterEditor;
import au.gov.ansto.bragg.kakadu.ui.editors.DefaultOperationParameterEditor;
import au.gov.ansto.bragg.kakadu.ui.editors.NumericOperationParameterEditor;
import au.gov.ansto.bragg.kakadu.ui.editors.OperationParameterEditor;
import au.gov.ansto.bragg.kakadu.ui.editors.OptionOperationParameterEditor;
import au.gov.ansto.bragg.kakadu.ui.editors.RegionOperationParameterEditor;
import au.gov.ansto.bragg.kakadu.ui.editors.StepDirectionOperationParameterEditor;
import au.gov.ansto.bragg.kakadu.ui.editors.TextOperationParameterEditor;
import au.gov.ansto.bragg.kakadu.ui.editors.UriOperationParameterEditor;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotManager;
import au.gov.ansto.bragg.kakadu.ui.region.RegionParameterManager;
import au.gov.ansto.bragg.kakadu.ui.util.Util;
import au.gov.ansto.bragg.kakadu.ui.views.AnalysisParametersView;

/**
 * @author nxi
 * Created on 10/03/2009
 */
public class WombatAnalysisControlViewOld extends AnalysisParametersView {

	public static final String NAVIGATION_PROCESSOR_NAME = "slice_selection";
	public static final String ONEDPLOT_PROCESSOR_NAME = "plotGenerator_processor";
	public static final String SCAN_VARIABLE_NAME = "frame_valueOnAxis";
	public static final String APPLYTOALL_VARIABLE_NAME = "frame_loopToAll";
	public static final String RESET_HISTORY_VARIABLE_NAME = "frame_resetHistory";
	public static final String CORRECTED_HISTOGRAM_PROCESSOR_NAME = "geometryCorrection_processor";
	public static final String INTEGRATION_PROCESSOR_NAME = "integration_processor";
	
	protected static final String BACKGROUND_FOR_EFFICIENCY_MAP_TUNER_NAME 
		= "frame_backgroundForEfficiencyMapURI";
	protected ArrayList<OperationParameterEditor> parameterEditorList;

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kakadu.ui.views.AnalysisParametersView#getSavableTunerNames()
	 */
	@Override
	protected String[] getSavableTunerNames() {
		return new String[]{EFFICIENCY_MAP_TUNER_NAME, BACKGROUND_FOR_EFFICIENCY_MAP_TUNER_NAME};
	}

	@Override
	public void createPartControl(Composite composite) {
		super.createPartControl(composite);

		AlgorithmTask task = getAlgorithmTask();
		final Operation operation = task.getOperationManager(0).getOperation(NAVIGATION_PROCESSOR_NAME);
		final OperationParameter valueOnAxisParameter = operation.getOperationParameter(SCAN_VARIABLE_NAME);
		final Operation plotOperation = task.getOperationManager(0).getOperation(ONEDPLOT_PROCESSOR_NAME);
		final Operation geometryOperation = task.getOperationManager(0).getOperation(CORRECTED_HISTOGRAM_PROCESSOR_NAME);
		final Operation integrationOperation = task.getOperationManager(0).getOperation(INTEGRATION_PROCESSOR_NAME);
		final OperationParameter applyToAllParameter = operation.getOperationParameter(APPLYTOALL_VARIABLE_NAME);
		final OperationParameter resetHistoryParameter = operation.getOperationParameter(RESET_HISTORY_VARIABLE_NAME);
//		final IPointLocatorListener mouseListener = new IPointLocatorListener(){
//
//			public void locationUpdated(double x, double y, double val) {
//				List<OperationParameterEditor> editors = parameterEditorsMap.get(operation.getName());
//				for (OperationParameterEditor editor : editors){
//					if (editor.getOperationParameter() == parameter){
////						parameter.setValue(new Double(x));
////						editor.loadData();
//						if (editor instanceof OptionOperationParameterEditor)
//							((OptionOperationParameterEditor) editor).setSelection(new Double(x));
//					}
//				}
//				
//				applyParameters();
//			}
//		};
		
		final OperationParameterListener applyToAllListener = new OperationParameterListener(){

			public void serverDataUpdated(
					OperationParameter operationParameter, Object newData) {
				if (newData instanceof Boolean){
					geometryOperation.setOperationDataListenerEnabled(!(Boolean) newData);
					integrationOperation.setOperationDataListenerEnabled(!(Boolean) newData);
					plotOperation.setOperationDataListenerEnabled(!(Boolean) newData);
				}
			}
			
		};
		
		applyToAllParameter.addOperationParameterListener(applyToAllListener);
		
//		final KurandaMouseListener doubleClickListener = new KurandaMouseListener(){
//
//			public void mouseDoubleClick(EventData eventData) {
//				List<OperationParameterEditor> editors = parameterEditorsMap.get(operation.getName());
//				for (OperationParameterEditor editor : editors){
//					if (editor.getOperationParameter() == valueOnAxisParameter){
////						parameter.setValue(new Double(x));
////						editor.loadData();
//						if (editor instanceof OptionOperationParameterEditor)
//							((OptionOperationParameterEditor) editor).setSelection(
////									new Position(String.valueOf(eventData.getX())));
//									Double.valueOf(eventData.getX()));
//					}
//				}
//				
//				applyParameters();				
//			}
//
//			public void mouseDown(EventData eventData) {
//				
//			}
//
//			public void mouseUp(EventData eventData) {
//				
//			}
//			
//		};
//
//		PlotManager.addOpenNewPlotListener(new PlotManager.OpenNewPlotListener(){
//
//			public void newPlotOpened(Plot plot) {
//				if (plot.getOperaton() == plotOperation)
////					plot.getCurrentPlotWidget().addPointLocatorListener(mouseListener);
//					plot.getCurrentPlotWidget().addMouseEventListener(doubleClickListener);
//			}});
		
		final ChartMouseListener chartMouseListener = new ChartMouseListener() {
			
			@Override
			public void chartMouseMoved(ChartMouseEvent event) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void chartMouseClicked(ChartMouseEvent event) {
				if (event instanceof XYChartMouseEvent) {
					List<OperationParameterEditor> editors = parameterEditorsMap.get(operation.getName());
					for (OperationParameterEditor editor : editors){
						if (editor.getOperationParameter() == valueOnAxisParameter){
							if (editor instanceof OptionOperationParameterEditor)
								((OptionOperationParameterEditor) editor).setSelection(
										new Position(String.valueOf(
												((XYChartMouseEvent) event).getX())));
						}
					}
				}
			}
		};

		PlotManager.addOpenNewPlotListener(new PlotManager.OpenNewPlotListener(){

			public void newPlotOpened(au.gov.ansto.bragg.kakadu.ui.plot.Plot plot) {
				if (plot.getOperaton() == plotOperation)
//					plot.getCurrentPlotWidget().addPointLocatorListener(mouseListener);
					plot.getCurrentPlotWidget().addChartMouseListener(chartMouseListener);
			}});
		
		configurationButton.removeSelectionListener(configurationSelectionListener);
		configurationButton.setText("Reset");
		configurationButton.setToolTipText("clear all history data");
		configurationButton.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}

			public void widgetSelected(SelectionEvent arg0) {
				if (MessageDialog.openConfirm(getSite().getShell(), "Confirm of Clearing History Data", 
						"Do you want to clear all the history data?")){
					try {
						algorithmTask.applyParameterChangesForAllDataItems(RESET_HISTORY_VARIABLE_NAME, true);
						algorithmTask.markOperationsActual(NAVIGATION_PROCESSOR_NAME, false);
					} catch (SetTunerException e) {
						Util.handleException(getSite().getShell(), e);
					}
					applyParameters();
				}
			}});
		
	}
	
	protected void addOperationParameters(final Operation operation) {

		//create a new group of parameters
		if (operation.getParameters().size() > 0){
			//		Group parameterEditorsGroup = new Group(parameterEditorsHolderComposite, SWT.NONE);
			Composite parameterEditorsGroup = parameterEditorsHolderComposite;
			//		parameterEditorsGroup.setText(operation.getUILabel());
			parameterEditorsGroup.setLayoutData(new GridData(GridData.FILL, SWT.CENTER, true, false));
			GridLayout parameterEditorsCompositeGridLayout = new GridLayout();
			parameterEditorsCompositeGridLayout.numColumns = 2;
			parameterEditorsCompositeGridLayout.marginWidth = 2;
			parameterEditorsCompositeGridLayout.marginBottom = 0;
			parameterEditorsCompositeGridLayout.marginHeight = 2;
			parameterEditorsCompositeGridLayout.marginTop = 0;
			parameterEditorsCompositeGridLayout.verticalSpacing = 2;
			parameterEditorsGroup.setLayout(parameterEditorsCompositeGridLayout);
			//		parameterEditorsGroup.
			//		parameterEditorsGroup.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			//		parameterEditorsGroup.setBackgroundMode(SWT.INHERIT_FORCE);
			//			parameterEditorCompositeMap.put(operationName, parameterEditorComposite);

			//register new list of parameter editors
			parameterEditorList = new ArrayList<OperationParameterEditor>();
			parameterEditorsMap.put(operation.getName(), parameterEditorList);

			if (operation != null) {
				for (OperationParameter operationParameter : operation.getParameters()) {
					OperationParameterEditor operationParameterEditor;
					switch (operationParameter.getType()) {
					case Text:
						operationParameterEditor = new TextOperationParameterEditor(
								operationParameter,
								parameterEditorsGroup);
						break;
					case Number:
						operationParameterEditor = new NumericOperationParameterEditor(
								operationParameter,
								parameterEditorsGroup);
						break;
					case Boolean:
						operationParameterEditor = new BooleanOperationParameterEditor(
								operationParameter,
								parameterEditorsGroup);
						break;
					case Uri:
						operationParameterEditor = new UriOperationParameterEditor(
								operationParameter,
								parameterEditorsGroup);
						break;
					case Region:
						operationParameterEditor = new RegionOperationParameterEditor(
								operationParameter,
								parameterEditorsGroup);
						((RegionOperationParameterEditor) operationParameterEditor).setRegionParameter(
								((RegionParameterManager) algorithmTask.getRegionParameterManager()
										).findParameter(operation));
						break;
					case Option:
						operationParameterEditor = new OptionOperationParameterEditor(
								operationParameter,
								parameterEditorsGroup);
						break;
					case StepDirection:
						operationParameterEditor = new StepDirectionOperationParameterEditor(
								operationParameter,
								parameterEditorsGroup);								
						break;
					default:
						operationParameterEditor = new DefaultOperationParameterEditor(
								operationParameter,
								parameterEditorsGroup);
					break;
					}

					operationParameterEditor.addChangeListener(parameterEditorChangeListener);
					operationParameterEditor.addApplyParameterListener(applyParametersListener);
					parameterEditorList.add(operationParameterEditor);
				}
			}

			//		} else {
			//			if (operation != null) {
			//				//update existed editors with parameters of selected operation
			//				final List<OperationParameter> parameters = operation.getParameters();
			//				final List<OperationParameterEditor> parameterEditorList = parameterEditorsMap.get(operationName);
			//				for (int i = 0; i < parameters.size(); i++) {
			//					OperationParameter parameter = (OperationParameter) parameters.get(i);
			//					final OperationParameterEditor operationParameterEditor = parameterEditorList.get(i);
			//					operationParameterEditor.setOperationParameter(parameter);
			//					operationParameterEditor.loadData();
			//				}
			//			}
			//			
			//		}


			//define parameterEditorComposite which contains parameter editors of selected operation
			parameterEditorsGroup.layout();

			parameterEditorsHolderComposite.layout();
			//		parent.layout();

			//		final Point propertiesCompositeSize = propertiesComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			//		final Point size = plotAndOperationPropertiesSashForm.getSize();
			//		operationPropertiersScrolledComposite.setMinSize(propertiesCompositeSize);
			//		operationPropertiersScrolledComposite.layout();
		}
	}

	protected void applyParameters() {

		boolean isParametersChanged = false;

		//check is changed parameters available
		for (Operation operation : operations) {
			if (operation.isParametersChanged()) {
				isParametersChanged = true;
				break;
			}
		}

//		if (!isParametersChanged) {
//			//there are no changed parameters
//			return;
//		}

		//apply changed parameters
		try {
			for (Operation operation : operations) {
				getAlgorithmTask().applyParameterChangesForAllDataItems(operation);
			}
		} catch (Exception e) {
			handleException(e);
		}


		//OperationManager for selected data item
		OperationManager operationManager = algorithmTask.getOperationManager(algorithmTask.getSelectedDataItemIndex());

		//detect first no actual Operation data to run the algorithm from the operation.
//		final Operation[] firstNotActualOperation = new Operation[1];
//		Operation lastReprocessableOperation = operationManager.getOperations().get(0);
//		for (Operation operation : operationManager.getOperations()) {
//			//find first operation with not actual data
//			if (operation.isReprocessable())
//				lastReprocessableOperation = operation;
//			if (firstNotActualOperation[0] == null && !operation.isActual()) {
//				firstNotActualOperation[0] = operation;
//				break;
//			}
//		}
		Operation lastReprocessableOperation = AlgorithmTask.getOperationChainHead(
				operationManager.getOperations());
		//run Algorithm from first operation with not actual data
//		if (firstNotActualOperation[0] != null) {
//			Display.getDefault().asyncExec(new Runnable() {
//				public void run() {
//					System.out.println("CHECK TIMING RUN algorithm from operation*************");
//					try {
//						algorithmTask.runAlgorithmFromOperation(firstNotActualOperation[0]);
//					} catch (TunerNotReadyException e) {
//						handleException(e);
//					} catch (TransferFailedException e) {
//						handleException(e);
//					}
//				}
//			});
//		}

		if (lastReprocessableOperation == null || operations.indexOf(lastReprocessableOperation) < 1) {
			lastReprocessableOperation = operations.get(1);	
		}
		System.out.println("CHECK TIMING RUN algorithm from operation*************" 
				+ lastReprocessableOperation.getName());
		try {
			saveEfficiencyMapTuner();
			algorithmTask.forceRunAlgorithmFromOperation(lastReprocessableOperation);
		} catch (TunerNotReadyException e) {
			handleException(e);
		} catch (TransferFailedException e) {
			handleException(e);
		}
		
		updateParametersData();
	}
	
	public void setAlgorithmTask(AlgorithmTask algorithmTask) {
		super.setAlgorithmTask(algorithmTask);
		algorithmTask.removeStatusListener(statusListener);
	}
}
