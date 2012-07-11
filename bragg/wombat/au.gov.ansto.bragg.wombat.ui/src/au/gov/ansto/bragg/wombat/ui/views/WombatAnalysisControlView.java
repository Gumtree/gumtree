/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Norman Xiong (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.wombat.ui.views;

import java.io.File;
import java.net.URI;
import java.util.Formatter;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.cicada.core.Algorithm.AlgorithmStatus;
import au.gov.ansto.bragg.cicada.core.exception.TransferFailedException;
import au.gov.ansto.bragg.cicada.core.exception.TunerNotReadyException;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.kakadu.core.AlgorithmTask;
import au.gov.ansto.bragg.kakadu.core.AlgorithmTaskStatusListener;
import au.gov.ansto.bragg.kakadu.core.OperationManager;
import au.gov.ansto.bragg.kakadu.core.data.Operation;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotManager;
import au.gov.ansto.bragg.kakadu.ui.util.DisplayManager;
import au.gov.ansto.bragg.kakadu.ui.util.Util;
import au.gov.ansto.bragg.kakadu.ui.views.AnalysisParametersView;
import au.gov.ansto.bragg.process.agent.ProcessorAgent;
import au.gov.ansto.bragg.process.exception.ProcessFailedException;
import au.gov.ansto.bragg.process.exception.ProcessorChainException;
import au.gov.ansto.bragg.process.port.Tuner;
import au.gov.ansto.bragg.process.port.TunerPortListener;
import au.gov.ansto.bragg.wombat.ui.internal.Activator;
import au.gov.ansto.bragg.wombat.ui.internal.WombatAnalysisPerspective;


/**
 * The view displays operation parameters for 
 * all operations in current AlgorithmTask.
 * 
 * @author nxi
 */
public class WombatAnalysisControlView extends AnalysisParametersView {

	public static final String NAVIGATION_PROCESSOR_NAME = "nexus_processor";
	public static final String ONEDPLOT_PROCESSOR_NAME = "result_processor";
	public static final String SCAN_VARIABLE_NAME = "frame_axisValue";
	public static final String IS_IN_LOOP_TUNER_NAME = "frame_isInLoop";
	public static final String NUMBER_OF_STEPS_TUNER_NAME = "frame_numberOfSteps";
	public static final String CURRENT_STEP_INDEX_TUNER_NAME = "frame_currentStepIndex";
	public static final String CORRECTED_HISTOGRAM_PROCESSOR_NAME = "geometryCorrection_processor";
	public static final String INTEGRATION_PROCESSOR_NAME = "fitting_processor";
	public static final String EFFICIENCY_OPERATION_NAME = "efficiencyCorrection_processor";
	public static final String EFFICIENCY_FOLDER_TUNER_NAME = "frame_efficiencyMapFolderURI";
	public static final String EFFICIENCY_FILENAME_TUNER_NAME = "frame_efficiencyMapFilename";
	public static final String NAME_ENABLE_EFFICIENCY_CORRECTION = "frame_efficiencyCorrectionEnable";
	public static final String NAME_ENABLE_GEOMETRY_CORRECTION = "frame_geometryCorrectionSkip";
	
	
	protected static final String HEADER_LINE = "run#\t record\t mf1\t mf2\t sx\t sy\t sz\t som\t stth\t ip\t " +
			"integral\t int(ip)\t error(ip)\t 2theta(ip)\t error(ip)\t fwhm(ip)\t error(ip)\t chi";
	protected static final String[] DEVICE_NAMES = new String[]{"mf1", "mf2", "sx", "sy", "sz", "som", "stth"};
	protected ProgressBar progressBar;
	protected Button efficiencyMapButton;
	private AlgorithmStatus currentStatus = AlgorithmStatus.Idle;
	private AlgorithmTaskStatusListener statusListener;
	private PlotManager.OpenNewPlotListener plotListener;

	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kakadu.ui.views.AnalysisParametersView#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite composite) {
		super.createPartControl(composite);

		progressBar = new ProgressBar(parameterEditorsHolderComposite, SWT.HORIZONTAL | SWT.NULL);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		progressBar.setLayoutData (data);
		progressBar.setForeground(composite.getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
		progressBar.setEnabled(false);
		
		
//		progressBar.moveAbove(parameterGroupButtonsComposite);
		defaultParametersButton.dispose();
		revertParametersButton.dispose();
		configurationButton.dispose();
		applyParametersButton.setText("Apply");
		data = new GridData ();
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		applyParametersButton.setLayoutData(data);
		
		efficiencyMapButton = new Button(parameterGroupButtonsComposite, SWT.PUSH);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		efficiencyMapButton.setLayoutData (data);
		efficiencyMapButton.setImage(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, 
				"icons/add_obj.gif").createImage());
		efficiencyMapButton.setText("Efficiency");
		efficiencyMapButton.setToolTipText("Click to make new efficiency map");
		efficiencyMapButton.moveAbove(applyParametersButton);
		initListener();
//		parameterGroupButtonsComposite.layout();
//		parameterGroupButtonsComposite.update();
//		parameterGroupButtonsComposite.redraw();
//		parameterGroupButtonsComposite.getParent().update();
//		parameterGroupButtonsComposite.getParent().redraw();
		AlgorithmTask task = getAlgorithmTask();
		final Operation operation = task.getOperationManager(0).getOperation(NAVIGATION_PROCESSOR_NAME);
		final Tuner isInLoopTuner = ((ProcessorAgent) operation.getAgent()).getTuner(IS_IN_LOOP_TUNER_NAME);
//		final OperationParameter parameter = operation.getOperationParameter(SCAN_VARIABLE_NAME);
//		final Operation plotOperation = task.getOperationManager(0).getOperation(ONEDPLOT_PROCESSOR_NAME);
//		final Operation geometryOperation = task.getOperationManager(0).getOperation(CORRECTED_HISTOGRAM_PROCESSOR_NAME);
//		final Operation integrationOperation = task.getOperationManager(0).getOperation(INTEGRATION_PROCESSOR_NAME);

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
		
		statusListener = new AlgorithmTaskStatusListener(){

			public void onChange(final AlgorithmStatus status) {
				DisplayManager.getDefault().asyncExec(new Runnable(){

					public void run() {
						if (currentStatus != AlgorithmStatus.Running && status == AlgorithmStatus.Running){

							progressBar.setEnabled(true);
							progressBar.setMinimum(0);
							progressBar.setMaximum(algorithmTask.getOperationManager(0).getOperations().size());
//							progressBar.setMaximum(6);
						}else if (status == AlgorithmStatus.End){ //AlgorithmStatus.Running && status != AlgorithmStatus.Running){
							progressBar.setSelection(0);
							progressBar.setEnabled(false);
						}
						currentStatus = status;						
					}});
			}

			public void setStage(final int operationIndex, final AlgorithmStatus status) {
				DisplayManager.getDefault().asyncExec(new Runnable(){

					public void run() {
						if (progressBar.isEnabled()){
							if (progressBar.isEnabled() && progressBar.getMaximum() > operationIndex + 1 && 
									operationIndex + 1 > progressBar.getSelection()){
								progressBar.setSelection(operationIndex + 1);
							}else if (progressBar.getMaximum() == operationIndex + 1){
								progressBar.setSelection(0);
								progressBar.setMaximum(0);
								progressBar.setEnabled(false);
							}
						}
					}});
			}};
		
		algorithmTask.addStatusListener(statusListener);

//		SafeUIRunner.asyncExec(new SafeRunnable(){
//
//			@Override
//			public void run() throws Exception {
//				try {
//					au.gov.ansto.bragg.kakadu.ui.plot.Plot plot3 = PlotManager.openPlot(PlotType.OverlayPlot, 3);
//					plot3.setQuickRemoveEnabled(true);
////					plot3.setDataViewSelection(true);
//				} catch (PartInitException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			
//		}); 

		efficiencyMapButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().
					showView(WombatAnalysisPerspective.EFFICIENCY_MAP_MANAGER_VIEW_ID);
				} catch (PartInitException e) {
					e.printStackTrace();
					Util.handleException(getSite().getShell(), e);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		
		final TunerPortListener applyToAllListener = new TunerPortListener(isInLoopTuner){

			@Override
			public void updateUIMax(Object max) {
			}

			@Override
			public void updateUIMin(Object min) {
			}

			@Override
			public void updateUIOptions(List<?> options) {
			}

			@Override
			public void updateUIValue(Object newData) {
				if (newData instanceof Boolean){
					DisplayManager.getDefault().setEnable(!(Boolean) newData);
					System.err.println("Display manager " + (!(Boolean) newData ? "enabled" : "disabled"));
//					geometryOperation.setOperationDataListenerEnabled(!(Boolean) newData);
//					integrationOperation.setOperationDataListenerEnabled(!(Boolean) newData);
//					plotOperation.setOperationDataListenerEnabled(!(Boolean) newData);
				}
			}
			
		};
		isInLoopTuner.addVarPortListener(applyToAllListener);
	}
		
	private String getInstrumentInfo(Plot resultPlot, int index) {
		String info = "";
		for (String deviceName : DEVICE_NAMES){
			info += getDeviceInfo(resultPlot, deviceName, "%.5f", index) + "\t ";
		}
		return info;
	}

	private String getDeviceInfo(Plot inputdata, String deviceName, String numericFormat, int index) {
		String result = "";
		if (deviceName != null){
			try {
				Object item = inputdata.getContainer(deviceName);
				if (item == null)
					item = inputdata.getContainer("old_" + deviceName);
				IArray signal = null;
				String units = "";
				if (item instanceof IDataItem){
					signal = ((IDataItem) item).getData();
					units = ((IDataItem) item).getUnitsString();
				}
				else if (item instanceof IAttribute)
					signal = ((IAttribute) item).getValue();
				if (signal.getElementType() == Character.TYPE)
					result = signal.toString();
				else{
					double signalMax = signal.getArrayMath().getMaximum();
					double signalMin = signal.getArrayMath().getMinimum();
//					result = deviceName + "=";
					if (numericFormat == null)
						numericFormat = "%.5f";
					if (signalMax == signalMin)
						result += (new Formatter()).format(numericFormat, signalMax) + " ";
					else
						result += (new Formatter()).format(numericFormat, signal.getDouble(
								signal.getIndex().set(index))) + " ";
				}
			} catch (Exception e) {
			}
		}
		return result;
	}

	private String getFormatedString(double value, String numericFormat){
		return (new Formatter()).format(numericFormat, value).toString();
	}
	
	private String getProcessInfo(Plot resultPlot) {
		String processInfo = "";
		String log = resultPlot.getProcessingLog();
		if (log.contains("use corrected data"))
			processInfo += "c";
		if (log.contains("efficiency correction"))
			processInfo += "e";
		if (log.contains("geometry curve correction"))
			processInfo += "g";
		if (processInfo.length() > 0)
			processInfo = "_" + processInfo;
		if (log.contains("y in [")){
			String yInfo = log.substring(log.indexOf("y in ["));
			yInfo = yInfo.substring(6, yInfo.indexOf("]"));
			yInfo = yInfo.replace(",", "_");
			processInfo += "_" + yInfo;
		}
		return processInfo;
	}

	private void initListener() {
//		PlotManager.resetPlotViewId();
//		plotListener = new PlotManager.OpenNewPlotListener() {
//			
//			public void newPlotOpened(final au.gov.ansto.bragg.kakadu.ui.plot.Plot plot) {
//				if (plot.getOperaton().getName().equals("integration_processor")){
//					ToolBar verticalBar = plot.getVerticalToolbar();
//					ToolItem transferToolItem = new ToolItem (verticalBar, SWT.NONE);
//					//				copyToolItem.setToolTipText("Copy");
//					transferToolItem.setImage(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, 
//					"icons/arrow_down16x16.png").createImage());
//					transferToolItem.addSelectionListener(new SelectionListener() {
//						
//						public void widgetSelected(SelectionEvent arg0) {
//							List<PlotDataItem> dataItems = plot.getMultiPlotDataManager().getPlotDataItems();
//							if (dataItems != null && dataItems.size() > 0){
//								au.gov.ansto.bragg.kakadu.ui.plot.Plot plot2 = null; 
//								Collection<PlotView> plotViews = PlotManager.getPlotViews();
//								for (PlotView view : plotViews){
//									au.gov.ansto.bragg.kakadu.ui.plot.Plot plot = view.getPlotComposite();
//									if (plot != null && plot.getOperaton() == null)
//										plot2 = view.getPlotComposite();
//								}
//								if (plot2 != null)
//									try {
//										PlotDataItem newDataItem = dataItems.get(0).clone();
//										newDataItem.setColor(AbstractDataSource.getNextColor());
//										newDataItem.setLinked(false);
//										plot2.getMultiPlotDataManager().addPlotDataItem(newDataItem);
//									} catch (PlotException e) {
//										e.printStackTrace();
//									}
//							}
//						}
//						
//						public void widgetDefaultSelected(SelectionEvent arg0) {
//							
//						}
//					});
//				}
//			}
//		};
//		PlotManager.addOpenNewPlotListener(plotListener);
	}

	
//	private void addMouseListener(){
//		List<Plot> plotList = PlotManager.getPlot(operation);
//		IPointLocatorListener mouseListener = new IPointLocatorListener(){
//
//			public void locationUpdated(double x, double y, double val) {
//				List<OperationParameterEditor> editors = parameterEditorsMap.get(operation.getName());
//				for (OperationParameterEditor editor : editors){
//					if (editor.getOperationParameter() == parameter)
//						editor.setData(new Double(x));
//				}
//				updateAllOperationParameters();
//			}
//		};
//		for (Plot plot : plotList){
//			plot.getCurrentPlotWidget().addPointLocatorListener(mouseListener);
//		}
//	}
	
	@Override
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
				algorithmTask.applyParameterChangesForAllDataItems(operation);
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
		if (operations.indexOf(lastReprocessableOperation) < 4)
			lastReprocessableOperation = operations.get(0);
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

		if (lastReprocessableOperation != null) {
	
					System.out.println("CHECK TIMING RUN algorithm from operation*************" 
							+ lastReprocessableOperation.getName());
					try {
						saveEfficiencyMapTuner();
						algorithmTask.runAlgorithmFromOperation(lastReprocessableOperation);
					} catch (TunerNotReadyException e) {
						handleException(e);
					} catch (TransferFailedException e) {
						handleException(e);
					}
			
		}else{
			try {
				algorithmTask.runAlgorithmFromOperation(operations.get(0));
//				runAlgorithmWithSelectedData();
			} catch (Exception e) {
				e.printStackTrace();
				LoggerFactory.getLogger(AnalysisParametersView.class).error(
						"Failed to run the algorithm.", e);
			}
		}
		
		updateParametersData();
	}
	
	@Override
	public void dispose(){
		super.dispose();
		if (algorithmTask != null && statusListener != null)
			algorithmTask.addStatusListener(statusListener);
		PlotManager.removeOpenNewPlotListener(plotListener);
	}
	
	public void updateEfficiencyFolderContents(URI newMapURI) 
	throws ProcessorChainException, ProcessFailedException{
		Operation efficiencyOperation = algorithmTask.getOperationManager(0).getOperation(EFFICIENCY_OPERATION_NAME);
		Tuner efficiencyFolderTuner = ((ProcessorAgent) efficiencyOperation.getAgent()).getTuner(EFFICIENCY_FOLDER_TUNER_NAME);
		if (efficiencyFolderTuner != null){
			URI currentFolder = (URI) efficiencyFolderTuner.getSignal();
			if (currentFolder == null)
				return;
			File newFile = new File(newMapURI);
			File folder = new File(currentFolder);
			if (newFile.getParentFile().equals(folder)){
				efficiencyFolderTuner.setSignal(currentFolder);
			}
			
		}
	}
	
	@Override
	protected String[] getSavableTunerNames(){
		return new String[]{NAME_ENABLE_EFFICIENCY_CORRECTION, NAME_ENABLE_GEOMETRY_CORRECTION, 
				EFFICIENCY_FOLDER_TUNER_NAME, EFFICIENCY_FILENAME_TUNER_NAME};
	}

}
