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
package au.gov.ansto.bragg.kowari.ui.views;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Formatter;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.vis.listener.XYChartMouseEvent;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.cicada.core.exception.TransferFailedException;
import au.gov.ansto.bragg.cicada.core.exception.TunerNotReadyException;
import au.gov.ansto.bragg.datastructures.core.plot.Data;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.Position;
import au.gov.ansto.bragg.datastructures.core.plot.Variance;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.kakadu.core.AlgorithmTask;
import au.gov.ansto.bragg.kakadu.core.OperationManager;
import au.gov.ansto.bragg.kakadu.core.data.Operation;
import au.gov.ansto.bragg.kakadu.core.data.OperationParameter;
import au.gov.ansto.bragg.kakadu.ui.editors.OperationParameterEditor;
import au.gov.ansto.bragg.kakadu.ui.editors.OptionOperationParameterEditor;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotManager;
import au.gov.ansto.bragg.kakadu.ui.util.DisplayManager;
import au.gov.ansto.bragg.kakadu.ui.util.Util;
import au.gov.ansto.bragg.kakadu.ui.views.AnalysisParametersView;
import au.gov.ansto.bragg.kowari.ui.internal.Activator;
import au.gov.ansto.bragg.process.agent.ProcessorAgent;
import au.gov.ansto.bragg.process.port.Tuner;
import au.gov.ansto.bragg.process.port.TunerPortListener;


/**
 * The view displays operation parameters for 
 * all operations in current AlgorithmTask.
 * 
 * @author nxi
 */
public class AnalysisControlView extends AnalysisParametersView {

	public static final String NAVIGATION_PROCESSOR_NAME = "nexus_processor";
	public static final String ONEDPLOT_PROCESSOR_NAME = "result_processor";
	public static final String SCAN_VARIABLE_NAME = "frame_axisValue";
	public static final String IS_IN_LOOP_TUNER_NAME = "frame_isInLoop";
	public static final String NUMBER_OF_STEPS_TUNER_NAME = "frame_numberOfSteps";
	public static final String CURRENT_STEP_INDEX_TUNER_NAME = "frame_currentStepIndex";
	public static final String CURRENT_INDEX_TUNER_NAME = "frame_currentIndex";
	public static final String CORRECTED_HISTOGRAM_PROCESSOR_NAME = "geometryCorrection_processor";
	public static final String INTEGRATION_PROCESSOR_NAME = "fitting_processor";
	
	protected static final String HEADER_LINE = "run#\t record\t mf1\t mf2\t sx\t sy\t sz\t som\t stth\t ip\t " +
			"integral\t int(ip)\t error(ip)\t 2theta(ip)\t error(ip)\t fwhm(ip)\t error(ip)\t chi";
	protected static final String[] DEVICE_NAMES = new String[]{"mf1", "mf2", "sx", "sy", "sz", "som", "stth"};
	
	protected Button generateReportButton;
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kakadu.ui.views.AnalysisParametersView#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite composite) {
		super.createPartControl(composite);
		defaultParametersButton.dispose();
		revertParametersButton.dispose();
		configurationButton.dispose();
		applyParametersButton.setText("Apply");
		GridData data = new GridData ();
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		applyParametersButton.setLayoutData(data);
		generateReportButton = new Button(parameterGroupButtonsComposite, SWT.PUSH);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		generateReportButton.setLayoutData(data);
		generateReportButton.setText("Report");
		generateReportButton.setToolTipText("Generate the analysis report");
		generateReportButton.setImage(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, 
				"icons/report16x16.png").createImage());
		parametersComposite.layout(parameterGroupButtonsComposite.getChildren());
		
		initReportListener();
//		parameterGroupButtonsComposite.layout();
//		parameterGroupButtonsComposite.update();
//		parameterGroupButtonsComposite.redraw();
//		parameterGroupButtonsComposite.getParent().update();
//		parameterGroupButtonsComposite.getParent().redraw();
		AlgorithmTask task = getAlgorithmTask();
		final Operation operation = task.getOperationManager(0).getOperation(NAVIGATION_PROCESSOR_NAME);
		final Tuner isInLoopTuner = ((ProcessorAgent) operation.getAgent()).getTuner(IS_IN_LOOP_TUNER_NAME);
		final OperationParameter parameter = operation.getOperationParameter(SCAN_VARIABLE_NAME);
		final Operation plotOperation = task.getOperationManager(0).getOperation(ONEDPLOT_PROCESSOR_NAME);
		final Operation geometryOperation = task.getOperationManager(0).getOperation(CORRECTED_HISTOGRAM_PROCESSOR_NAME);
		final Operation integrationOperation = task.getOperationManager(0).getOperation(INTEGRATION_PROCESSOR_NAME);

		generateReportButton.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				IGroup result = plotOperation.getOutputData();
				if (result == null){
					MessageDialog.openError(getSite().getShell(), 
							"Report Error", 
							"There is no result data available");
				}
				Plot algorithmResult = (Plot) result;
				String filename = Util.selectDirectoryFromShell(getSite().getShell());
				if (filename == null || filename.trim().length() == 0)
					return;
				URI targetFolderURI = null;
				try {
					targetFolderURI = ConverterLib.path2URI(filename);
				} catch (FileAccessException e) {
					Util.handleException(getSite().getShell(), new FileAccessException("Illegal folder path"));
				}
				exportReport(targetFolderURI, algorithmResult);
			}
		});
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
		
