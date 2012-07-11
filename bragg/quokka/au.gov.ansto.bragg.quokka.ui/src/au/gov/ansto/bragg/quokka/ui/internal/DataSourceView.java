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
package au.gov.ansto.bragg.quokka.ui.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.core.object.ObjectConfigException;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.service.directory.IDirectoryService;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.util.messaging.EventHandler;
import org.osgi.service.event.Event;

import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.kakadu.core.AlgorithmTask;
import au.gov.ansto.bragg.kakadu.core.DataSourceManager;
import au.gov.ansto.bragg.kakadu.core.data.DataItem;
import au.gov.ansto.bragg.kakadu.core.data.DataSourceFile;
import au.gov.ansto.bragg.kakadu.core.data.Operation;
import au.gov.ansto.bragg.kakadu.ui.ProjectManager;
import au.gov.ansto.bragg.kakadu.ui.util.Util;
import au.gov.ansto.bragg.process.agent.ProcessorAgent;
import au.gov.ansto.bragg.process.port.Tuner;
import au.gov.ansto.bragg.process.port.TunerPortListener;
import au.gov.ansto.bragg.quokka.dra.online.util.ScatteringResult;
import au.gov.ansto.bragg.quokka.experiment.report.ExperimentUserReport;
import au.gov.ansto.bragg.quokka.experiment.report.ExperimentUserReportUtils;
import au.gov.ansto.bragg.quokka.experiment.report.SampleResult;

public class DataSourceView extends ViewPart {

	public static final String TRANSMISSION_OPERATOR_NAME = "transmission.op";
	public static final String TRANSMISSIONURI_TUNER_NAME = "frame.txSampleUri";
	public static final String MASTERREPORT_TUNER_NAME = "frame.masterReport.uri";
	public static final String EMPTY_CELL_TRANSMISSION_URI_TUNER_NAME = "frame.txEmptyUri";
	public static final String EMPTY_CELL_SCATTERING_RI_TUNER_NAME = "frame.scatterEmptyUri";
	public static final String BACKGROUND_SCATTERING_URI_TUNER_NAME = "frame.scatterBackgroundUri";
	public static final String BEAM_CENTER_TUNER_NAME = "frame.centroidUri";
	public static final String EFFICIENCY_OPERATOR_NAME = "efficiency.op";
	public static final String SENSITIVITY_URI_TUNER_NAME = "frame.efficiency.mapUri";
	public static final String SCALER_OPERATOR_NAME = "scaler.op";
	public static final String DIRECT_EMPTY_BEAM_TRANSMISSION_URI_TUNER_NAME = "frame.txDirectUri";
	
	private QuokkaDataSourceComposite dataSourceComposite;
	private EventHandler eventHandler;
	private SelectionListener dataSelectionListener;
	private List<ExperimentUserReport> modelList = new ArrayList<ExperimentUserReport>();
	protected Action loadModelAction;

//	private ExperimentUserReport modelList;

	public DataSourceView() {} //empty constructor

	/**
	 * Creates all UI controls for the view.
	 */
	public void createPartControl(Composite parent) {
		ProjectManager.init();

		parent.setLayout(new FillLayout());
		dataSourceComposite = new QuokkaDataSourceComposite(parent, SWT.NONE);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(dataSourceComposite, "au.gov.ansto.bragg.kakadu.dataSourceView");
		
		contributeToActionBars();
		initListeners();
	}

