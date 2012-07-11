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
package au.gov.ansto.bragg.echidna.ui.views;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Formatter;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PartInitException;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.ui.util.SafeUIRunner;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.cicada.core.Algorithm.AlgorithmStatus;
import au.gov.ansto.bragg.cicada.core.Exporter;
import au.gov.ansto.bragg.cicada.core.Format;
import au.gov.ansto.bragg.cicada.core.exception.TransferFailedException;
import au.gov.ansto.bragg.cicada.core.exception.TunerNotReadyException;
import au.gov.ansto.bragg.cicada.dom.core.CicadaDOM;
import au.gov.ansto.bragg.cicada.dom.core.CicadaDOMFactory;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.nexus.NexusUtils;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.echidna.dra.core.Stitching;
import au.gov.ansto.bragg.echidna.ui.internal.Activator;
import au.gov.ansto.bragg.echidna.ui.internal.EchidnaAnalysisPerspective;
import au.gov.ansto.bragg.echidna.ui.preference.PreferenceConstants;
import au.gov.ansto.bragg.kakadu.core.AlgorithmTask;
import au.gov.ansto.bragg.kakadu.core.AlgorithmTaskStatusListener;
import au.gov.ansto.bragg.kakadu.core.DataSourceManager;
import au.gov.ansto.bragg.kakadu.core.OperationManager;
import au.gov.ansto.bragg.kakadu.core.data.DataSourceFile;
import au.gov.ansto.bragg.kakadu.core.data.Operation;
import au.gov.ansto.bragg.kakadu.core.data.OperationParameter;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotDataItem;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotException;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotManager;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotType;
import au.gov.ansto.bragg.kakadu.ui.util.DisplayManager;
import au.gov.ansto.bragg.kakadu.ui.util.Util;
import au.gov.ansto.bragg.kakadu.ui.views.AnalysisParametersView;
import au.gov.ansto.bragg.kakadu.ui.views.PlotView;
import au.gov.ansto.bragg.process.port.Tuner;


/**
 * The view displays operation parameters for 
 * all operations in current AlgorithmTask.
 * 
 * @author nxi
 */
public class EchidnaAnalysisControlView extends AnalysisParametersView {

	public static final String NAVIGATION_PROCESSOR_NAME = "nexus_processor";
	public static final String ONEDPLOT_PROCESSOR_NAME = "result_processor";
	public static final String SCAN_VARIABLE_NAME = "frame_axisValue";
	public static final String IS_IN_LOOP_TUNER_NAME = "frame_isInLoop";
	public static final String NUMBER_OF_STEPS_TUNER_NAME = "frame_numberOfSteps";
	public static final String CURRENT_STEP_INDEX_TUNER_NAME = "frame_currentStepIndex";
	public static final String CORRECTED_HISTOGRAM_PROCESSOR_NAME = "geometryCorrection_processor";
	public static final String EFFICIENCY_CORRECTION_PROCESSOR_NAME = "efficiencyCorrection_processor";
	public static final String NORMALISATION_PROCESSOR_NAME = "normalisation_processor";
	
	public static final String INTEGRATION_PROCESSOR_NAME = "fitting_processor";
	public final static String EXPORT_ALL_ALGORITHM = EchidnaAnalysisPerspective.REDUCTION_ALGORITHM_NAME;
	
	private static String ALIGNMENT_STATISTIC_TUNER_NAME = "frame_findDataName";
	private static String SKIP_NORMALISATION_TUNER_NAME = "frame_normSkip";
	private static String SKIP_BACKGROUND_TUNER_NAME = "frame_backgroundCorrectionSkip";
	private static String BACKGROUND_FILE_TUNER_NAME = "frame_backgroundFilename";
	private static String NORMALISATION_REFERENCE_TUNER_NAME = "frame_normReference";
	private static String SKIP_EFFICIENCY_TUNER_NAME = "frame_efficiencyCorrectionSkip";
	private static String EFFICIENCY_FILE_TUNER_NAME = "frame_efficiencyCorrectionMapFilename";
	private static String SKIP_GAIN_REFINEMENT_TUNER_NAME = "frame_gainSkip";
	private static String TUBE_ANGLES_REVERSED_TUNER_NAME = "frame_gainreverse";
	private static String APPLY_ANGULAR_TUNER_NAME = "frame_gaincorrect";
	private static String SKIP_GEOMETRY_TUNER_NAME = "frame_geometryCorrectionSkip";
	private static String LOWER_BOUNDARY_TUNER_NAME = "frame_minDist";
	private static String UPPER_BOUNDARY_TUNER_NAME = "frame_maxDist";
	private static String DO_RESCALE_TUNER_NAME = "frame_rescale";
	private static String MERGE_GROUPS_TUNER_NAME = "frame_debunch";
	private static String SAMPLING_STATISTICS_TUNER_NAME = "frame_vertsampling";