//		final KurandaMouseListener doubleClickListener = new KurandaMouseListener(){
//
//			public void mouseDoubleClick(EventData eventData) {
//				List<OperationParameterEditor> editors = parameterEditorsMap.get(operation.getName());
//				for (OperationParameterEditor editor : editors){
//					if (editor.getOperationParameter() == parameter){
////						parameter.setValue(new Double(x));
////						editor.loadData();
//						if (editor instanceof OptionOperationParameterEditor)
//							((OptionOperationParameterEditor) editor).setSelection(
//									new Position(String.valueOf(eventData.getX())));
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
						if (editor.getOperationParameter() == parameter){
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
	
	private void exportReport(URI targetFolderURI, Plot resultPlot) {
		File folder = null;
		try{
			folder = new File(targetFolderURI);
			if (!folder.exists())
				folder.mkdirs();
		}catch (Exception e) {
			folder = new File(".");
		}
		PrintWriter outputfile = null;
		try {
			File nexusFile = new File(resultPlot.getLocation());
			String sourceFilename = nexusFile.getName();
			String runNumber = sourceFilename.substring(3, sourceFilename.indexOf("."));
			File newFile = new File(targetFolderURI.getPath() + "/KWR" + runNumber + getProcessInfo(resultPlot) + ".sum");
			outputfile = new PrintWriter(new FileWriter(newFile));
			outputfile.append("#" + HEADER_LINE + "\r\n");
			Data intensity = resultPlot.findCalculationData("intensity");
			Data area = resultPlot.findCalculationData("intensity");
			Variance areaVariance = area.getVariance();
			Data mean = resultPlot.findCalculationData("mean");
			Variance meanVariance = mean.getVariance();
			Data sigma = resultPlot.findCalculationData("sigma");
			Variance sigmaVariance = sigma.getVariance();
			Data chi2 = resultPlot.findCalculationData("Chi2");
			IArrayIterator intensityIterator = intensity.getData().getIterator();
			IArrayIterator areaIterator = area.getData().getIterator();
			IArrayIterator areaVarIterator = areaVariance.getData().getIterator();
			IArrayIterator meanIterator = mean.getData().getIterator();
			IArrayIterator meanVarIterator = meanVariance.getData().getIterator();
			IArrayIterator sigmaIterator = sigma.getData().getIterator();
			IArrayIterator sigmaVarIterator = sigmaVariance.getData().getIterator();
			IArrayIterator chi2Iterator = chi2.getData().getIterator();
			String calculationString = null;
			int index = 0;
			while (intensityIterator.hasNext()){
				String instrumentInfo = getInstrumentInfo(resultPlot, index);
				calculationString = getFormatedString(intensityIterator.getDoubleNext(), 
						"%.1f") + "\t " + getFormatedString(areaIterator.getDoubleNext(),
						"%.1f") + "\t " + getFormatedString(Math.sqrt(areaVarIterator.getDoubleNext()),
						"%.1f") + "\t " + getFormatedString(meanIterator.getDoubleNext(),
						"%.5f") + "\t " + getFormatedString(Math.sqrt(meanVarIterator.getDoubleNext()),
						"%.5f") + "\t " + getFormatedString(2 * sigmaIterator.getDoubleNext(),
						"%.5f") + "\t " + getFormatedString(2 * Math.sqrt(sigmaVarIterator.getDoubleNext()),
						"%.5f") + "\t " + getFormatedString(chi2Iterator.getDoubleNext(),
						"%.5f");
				outputfile.append(runNumber + "\t " + (++index) + "\t " + instrumentInfo + 
						"\t " + "1\t " + calculationString + "\r\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if (outputfile != null)
				outputfile.close();
		}

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

	private void initReportListener() {
		
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
				saveEfficiencyMapTuner();
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
}