	private void initListeners() {
		eventHandler = new EventHandler(IDirectoryService.EVENT_TOPIC_BIND) {
			@Override
			public void handleEvent(Event event) {
				if (!event.getProperty(IDirectoryService.EVENT_PROP_NAME).equals(ExperimentUserReport.class.getName())) {
					return;
				}
				Object object = event.getProperty(IDirectoryService.EVENT_PROP_OBJECT);
				if (object instanceof ExperimentUserReport) {
					if (ProjectManager.getCurrentAlgorithmTask() == null)
						return;
					final ExperimentUserReport model = (ExperimentUserReport) object;
//					System.out.println(ExperimentResultUtils.getXStream().toXML(model));
					updateModel(model);
//					final String filePath = model.getSensitivityFile();
					boolean isAnalysisForced = true;
					SampleResult sampleScattering = ExperimentUserReportUtils.getScatteringForAnalysis(model);
					if (sampleScattering == null || sampleScattering.getRunId() == null || 
							sampleScattering.getRunId().trim().length() == 0){
						sampleScattering = ExperimentUserReportUtils.getScatteringAOL(model);
						if (sampleScattering == null || sampleScattering.getRunId() == null || 
								sampleScattering.getRunId().trim().length() == 0)
							return;
						isAnalysisForced = false;
					}
					final SampleResult sample = sampleScattering;
					final boolean doAnalysisAnyway = isAnalysisForced;
					System.err.println("**************" + sampleScattering.getRunId());
//					Display.getDefault().asyncExec(new Runnable() {
					SafeUIRunner.asyncExec(new SafeRunnable() {

						public void run() {
							ScatteringResult scatteringResult = new ScatteringResult(model, sample);
							String filePath = scatteringResult.getSampleScatteringFilename();
							if (filePath == null)
								return;
							File file = new File(filePath);
							if (!file.exists()){
								System.err.println("can not find file " + filePath);
								return;
							}
							if (!doAnalysisAnyway && DataSourceManager.getInstance().getDataSourceFile(
									filePath) != null){
								return;
							}
							setAlgorithmParameters(scatteringResult);
//							boolean isFileSelected = DataSourceManager.getSelectedFile() 
//								== DataSourceManager.getInstance().getDataSourceFile(getDataFolder() + "/" + filePath);
//							modelMap.remove(file.getAbsolutePath());
							dataSourceComposite.removeFile(filePath);
							dataSourceComposite.addFile(filePath, true, 0);
							dataSourceComposite.refresh();
//							modelMap.put(file.getAbsolutePath(), model);
						}
					});
				}
			}
		};
		eventHandler.activate();
		
		AlgorithmTask task = ProjectManager.getCurrentAlgorithmTask();
		final Operation transmissionOperation = task.getOperationManager(0).getOperation(
				TRANSMISSION_OPERATOR_NAME);
		final Tuner masterReportTuner = ((ProcessorAgent) transmissionOperation.getAgent()).getTuner(
				MASTERREPORT_TUNER_NAME);
		
		dataSelectionListener = new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Object data = arg0.data;
				if (data instanceof DataSourceFile){
					DataSourceFile dataSourceFile = (DataSourceFile) data;
					String filename = dataSourceFile.getLocalName();
					filename = filename.replace("QKK", "");
					filename = filename.replace(".nx.hdf", "").trim();
					for (ExperimentUserReport model : modelList){
						SampleResult sampleScattering = ExperimentUserReportUtils.getSampleScatteringResult(
								model, filename);
						if (sampleScattering != null){
							ScatteringResult scatteringResult = new ScatteringResult(model, sampleScattering, dataSourceFile.getName());
							try{
								URI reportUri = (URI) masterReportTuner.getSignal();
								if (reportUri != null){
									File file = new File(reportUri);
									if (file.exists()){
										Object object = null;
										try {
											InputStream input = new FileInputStream(file);
											object = ExperimentUserReportUtils.getXStream().fromXML(input);
										} catch (Throwable t) {
											throw new ObjectConfigException("Invalid experiment report file: " + filename, t);
										}
										if (object instanceof ExperimentUserReport){
											final ExperimentUserReport masterModel = (ExperimentUserReport) object;
											updateModel(model);
											scatteringResult.setMasterReport(masterModel);
										}
									}
								}
							}catch (Exception e) {
								e.printStackTrace();
							}
							setAlgorithmParameters(scatteringResult);
							return;
						}
					}
//					ScatteringResult emptyReport = new ScatteringResult();
//					setAlgorithmParameters(emptyReport);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		};
		dataSourceComposite.addSelectionChangedListener(dataSelectionListener);
		
		TunerPortListener tunerListener = new TunerPortListener(masterReportTuner) {
			
			@Override
			public void updateUIValue(Object value) {
				DataSourceFile dataSourceFile = DataSourceManager.getSelectedFile();
				if (dataSourceFile != null){
					String filename = dataSourceFile.getLocalName();
					filename = filename.replace("QKK", "");
					filename = filename.replace(".nx.hdf", "").trim();
					for (ExperimentUserReport model : modelList){
						SampleResult sampleScattering = ExperimentUserReportUtils.getSampleScatteringResult(
								model, filename);
						if (sampleScattering != null){
							ScatteringResult scatteringResult = new ScatteringResult(model, sampleScattering, dataSourceFile.getName());
							try{
								URI reportUri = (URI) masterReportTuner.getSignal();
								if (reportUri != null){
									File file = new File(reportUri);
									if (file.exists()){
										Object object = null;
										try {
											InputStream input = new FileInputStream(file);
											object = ExperimentUserReportUtils.getXStream().fromXML(input);
										} catch (Throwable t) {
											throw new ObjectConfigException("Invalid experiment report file: " + filename, t);
										}
										if (object instanceof ExperimentUserReport){
											final ExperimentUserReport masterModel = (ExperimentUserReport) object;
											updateModel(model);
											scatteringResult.setMasterReport(masterModel);
										}
									}
								}
							}catch (Exception e) {
								e.printStackTrace();
							}
							setAlgorithmParameters(scatteringResult);
							return;
						}
					}
				}
			}
			
			@Override
			public void updateUIOptions(List<?> options) {
			}
			
			@Override
			public void updateUIMin(Object min) {
			}
			
			@Override
			public void updateUIMax(Object max) {
			}
		};
		masterReportTuner.addChangeListener(tunerListener);
	}

	protected void updateModel(ExperimentUserReport model){
		List<ExperimentUserReport> toRemoveList = new ArrayList<ExperimentUserReport>();
		for (ExperimentUserReport report : modelList){
			if (report.getStartTime() == null)
				continue;
			if (report.getStartTime().equals(model.getStartTime())){
				toRemoveList.add(report);
			}
		}
		modelList.removeAll(toRemoveList);
		modelList.add(model);
	}
	
	protected void setAlgorithmParameters(ScatteringResult scatteringResult){
		AlgorithmTask task = ProjectManager.getCurrentAlgorithmTask();
		String sampleTransmission = scatteringResult.getTransmissionFilename();
//		if (sampleTransmission != null){
		final Operation transmissionOperation = task.getOperationManager(0).getOperation(
				TRANSMISSION_OPERATOR_NAME);
		final Tuner sampleTransmissionTuner = ((ProcessorAgent) transmissionOperation.getAgent()).getTuner(
				TRANSMISSIONURI_TUNER_NAME);
		final Tuner emptyCellTransmissionTuner = ((ProcessorAgent) transmissionOperation.getAgent()).getTuner(
				EMPTY_CELL_TRANSMISSION_URI_TUNER_NAME);
		final Tuner emptyCellScatteringTuner = ((ProcessorAgent) transmissionOperation.getAgent()).getTuner(
				EMPTY_CELL_SCATTERING_RI_TUNER_NAME);
		final Tuner backgroundScatteringTuner = ((ProcessorAgent) transmissionOperation.getAgent()).getTuner(
				BACKGROUND_SCATTERING_URI_TUNER_NAME);
		final Tuner beamCenterUriTuner = ((ProcessorAgent) transmissionOperation.getAgent()).getTuner(
				BEAM_CENTER_TUNER_NAME);
		final Operation efficiencyOperation = task.getOperationManager(0).getOperation(
				EFFICIENCY_OPERATOR_NAME);
		final Tuner sensitivityTuner = ((ProcessorAgent) efficiencyOperation.getAgent()).getTuner(
				SENSITIVITY_URI_TUNER_NAME);
		final Operation scalerOperation = task.getOperationManager(0).getOperation(
				SCALER_OPERATOR_NAME);
		final Tuner emptyBeamTransmissionTuner = ((ProcessorAgent) scalerOperation.getAgent()).getTuner(
				DIRECT_EMPTY_BEAM_TRANSMISSION_URI_TUNER_NAME);
		try {
			sampleTransmissionTuner.setSignal(ConverterLib.path2URI(sampleTransmission));
			emptyCellTransmissionTuner.setSignal(ConverterLib.path2URI(scatteringResult.getEmptyCellTransmissionFilename()));
			emptyCellScatteringTuner.setSignal(ConverterLib.path2URI(scatteringResult.getEmptyCellScatteringFilename()));
			backgroundScatteringTuner.setSignal(ConverterLib.path2URI(scatteringResult.getDarkCurrentScatteringFilename()));
			beamCenterUriTuner.setSignal(ConverterLib.path2URI(scatteringResult.getEmptyBeamTransmissionFilename()));
			sensitivityTuner.setSignal(ConverterLib.path2URI(scatteringResult.getSensitivityFilename()));
			emptyBeamTransmissionTuner.setSignal(ConverterLib.path2URI(scatteringResult.getEmptyBeamTransmissionFilename()));									
		} catch (Exception e) {
			e.printStackTrace();
		} 
//		}

	}
	
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		final IMenuManager menuManager = bars.getMenuManager();
		final IToolBarManager toolBarManager = bars.getToolBarManager();

		loadModelAction = new Action() {
			public void run() {
				String[] selectedFiles = Util.selectFilesFromShell(
						dataSourceComposite.getShell(), "*.xml", "Workflow Model Report");
				if (selectedFiles == null || selectedFiles.length == 0)
					return;
				for (int i = 0; i < selectedFiles.length; i++) {
					addModel(selectedFiles[i]);
				}
				dataSourceComposite.refresh();
//				adjustColumnSize();;
			}
		};
		loadModelAction.setText("Load Experiment Report");
		loadModelAction.setToolTipText("Load data from an experiment report");
		loadModelAction.setImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, 
				"icons/table_add_16x16.gif"));

		final List<Object> actionList = dataSourceComposite.getActionList();
		if (actionList != null)
			actionList.add(0, loadModelAction);
		for (Iterator<?> iterator = actionList.iterator(); iterator.hasNext();) {
			Object action = (Object) iterator.next();
			if (action instanceof Action) {
				Action a = (Action) action;
				menuManager.add(a);
				toolBarManager.add(a);
			} else if (action instanceof Separator) {
				Separator s = (Separator) action;
				menuManager.add(s);
				toolBarManager.add(s);
			}
		}
		
	}

	protected void addModel(String filename){
		Object object = null;
		try {
			InputStream input = new FileInputStream(filename);
			object = ExperimentUserReportUtils.getXStream().fromXML(input);
		} catch (Throwable t) {
			throw new ObjectConfigException("Invalid experiment report file: " + filename, t);
		}
		String warningMessage = "";
		if (object instanceof ExperimentUserReport){
			final ExperimentUserReport model = (ExperimentUserReport) object;
			updateModel(model);
//			final String filePath = model.getSensitivityFile();
			List<SampleResult> sampleScatterings = ExperimentUserReportUtils.getAllSampleScatterings(model);
			List<String> missingFiles = new ArrayList<String>();
			for (SampleResult scattering : sampleScatterings){
				if (scattering.getRunId() == null || scattering.getRunId().trim().length() == 0)
					continue;
				final SampleResult sample = scattering;
				System.err.println("**************" + sample.getRunId());
				ScatteringResult scatteringResult = new ScatteringResult(model, sample, filename);
				String filePath = scatteringResult.getSampleScatteringFilename();
				if (filePath == null)
					continue;
				File file = new File(filePath);
				if (!file.exists()){
					System.err.println("can not find file " + filePath);
					missingFiles.add(filePath);
					continue;
				}
				dataSourceComposite.removeFile(filePath);
				dataSourceComposite.addFile(filePath, false);
			}
			if (missingFiles.size() > 0){
				warningMessage = "Can not find the following files: \n";
				for (String missingFilename : missingFiles){
					warningMessage += "\t" + missingFilename + "\n";
				}
				throw new ObjectConfigException(warningMessage);
			}
		}else{
			throw new ObjectConfigException("Invalid experiment report file: " + filename);
		}
	}
	
	/**
	 * Adds data file to the list of source files. 
	 * @param filePath absolute file path to the file.
	 */
	public List<DataItem> addDataSourceFile(String filePath) {
		DataSourceFile sourceFile = dataSourceComposite.addFile(filePath);
		if (sourceFile == null)
			return new ArrayList<DataItem>();
		return sourceFile.getDataItems();
	}
	
	/**
	 * Removes all data files previously added to the view. 
	 */
	public void removeAllDataSourceFiles() {
		dataSourceComposite.removeAll();
		modelList.clear();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		dataSourceComposite.setFocus();
	}
	
	public void setSelectionAll(boolean flag){
		dataSourceComposite.setSelectionAll(flag);
	}
	
	public void selectDataSourceItem(final URI fileUri, String  entryName){
		dataSourceComposite.selectDataSourceItem(fileUri, entryName);
//		dataSourceComposite.selectDataSourceItem(fileUri, entryName);
	}
	
	public void addSelectionChangedListener(SelectionListener listener){
		dataSourceComposite.addSelectionChangedListener(listener);
	}
	
	public void removeSelectionChangedListener(SelectionListener listener){
		dataSourceComposite.removeSelectionChangedListener(listener);
	}
	
	public static String getDataFolder(){
		String folder = System.getProperty("sics.data.path");
		if (folder == null || folder.trim().length() == 0)
			folder = "W:/commissioning";
		return folder;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		dataSourceComposite.removeSelectionChangedListener(
				dataSelectionListener);
		if (eventHandler != null) {
			eventHandler.deactivate();
			eventHandler = null;
		}
		modelList.clear();
	}
	
}