	private static final Format[] SUPPORTED_EXPORT_FORMAT = new Format[]{Format.pdCIF, Format.GSAS, Format.XYSigma, Format.hdf}; 
	private static final char[] ILLEGAL_CHARACTERS = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':' };

	protected static final String HEADER_LINE = "run#\t record\t mf1\t mf2\t sx\t sy\t sz\t som\t stth\t ip\t " +
			"integral\t int(ip)\t error(ip)\t 2theta(ip)\t error(ip)\t fwhm(ip)\t error(ip)\t chi";
	protected static final String[] DEVICE_NAMES = new String[]{"mf1", "mf2", "sx", "sy", "sz", "som", "stth"};
	protected ProgressBar progressBar;
	protected Button exportAllButton;
	protected Menu stripChoiceMenu;
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
		
		exportAllButton = new Button(parameterGroupButtonsComposite, SWT.PUSH);
		exportAllButton.setText("Export Selected ...");
		exportAllButton.setImage(Activator.imageDescriptorFromPlugin(
				Activator.PLUGIN_ID, "icons/file-export-16x16.png").createImage());
		exportAllButton.setToolTipText("Export reduction results of all opened nexus files into 3-column files");
//		exportAllButton.setEnabled(isDataSourceAvailable());
		stripChoiceMenu = new Menu(composite.getShell(), SWT.POP_UP);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		exportAllButton.setLayoutData (data);
		
		initListener();
//		parameterGroupButtonsComposite.layout();
//		parameterGroupButtonsComposite.update();
//		parameterGroupButtonsComposite.redraw();
//		parameterGroupButtonsComposite.getParent().update();
//		parameterGroupButtonsComposite.getParent().redraw();
		AlgorithmTask task = getAlgorithmTask();
		final Operation operation = task.getOperationManager(0).getOperation(NAVIGATION_PROCESSOR_NAME);
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
//		loadPreference();

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
		loadPreference();

		SafeUIRunner.asyncExec(new SafeRunnable(){

			@Override
			public void run() throws Exception {
				try {
					au.gov.ansto.bragg.kakadu.ui.plot.Plot plot3 = PlotManager.openPlot(
							PlotType.OverlayPlot, getViewSite().getWorkbenchWindow(), 3);
					plot3.setQuickRemoveEnabled(true);
					plot3.getMultiPlotDataManager().setStartColorIndex(1);
//					plot3.setDataViewSelection(true);
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}); 

	}
		
	public void loadPreference() {
		String efficiencyFile = Platform.getPreferencesService().getString(
				Activator.PLUGIN_ID, PreferenceConstants.P_EFFICIENCY_FILE, "", null); 
		if (efficiencyFile != null && efficiencyFile.trim().length() > 0) {
			File file = new File(efficiencyFile);
			if (file.exists()) {
				URI fileURI = null;
				try {
					fileURI = ConverterLib.path2URI(efficiencyFile);
				} catch (FileAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (fileURI != null) {
					final Operation operation = algorithmTask.getOperationManager(
							0).getOperation(EFFICIENCY_CORRECTION_PROCESSOR_NAME);
					final OperationParameter parameter = operation.getOperationParameter(
							EFFICIENCY_MAP_TUNER_NAME);
					final URI efficiencyFileURI = fileURI;
					DisplayManager.getDefault().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							parameter.setValue(efficiencyFileURI);
//							applyParameters();
							try {
								algorithmTask.applyParameterChangesForAllDataItems(operation);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
//					final Tuner numberOfStepsTuner = ((ProcessorAgent) operation.getAgent()
//							).getTuner(EFFICIENCY_MAP_TUNER_NAME);
//					try {
//						numberOfStepsTuner.setSignal(fileURI);
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} 
				}
			}
		}
		
		
		String angularOffsetFile = Platform.getPreferencesService().getString(
				Activator.PLUGIN_ID, PreferenceConstants.P_ANGULAR_OFFSET_FILE, "", null); 
		Stitching.setANGULAR_OFFSET_FILE(angularOffsetFile);
		
		final String normReference = Platform.getPreferencesService().getString(
				Activator.PLUGIN_ID, PreferenceConstants.P_NORM_REF, "", null);
		if (normReference != null && normReference.length() > 0) {
			final Operation operation = algorithmTask.getOperationManager(
					0).getOperation(NORMALISATION_PROCESSOR_NAME);
			final OperationParameter parameter = operation.getOperationParameter(
					NORMALISATION_REFERENCE_TUNER_NAME);
			DisplayManager.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					parameter.setValue(normReference);
					try {
						algorithmTask.applyParameterChangesForAllDataItems(operation);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}

		initExportAllMenu();
	}
	
	private void initExportAllMenu() {
		
		for (final Format format : SUPPORTED_EXPORT_FORMAT){
			MenuItem formatMenuItem = new MenuItem (stripChoiceMenu, SWT.PUSH);
			formatMenuItem.setText(format.name());
			formatMenuItem.addSelectionListener(new SelectionListener(){

				public void widgetDefaultSelected(SelectionEvent arg0) {

				}

				public void widgetSelected(SelectionEvent e) {
					batchProcess(format);
				}
			});
		}
	}
	
	private void batchProcess(Format format) {
		CicadaDOM cicada = (CicadaDOM) CicadaDOMFactory.getCicadaDOM();
		try{
			String folder = Util.selectDirectoryFromShell(getSite().getShell());
			if (folder == null || folder.trim().length() == 0)
				return;
//			Algorithm exportAllAlgorithm = cicada.loadAlgorithm("Vertical Integration");
			List<DataSourceFile> fileList = 
				DataSourceManager.getInstance().getSelectedFiles();
			if (fileList == null || fileList.size() == 0)
				throw new NullPointerException("No data is available");
			progressBar.setEnabled(true);
			progressBar.setMinimum(0);
			progressBar.setMaximum(fileList.size());
			progressBar.setSelection(0);
//			exportAllButton.setText("Exporting");
			exportAllButton.setEnabled(false);
			
			Exporter exporter = cicada.getAlgorithmManager().getExporter(format);
			int id = 0;
			for (DataSourceFile file : fileList){
				progressBar.setSelection(progressBar.getSelection() + 1);
				IGroup groupData = NexusUtils.getNexusEntryList(file.getDataObject()).get(0);
				cicada.loadInputData(groupData);
//				System.out.println( amanager.listAvailableAlgorithms() );
				cicada.loadAlgorithm(EXPORT_ALL_ALGORITHM);
//				RectilinearRegion region = null;
//				if (groupData instanceof Plot){
//				Group nexusData = NexusUtils.getNexusData(groupData);
//				if (nexusData != null){
//					List<org.gumtree.data.gdm.core.DataItem> axes = NexusUtils.getNexusAxis(nexusData);
//					if (axes != null && axes.size() >= 2){
//						org.gumtree.data.gdm.core.DataItem yAxis = axes.get(axes.size() - 2);
//						org.gumtree.data.gdm.core.DataItem xAxis = axes.get(axes.size() - 1);
//						Array yAxisArray = yAxis.getData();
//						Array xAxisArray = xAxis.getData();
//						double minY = yAxisArray.getMinimum();
//						double maxY = yAxisArray.getMaximum();
//						double yStep = Math.round((maxY - minY) / stripCounts);
//						double minX = xAxisArray.getMinimum();
//						double maxX = xAxisArray.getMaximum();
//						double[] reference = new double[]{minY + yStep * stripId, minX};
//						double[] range = new double[]{yStep, maxX - minX}; 
//						if (stripId == stripCounts - 1)
//							range = new double[]{maxY - reference[0], maxX - minX};
//						region = (RectilinearRegion) RegionFactory.createRectilinearRegion(
//								regionSet, "region" + stripId, reference, range, new String[]{"mm", "mm"}, true);
//					}
//				}
				if (algorithmTask != null && algorithmTask.getAlgorithmInputs().size() > 0){
					List<Tuner> tuners = algorithmTask.getAlgorithmInputs().get(0).getAlgorithm().getTunerArray();
					for (Tuner tuner : tuners){
						if (tuner.getCoreName().equals(ALIGNMENT_STATISTIC_TUNER_NAME))
							cicada.setTuner(ALIGNMENT_STATISTIC_TUNER_NAME, tuner.getSignal());
						if (tuner.getCoreName().equals(SKIP_NORMALISATION_TUNER_NAME))
							cicada.setTuner(SKIP_NORMALISATION_TUNER_NAME, tuner.getSignal());
						if (tuner.getCoreName().equals(SKIP_BACKGROUND_TUNER_NAME))
							cicada.setTuner(SKIP_BACKGROUND_TUNER_NAME, tuner.getSignal());
						if (tuner.getCoreName().equals(BACKGROUND_FILE_TUNER_NAME))
							cicada.setTuner(BACKGROUND_FILE_TUNER_NAME, tuner.getSignal());
						if (tuner.getCoreName().equals(SKIP_EFFICIENCY_TUNER_NAME))
							cicada.setTuner(SKIP_EFFICIENCY_TUNER_NAME, tuner.getSignal());
						if (tuner.getCoreName().equals(EFFICIENCY_FILE_TUNER_NAME))
							cicada.setTuner(EFFICIENCY_FILE_TUNER_NAME, tuner.getSignal());
						if (tuner.getCoreName().equals(SKIP_GAIN_REFINEMENT_TUNER_NAME))
							cicada.setTuner(SKIP_GAIN_REFINEMENT_TUNER_NAME, tuner.getSignal());
						if (tuner.getCoreName().equals(TUBE_ANGLES_REVERSED_TUNER_NAME))
							cicada.setTuner(TUBE_ANGLES_REVERSED_TUNER_NAME, tuner.getSignal());
						if (tuner.getCoreName().equals(APPLY_ANGULAR_TUNER_NAME))
							cicada.setTuner(APPLY_ANGULAR_TUNER_NAME, tuner.getSignal());
						if (tuner.getCoreName().equals(SKIP_GEOMETRY_TUNER_NAME))
							cicada.setTuner(SKIP_GEOMETRY_TUNER_NAME, tuner.getSignal());
						if (tuner.getCoreName().equals(LOWER_BOUNDARY_TUNER_NAME))
							cicada.setTuner(LOWER_BOUNDARY_TUNER_NAME, tuner.getSignal());
						if (tuner.getCoreName().equals(UPPER_BOUNDARY_TUNER_NAME))
							cicada.setTuner(UPPER_BOUNDARY_TUNER_NAME, tuner.getSignal());
						if (tuner.getCoreName().equals(DO_RESCALE_TUNER_NAME))
							cicada.setTuner(DO_RESCALE_TUNER_NAME, tuner.getSignal());
						if (tuner.getCoreName().equals(MERGE_GROUPS_TUNER_NAME))
							cicada.setTuner(MERGE_GROUPS_TUNER_NAME, tuner.getSignal());
						if (tuner.getCoreName().equals(SAMPLING_STATISTICS_TUNER_NAME))
							cicada.setTuner(SAMPLING_STATISTICS_TUNER_NAME, tuner.getSignal());
					}
				}
				cicada.process();
				exportResult((Plot) cicada.getDefaultResult(), folder, file.getLocalName(), exporter);
				progressBar.setSelection(id++);
			}
			progressBar.setSelection(0);
			progressBar.setEnabled(false);
			exportAllButton.setEnabled(true);
//			exportAllButton.setText("Exported");
		}catch (Exception e) {
			exportAllButton.setEnabled(true);
//			exportAllButton.setText("Error");
			Util.handleException(getSite().getShell(), e);
		}
	}
	
	private void exportResult(IGroup result, String folder, String filename, Exporter exporter) {
		String formatExtensionName = exporter.getFormater().getExtensionName();
		formatExtensionName = formatExtensionName.replace('*', 'y');
//		filename = filename.substring(0, filename.indexOf(".")) + formatExtensionName.substring(1);
		int fileID = 0;
		if (filename.startsWith("ECH") && filename.length() > 10) {
			String part = filename.substring(3, 10);
			try {
				fileID = Integer.valueOf(part);
			} catch (Exception e) {
			}
		}
		if (fileID != 0) {
			filename = String.valueOf(fileID);
		} else {
			filename = filename.substring(0, filename.indexOf("."));
		}
		String sampleName = null;
		try {
			sampleName = result.getDataItem("sample_name").getData().toString();
			if (sampleName.length() > 14) {
				sampleName = sampleName.substring(0, 14);
			}
			for (int i = 0; i < ILLEGAL_CHARACTERS.length; i++) {
				sampleName = sampleName.replace(ILLEGAL_CHARACTERS[i], '_');
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		if (sampleName != null) {
			filename += '_' + sampleName;
		}
		filename += formatExtensionName.substring(1);
		filename = folder + "/" + filename;
		
		try {
			exporter.signalExport(result, ConverterLib.path2URI(filename));
		} catch (Exception e1) {
			handleException(e1);
		}
	}
	
	private boolean isDataSourceAvailable() {
		List<au.gov.ansto.bragg.kakadu.core.data.DataItem> dataItemList = DataSourceManager.getInstance().getAllDataItems();
		if (dataItemList == null || dataItemList.size() == 0)
			return false;
		return true;
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
		plotListener = new PlotManager.OpenNewPlotListener() {
			
			public void newPlotOpened(final au.gov.ansto.bragg.kakadu.ui.plot.Plot plot) {
				if (plot.getOperaton().getName().equals("integration_processor")){
					ToolBar verticalBar = plot.getVerticalToolbar();
					ToolItem transferToolItem = new ToolItem (verticalBar, SWT.NONE);
					//				copyToolItem.setToolTipText("Copy");
					transferToolItem.setImage(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, 
					"icons/arrow_down16x16.png").createImage());
					transferToolItem.addSelectionListener(new SelectionListener() {
						
						public void widgetSelected(SelectionEvent arg0) {
							List<PlotDataItem> dataItems = plot.getMultiPlotDataManager().getPlotDataItems();
							if (dataItems != null && dataItems.size() > 0){
								au.gov.ansto.bragg.kakadu.ui.plot.Plot plot2 = null; 
								Collection<PlotView> plotViews = PlotManager.getPlotViews();
								for (PlotView view : plotViews){
									au.gov.ansto.bragg.kakadu.ui.plot.Plot plot = view.getPlotComposite();
									if (plot != null && plot.getOperaton() == null)
										plot2 = view.getPlotComposite();
								}
								if (plot2 != null)
									try {
										PlotDataItem newDataItem = dataItems.get(0).clone();
//										newDataItem.setColor(AbstractDataSource.getNextColor());
										newDataItem.setLinked(false);
										plot2.getMultiPlotDataManager().addPlotDataItem(newDataItem);
									} catch (PlotException e) {
										e.printStackTrace();
									}
							}
						}
						
						public void widgetDefaultSelected(SelectionEvent arg0) {
							
						}
					});
				}
			}
		};
		PlotManager.addOpenNewPlotListener(plotListener);
		
		exportAllButton.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				Rectangle rect = exportAllButton.getBounds ();
//				final Point location = exportAllButton.getLocation();
//				final Point size = exportAllButton.getSize();
				Point pt = new Point (rect.x, rect.y + rect.height);
				pt = exportAllButton.getParent().toDisplay (pt);
				stripChoiceMenu.setLocation (pt.x, pt.y);
				stripChoiceMenu.setVisible (true);
			}

		});
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
			algorithmTask.removeStatusListener(statusListener);
		PlotManager.removeOpenNewPlotListener(plotListener);
	}
	
	@Override
	protected void loadEfficiencyMapConfiguration() {
	}
}
