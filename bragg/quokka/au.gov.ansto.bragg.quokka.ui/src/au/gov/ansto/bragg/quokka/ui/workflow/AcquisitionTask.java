/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package au.gov.ansto.bragg.quokka.ui.workflow;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.ScriptContext;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.statushandlers.StatusManager;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.scripting.IScriptBlock;
import org.gumtree.scripting.IScriptExecutor;
import org.gumtree.scripting.ScriptBlock;
import org.gumtree.service.directory.IDirectoryService;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.ui.scripting.viewer.ICommandLineViewer;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;
import org.gumtree.util.string.StringUtils;
import org.gumtree.widgets.swt.util.UIResources;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.WorkflowState;
import org.gumtree.workflow.ui.events.WorkflowStateEvent;
import org.gumtree.workflow.ui.tasks.ScriptEngineTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.quokka.experiment.model.Acquisition;
import au.gov.ansto.bragg.quokka.experiment.model.AcquisitionSetting;
import au.gov.ansto.bragg.quokka.experiment.model.ControlledAcquisition;
import au.gov.ansto.bragg.quokka.experiment.model.Experiment;
import au.gov.ansto.bragg.quokka.experiment.model.SampleEnvironment;
import au.gov.ansto.bragg.quokka.experiment.model.SampleEnvironmentPreset;
import au.gov.ansto.bragg.quokka.experiment.report.ExperimentUserReport;
import au.gov.ansto.bragg.quokka.experiment.report.ExperimentUserReportUtils;
import au.gov.ansto.bragg.quokka.experiment.util.ExperimentModelUtils;
import au.gov.ansto.bragg.quokka.experiment.util.ExperimentScriptGenerator;
import au.gov.ansto.bragg.quokka.experiment.util.ExperimentStateManager;
import au.gov.ansto.bragg.quokka.experiment.util.IExperimentStateListener;
import au.gov.ansto.bragg.quokka.ui.QuokkaUIUtils;
import au.gov.ansto.bragg.quokka.ui.internal.Activator;
import au.gov.ansto.bragg.quokka.ui.internal.InternalImage;
import au.gov.ansto.bragg.quokka.ui.internal.SystemProperties;
import de.kupzog.ktable.KTable;
import de.kupzog.ktable.KTableCellSelectionAdapter;
import de.kupzog.ktable.SWTX;

public class AcquisitionTask extends AbstractExperimentTask {
	
	private static final Logger logger = LoggerFactory.getLogger(AcquisitionTask.class);
	
	private AcquisitionTaskView view;
	
	private Shell currentShell;
	
	@Override
	protected ITaskView createViewInstance() {
		view = new AcquisitionTaskView();
		return view;
	}

	@Override
	public Object run(Object input) {
		/*********************************************************************
		 * [GT-99] Check run pre condition (tertiary shutter)
		 *********************************************************************/
		boolean canRun = QuokkaUIUtils.checkTertiaryShutter(currentShell);
		if (!canRun) {
			// Stop all
			getWorkflow().stop();
			return null;
		}
		
		/*********************************************************************
		 * Update estimate
		 *********************************************************************/
		view.upateTimeEstimation();
		
		/*********************************************************************
		 * Clear previous result
		 *********************************************************************/
		ExperimentModelUtils.clearResult(getExperiment());
		
		/*********************************************************************
		 * Acquire executor
		 *********************************************************************/
		final IScriptExecutor executor = getContext().getSingleValue(IScriptExecutor.class);
		
		// Clear console if viewer is one of the pre-registered console
		final ICommandLineViewer viewer = (ICommandLineViewer) getContext().get(ScriptEngineTask.CONTEXT_KEY_SCRIPT_VIEWER);
		if (viewer != null) {
			SafeUIRunner.asyncExec(new SafeRunnable() {
				public void run() throws Exception {
					viewer.clearConsole();
				}
			});
		}
		
		if (executor == null) {
			// Error
			logger.error("Script executor is missing.");
			return null;
		}
		
		final ExperimentStateManager stateManager = new ExperimentStateManager(getExperiment());
		IExperimentStateListener listener = new IExperimentStateListener() {
			@Override
			public void stateUpdated(int runId) {
				System.err.println("Updated " + runId);
				Acquisition acquisition = stateManager.getAcquisition(runId);
				view.updateUI(acquisition);
			}
		};
		stateManager.addListener(listener);
		
		// Clear previous result
		ExperimentModelUtils.clearResult(getExperiment());
		
		// Mark as start
		ExperimentModelUtils.markStartTime(getExperiment());
		
		// Generate script
		final String script = ExperimentScriptGenerator.generate(getExperiment(), stateManager);
		
		// Execute on the script executor
		executor.getEngine().getContext().setAttribute("stateManager", stateManager, ScriptContext.ENGINE_SCOPE);
		
		// Make sure the executor is not busy in first 5 sec
		LoopRunner.run(new ILoopExitCondition() {
			@Override
			public boolean getExitCondition() {
				return !executor.isBusy();
			}
		}, 5000, 10);
		
		// Run Script
		IScriptBlock block = new ScriptBlock() {
			public String getScript() {
				return script;
			}
		};
		executor.runScript(block);

		// Not thread safe!  We need listener model instead!
		// Wait until it gets busy
		LoopRunner.run(new ILoopExitCondition() {
			@Override
			public boolean getExitCondition() {
				return executor.isBusy();
			}
		}, 5000, 10);
		
		// Check execution status every 1 sec
		LoopRunner.run(new ILoopExitCondition() {
			@Override
			public boolean getExitCondition() {
				return !executor.isBusy();
			}
		}, LoopRunner.NO_TIME_OUT, 1000);
		
		/*****************************************************************
		 * Export
		 *****************************************************************/
		boolean autoExport = SystemProperties.AUTO_EXPORT.getBoolean();
		autoExport = true;
		if (autoExport) {
			try {
				/*********************************************************
				 * Export experiment report
				 *********************************************************/
				// Archive copy
				URI reportFolderURI = new URI(SystemProperties.REPORT_LOCATION.getValue());
				final File reportFolder = EFS.getStore(reportFolderURI).toLocalFile(EFS.NONE, new NullProgressMonitor());
//				ExperimentResult result = ExperimentResultUtils.createExperimentResult(getExperiment());
				// Auto export only when meaningful data are available
//				if (result.getConfigs().size() > 0) {
//					ExperimentResultUtils.exportReport(reportFolder, result);
//				}
				ExperimentUserReport report = ExperimentUserReportUtils.createExperimentUserReport(getExperiment());
				ExperimentUserReportUtils.exportUserReport(reportFolder, report);
				SafeUIRunner.asyncExec(new SafeRunnable() {
					
					@Override
					public void run() throws Exception {
						exportImageReport(reportFolder);
					}
				});
				
				// User copy
				final String userReportFolderLocation = getExperiment().getUserReportDirectory();
				if (userReportFolderLocation != null) {
					final File userReportFolder = new File(userReportFolderLocation);
					// Auto export only when meaningful data are available
					if (getExperiment().getInstrumentConfigs().size() > 0) {
						ExperimentUserReportUtils.exportUserReport(userReportFolder, report);
						SafeUIRunner.asyncExec(new SafeRunnable() {
							@Override
							public void run() throws Exception {
								exportImageReport(userReportFolder);
							}
						});
//						ExperimentResultUtils.exportReport(userReportFolder, result);
					}
				}
				
				/*********************************************************
				 * Export console log
				 *********************************************************/
				SafeUIRunner.asyncExec(new SafeRunnable() {
					@Override
					public void run() throws Exception {
						ICommandLineViewer commandLineViewer = (ICommandLineViewer) getContext()
								.get(ScriptEngineTask.CONTEXT_KEY_SCRIPT_VIEWER);
						String outputText = commandLineViewer == null ? "" : commandLineViewer.getConsoleText();
						ExperimentModelUtils.exportConsoleLog(reportFolder, outputText);
						if (userReportFolderLocation != null) {
							File userReportFolder = new File(userReportFolderLocation);
							ExperimentModelUtils.exportConsoleLog(userReportFolder, outputText);
						}
					}
				});
			} catch (URISyntaxException e) {
				logger.error("Incorrect report folder location.", e);
			} catch (CoreException e) {
				logger.error("Error in openning report folder location.", e);
			} catch (IOException e) {
				logger.error("Error in writing report.", e);
			}
		}
		return null;
	}
	
	private void exportImageReport(File folder) throws IOException {
		
		if (!folder.exists()) {
			folder.mkdir();
		}
		
		// Write report
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
		final File reportFile = new File(folder, "QKK_" + format.format(Calendar.getInstance().getTime()) + "_report.jpg");
		if (!reportFile.exists()) {
			reportFile.createNewFile();
		}
		
		int sizeX = 0;
		int sizeY = 0;
		final KTable table = view.table;
//		Point tableSize = table.computeSize(table.getSize().x, table.getSize().y);
		Point tableSize = table.getSize();
		final Image tableImage = new Image(table.getDisplay(), tableSize.x, tableSize.y);
		GC gc2 = new GC(tableImage);
		table.print(gc2);
		gc2.dispose();
		
//		if (modePanel != null) {
//			GC gc2 = new GC(modePanel);
//			gc2.copyArea(image, 0, 0);
//			gc2.dispose();
//			modePanel.print(gc2);
//		}
//		GC gc2 = new GC(table);
//		gc2.copyArea(image, 0, sizeY - tableSize.y);
//		table.print(gc2);

		Point panelSize = null;
		final Control modePanel = view.getTopControl();
		Image modeImage = null;
		if (modePanel != null){
//			panelSize = modePanel.computeSize(modePanel.getSize().x, modePanel.getSize().y);
			panelSize = modePanel.getSize();
			final Image panelImage = new Image(modePanel.getDisplay(), panelSize.x, panelSize.y);
			gc2 = new GC(panelImage);
			modePanel.print(gc2);
			gc2.dispose();
			sizeX = panelSize.x;
			sizeY = panelSize.y;
			modeImage = panelImage;
		}
		
		sizeX = sizeX > tableSize.x ? sizeX : tableSize.x;
		sizeY += tableSize.y;
		
		final Image cmbImage = new Image(table.getDisplay(), sizeX, sizeY);
		gc2 = new GC(cmbImage);
		if (modePanel != null) {
			gc2.drawImage(modeImage, 0, 0, panelSize.x, panelSize.y, 0, 0, panelSize.x, panelSize.y);			
		}
		gc2.drawImage(tableImage, 0, 0, tableSize.x, tableSize.y, 0, sizeY - tableSize.y, tableSize.x, tableSize.y);
		gc2.dispose();
		ImageLoader saver = new ImageLoader();
		saver.data = new ImageData[] { cmbImage.getImageData() };
		saver.save(reportFile.getAbsolutePath(), SWT.IMAGE_PNG);
		tableImage.dispose();
		modeImage.dispose();
		tableImage.dispose();
	}
	
	protected void handleStop() {
		// And interrupt the current script block by interrupting SICS
		try {
			SicsCore.getSicsController().interrupt();
		} catch (SicsIOException e) {
			e.printStackTrace();
		}
	}
	
	private class AcquisitionTaskView extends AbstractTaskView {

		// Listener for normal / controlled environment mode changes
		private PropertyChangeListener modeListener;
		
		private PropertyChangeListener sampleEnvironmentsListener;
		
		private Map<SampleEnvironment, SampleEnvironmentUIContext> sampleEnvContexts;
		
		private Map<Acquisition, ScanTableModel> tableModelMap;
		
		private Text collectionText;
		
		private Text configText;
		
		private Text totalText;
		
		private KTable table;
		
		private StackLayout stackLayout;
		
		private int currentNumber;
		
		private IEventHandler<WorkflowStateEvent> workfloEventHandler;
		
		@Override
		public void createPartControl(final Composite parent) {
			parent.setLayout(new GridLayout());
			currentShell = parent.getShell();
			sampleEnvContexts = new HashMap<SampleEnvironment, SampleEnvironmentUIContext>();
			tableModelMap = new HashMap<Acquisition, ScanTableModel>();
			
			/*****************************************************************
			 * Utility buttons
			 *****************************************************************/
			Composite buttonArea = getToolkit().createComposite(parent);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(buttonArea);
			createButtonArea(buttonArea);
			
			/*****************************************************************
			 * Sample environment bar
			 *****************************************************************/
			Composite environmentArea = getToolkit().createComposite(parent);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(environmentArea);
			stackLayout = new StackLayout();
			environmentArea.setLayout(stackLayout);
			
			final Composite normalEnvComposite = getToolkit().createComposite(environmentArea);
			createNormalEnvironmentBar(normalEnvComposite);
			final Composite controlledEnvComposite = getToolkit().createComposite(environmentArea);
			createSampleEnvironmentBar(controlledEnvComposite);
			
			// Default setting
			if (getExperiment().isControlledEnvironment()) {
				stackLayout.topControl = controlledEnvComposite;
			} else {
				stackLayout.topControl = normalEnvComposite;
			}
			
			// Listen to mode (normal or controlled) switch
			modeListener = new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if (getExperiment().isControlledEnvironment()) {
						stackLayout.topControl = controlledEnvComposite;
					} else {
						stackLayout.topControl = normalEnvComposite;
					}
					parent.layout(true, true);
				}				
			};
			getExperiment().addPropertyChangeListener(Experiment.PROP_CONTROLLED_ACQUISITION, modeListener);
						
			// Listen to sample environment structural change
			sampleEnvironmentsListener = new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					resetSampleEnviromentBar(controlledEnvComposite);
				}
			};
			getExperiment().addPropertyChangeListener(Experiment.PROP_SAMPLE_ENVIRONMENTS, sampleEnvironmentsListener);
			
			/*****************************************************************
			 * Scan Table
			 *****************************************************************/
			Composite mainArea = getToolkit().createComposite(parent);
			GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 450).applyTo(mainArea);
			createMainArea(mainArea);

			/*****************************************************************
			 * Table operation buttons
			 *****************************************************************/
//			Composite tableButtonArea = getToolkit().createComposite(parent);
//			GridDataFactory.fillDefaults().grab(true, false).applyTo(tableButtonArea);
//			createTableButtonArea(tableButtonArea);
			
			/*****************************************************************
			 * Status and time estimate
			 *****************************************************************/ 
			Composite statusArea = getToolkit().createComposite(parent);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(statusArea);
			createStatusArea(statusArea);
		}
		
		private Control getTopControl(){
			if (stackLayout == null) {
				return null;
			}
			return stackLayout.topControl;
		}
		
		private void createButtonArea(final Composite parent) {
			parent.setLayout(new GridLayout(10, false));
			
			/*****************************************************************
			 * Label
			 *****************************************************************/
			Label label = getToolkit().createLabel(parent, "Edit:");
			label.setFont(UIResources.getDefaultFont(SWT.BOLD));
			GridDataFactory.swtDefaults().span(5, 0).applyTo(label);
			
			label = getToolkit().createLabel(parent, "Preview:");
			label.setFont(UIResources.getDefaultFont(SWT.BOLD));
			GridDataFactory.swtDefaults().span(1, 0).applyTo(label);
			
			label = getToolkit().createLabel(parent, "Export:");
			label.setFont(UIResources.getDefaultFont(SWT.BOLD));
			GridDataFactory.swtDefaults().span(4, 0).applyTo(label);
			
			/*****************************************************************
			 * Create duplicate
			 *****************************************************************/
			final Button duplicateButton = getToolkit().createButton(parent, "Duplicate", SWT.PUSH);
			duplicateButton.setImage(InternalImage.COPY.getImage());
			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(duplicateButton);
			duplicateButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					// TODO
					int[] rows = table.getRowSelection();
					if (rows.length > 0) {
						Acquisition acquisition = ((ScanTableModel) table
								.getModel()).getAcquisition();
						// entry 0 starts at row 2
						int index = rows[0] - 2;
						// Duplicate entry ()
						acquisition.handleDuplicateEntry(index);
						// Update table
						table.setModel(table.getModel());
						// Reselect row
						table.setSelection(0, rows[0], true);
						logger.info("Duplicated entry " + index);
					}
				}
			});
			
			/*****************************************************************
			 * Create remove
			 *****************************************************************/
			final Button removeButton = getToolkit().createButton(parent, "Remove", SWT.PUSH);
			removeButton.setImage(InternalImage.DELETE.getImage());
			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(removeButton);
			removeButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					// TODO
					int[] rows = table.getRowSelection();
					if (rows.length > 0) {
						Acquisition acquisition = ((ScanTableModel) table
								.getModel()).getAcquisition();
						// entry 0 starts at row 2
						int index = rows[0] - 2;
						// Duplicate entry ()
						acquisition.handleRemoveEntry(index);
						// Update table
						table.setModel(table.getModel());
						logger.info("remove entry " + index);
					}
				}
			});
			
			/*****************************************************************
			 * Create up
			 *****************************************************************/
			final Button upButton = getToolkit().createButton(parent, "Up", SWT.PUSH);
			upButton.setImage(InternalImage.UP.getImage());
			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(upButton);
			upButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					int[] rows = table.getRowSelection();
					Acquisition acquisition = ((ScanTableModel) table
							.getModel()).getAcquisition();
					if (rows.length > 0 && rows[0] > 2) {
						int index = rows[0] - 2;
						acquisition.handleSwapEntries(index - 1, index);
						// Update table
						table.setModel(table.getModel());
						// Reselect row
						table.setSelection(0, rows[0] - 1, true);
						logger.info("Moved entry " + index);
					}
				}
			});
			
			/*****************************************************************
			 * Create down
			 *****************************************************************/
			final Button downButton = getToolkit().createButton(parent, "Down", SWT.PUSH);
			downButton.setImage(InternalImage.DOWN.getImage());
			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(downButton);
			downButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					int[] rows = table.getRowSelection();
					Acquisition acquisition = ((ScanTableModel) table
							.getModel()).getAcquisition();
					if (rows.length > 0 && rows[0] >= 2 && rows[0] < acquisition.getEntries().size() + 1) {
						int index = rows[0] - 2;
						acquisition.handleSwapEntries(index, index + 1);
						// Update table
						table.setModel(table.getModel());
						// Reselect row
						table.setSelection(0, rows[0] + 1, true);
						logger.info("Moved entry " + index);
					}
				}
			});
			
			/*****************************************************************
			 * Import Excel
			 *****************************************************************/
			Button importExcel = getToolkit().createButton(parent, "Import Excel", SWT.PUSH);
			importExcel.setImage(InternalImage.IMPORT_FILE.getImage());
			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).grab(true, false).applyTo(importExcel);
			importExcel.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					FileDialog dialog = new FileDialog(parent.getShell(), SWT.OPEN);
					String filename = dialog.open();
					try {
						ExperimentModelUtils.refineExperimentFromExcel(getExperiment(), filename);
					} catch (IOException ioe) {
						StatusManager.getManager().handle(
								new Status(IStatus.ERROR, Activator.PLUGIN_ID,
										"Failed to load from Excel file.", ioe),
								StatusManager.SHOW);
					}
				}
			});
			
			/*****************************************************************
			 * Create preview
			 *****************************************************************/
			Button previewButton = getToolkit().createButton(parent, "Script Preview", SWT.PUSH);
			previewButton.setImage(InternalImage.PREVIEW.getImage());
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(previewButton);
			previewButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					String script = ExperimentScriptGenerator.generate(getExperiment());
					ScriptPreviewDialog dialog = new ScriptPreviewDialog(script);
					dialog.setBlockOnOpen(true);
					dialog.open();
				}
			});
			
			/*****************************************************************
			 * Create script export
			 *****************************************************************/
			Button exportButton = getToolkit().createButton(parent, "Script (Python)", SWT.PUSH);
			exportButton.setImage(InternalImage.SAVE.getImage());
			exportButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					FileDialog dialog = new FileDialog(parent.getShell(), SWT.SAVE);
					dialog.setText("Export script");
			        String[] filterExt = { "*.py" };
			        dialog.setFilterExtensions(filterExt);
					String selectedFile = dialog.open();
					if (selectedFile != null) {
						try {
							FileWriter writer = new FileWriter(selectedFile);
							String script = ExperimentScriptGenerator.generate(getExperiment());
							if (Platform.getOS().equals(Platform.OS_WIN32)) {
								script = script.replace("\n", "\r\n");
							}
							writer.write(script);
							writer.flush();
							writer.close();
						} catch (IOException ioe) {
							String errorMessage = "Failed to export script to " + selectedFile;
							logger.error(errorMessage, ioe);
							StatusManager.getManager().handle(
									new Status(IStatus.ERROR,
											Activator.PLUGIN_ID, IStatus.OK,
											errorMessage, ioe),
									StatusManager.SHOW);
						}
					}
				}
			});
			GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).grab(false, false).applyTo(exportButton);

			/*****************************************************************
			 * Create table image export [GT-110]
			 *****************************************************************/
			Button exportTableImageButton = getToolkit().createButton(parent, "Table (Image)", SWT.PUSH);
			exportTableImageButton.setImage(InternalImage.IMAGE.getImage());
			exportTableImageButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					// create and populate image object
					Control control = (Control) table;
					final Point size = control.getSize();
					final Image image = new Image(control.getDisplay(), size.x, size.y);
					GC gc = new GC(control);
					gc.copyArea(image, 0, 0);
					ImageData imageData = image.getImageData();
					
					// Prepare file dialog
					FileDialog dialog = new FileDialog(parent.getShell(), SWT.SAVE);
					dialog.setFilterExtensions(new String[]{"*.bmp", "*.jpg", "*.png"});
					dialog.setFilterNames(new String[]{"Windows Bitmap (*.bmp)", 
							"JPEG File Interchange Format (*.jpg)", 
							"Portable Network Graphics (*.png)"
							});
					String saveFilename = dialog.open();
					if (saveFilename != null) {
						ImageLoader imageLoader = new ImageLoader();
						imageLoader.data = new ImageData[] {imageData};
						
						int fileFormat = SWT.IMAGE_PNG;
						String fileExt = saveFilename.substring(saveFilename.lastIndexOf('.') + 1);
						if (fileExt.equalsIgnoreCase("jpg")) {
							fileFormat = SWT.IMAGE_JPEG;
						} else if (fileExt.equalsIgnoreCase("png")) {
							fileFormat = SWT.IMAGE_PNG;
						} else if (fileExt.equalsIgnoreCase("gif")) {
							fileFormat = SWT.IMAGE_GIF;
						} else if (fileExt.equalsIgnoreCase("tif")) {
							fileFormat = SWT.IMAGE_TIFF;
						} else if (fileExt.equalsIgnoreCase("bmp")) {
							fileFormat = SWT.IMAGE_BMP;
						} else {
							fileFormat = SWT.IMAGE_PNG;		
						}
						
						imageLoader.save(saveFilename, fileFormat);
					}
					
					// Dispose
					image.dispose();
					gc.dispose();
				}
			});
			GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).grab(false, false).applyTo(exportTableImageButton);

			/*****************************************************************
			 * Create table excel export [GUMTREE-779]
			 *****************************************************************/
			Button exportTableExcelButton = getToolkit().createButton(parent, "Table (Excel)", SWT.PUSH);
			exportTableExcelButton.setImage(InternalImage.EXCEL.getImage());
			exportTableExcelButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					// Prepare file dialog
					FileDialog dialog = new FileDialog(parent.getShell(), SWT.SAVE);
					dialog.setFilterExtensions(new String[]{"*.xls"});
					dialog.setFilterNames(new String[]{ "Excel (*.xls)" });
					String saveFilename = dialog.open();
					if (saveFilename != null) {
						try {
							ExperimentModelUtils.saveExperimentToExcel(getExperiment(), saveFilename);
						} catch (IOException e1) {
							logger.error("Failed to create Excel file from experiment", e1);
						}
					}
				}
			});
				
			GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).grab(false, false).applyTo(exportTableImageButton);

			/*****************************************************************
			 * Create result export
			 *****************************************************************/
			Button resultExportButton = getToolkit().createButton(parent, "Result (XML)", SWT.PUSH);
			resultExportButton.setImage(InternalImage.EXPORT.getImage());
			resultExportButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					FileDialog dialog = new FileDialog(parent.getShell(), SWT.SAVE);
					dialog.setText("Export result");
			        String[] filterExt = { "*.xml" };
			        dialog.setFilterExtensions(filterExt);
					String selectedFile = dialog.open();
					if (selectedFile != null) {
						try {
							FileWriter writer = new FileWriter(selectedFile);
//							ExperimentResult result = ExperimentResultUtils.createExperimentResult(getExperiment());
//							ExperimentResultUtils.getXStream().toXML(result, writer);
							ExperimentUserReport report = ExperimentUserReportUtils.createExperimentUserReport(getExperiment());
							ExperimentUserReportUtils.getXStream().toXML(report, writer);
							writer.flush();
							writer.close();
						} catch (IOException ioe) {
							String errorMessage = "Failed to export result to " + selectedFile;
							logger.error(errorMessage, ioe);
							StatusManager.getManager().handle(
									new Status(IStatus.ERROR,
											Activator.PLUGIN_ID, IStatus.OK,
											errorMessage, ioe),
									StatusManager.SHOW);
						}
					}
				}
			});
			GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).grab(false, false).applyTo(resultExportButton);
			
			// Event handler to control buttons availablility
			workfloEventHandler = new IEventHandler<WorkflowStateEvent>() {
				public void handleEvent(WorkflowStateEvent event) {
					if (event.getState().equals(WorkflowState.RUNNING) || 
							event.getState().equals(WorkflowState.SCHEDULED)) {
						SafeUIRunner.asyncExec(new SafeRunnable() {
							public void run() throws Exception {
								duplicateButton.setEnabled(false);
								removeButton.setEnabled(false);
								upButton.setEnabled(false);
								downButton.setEnabled(false);
							}						
						});
					} else if (event.getState().equals(WorkflowState.STOPPING)) {
						SafeUIRunner.asyncExec(new SafeRunnable() {
							public void run() throws Exception {
								duplicateButton.setEnabled(false);
								removeButton.setEnabled(false);
								upButton.setEnabled(false);
								downButton.setEnabled(false);
							}						
						});
					} else  {
						SafeUIRunner.asyncExec(new SafeRunnable() {
							public void run() throws Exception {
								duplicateButton.setEnabled(true);
								removeButton.setEnabled(true);
								upButton.setEnabled(true);
								downButton.setEnabled(true);
							}						
						});
					}
				}
			};
			getWorkflow().addEventListener(workfloEventHandler);
		}
		
		private void createNormalEnvironmentBar(Composite parent) {
			parent.setLayout(new FillLayout());
			Label label = getToolkit().createLabel(parent, "Normal Environment");
			label.setFont(UIResources.getDefaultFont(SWT.BOLD));
		}
		
		private void createSampleEnvironmentBar(Composite parent) {
			List<SampleEnvironment> envs = getExperiment().getSampleEnvironments();
			parent.setLayout(new GridLayout(envs.size() * 2, false));
			
			// First row
			for (SampleEnvironment env : envs) {
				// Controller label
				Label label = getToolkit().createLabel(parent, env.getControllerId());
				GridDataFactory.fillDefaults().span(2, 1).applyTo(label);
				// Create UI context
				SampleEnvironmentUIContext context = new SampleEnvironmentUIContext();
				context.sampleEnvironment = env;
				context.label = label;
				sampleEnvContexts.put(env, context);
			}
			
			// Second row
			for (final SampleEnvironment env : envs) {
				/*************************************************************
				 * UI
				 *************************************************************/
				// Comboviewer
				ComboViewer comboViewer = new ComboViewer(parent, SWT.READ_ONLY); 
				GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(comboViewer.getCombo());
				sampleEnvContexts.get(env).comboViewer = comboViewer;
				comboViewer.setContentProvider(new ArrayContentProvider());
				comboViewer.setLabelProvider(new LabelProvider() {
					public String getText(Object element) {
						return Float.toString(((SampleEnvironmentPreset) element).getPreset());
					}
				});
				comboViewer.setInput(env.getPresets().toArray());

				// Arrow icon
				if (envs.indexOf(env) < envs.size() - 1) {
					Label label = getToolkit().createLabel(parent, "");
					label.setImage(InternalImage.RIGHT_ARROW.getImage());
					GridDataFactory.fillDefaults().applyTo(label);
				}

				/*************************************************************
				 * Listeners
				 *************************************************************/
				// Listen to env preset selection
				comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						// Update wait time
						SampleEnvironmentPreset preset = (SampleEnvironmentPreset) ((IStructuredSelection) sampleEnvContexts.get(env).comboViewer.getSelection()).getFirstElement();
						
						// Update model
						Map<SampleEnvironment, SampleEnvironmentPreset> searchMap = new HashMap<SampleEnvironment, SampleEnvironmentPreset>();
						for (SampleEnvironmentUIContext context : sampleEnvContexts.values()) {
							if (context.comboViewer == null) {
								// Not ready
								return;
							}
							Object selection =  ((IStructuredSelection) context.comboViewer.getSelection()).getFirstElement();
							if (selection == null) {
								// Not ready
								return;
							}
							SampleEnvironmentPreset sampleEnvPreset = (SampleEnvironmentPreset) selection;
							searchMap.put(context.sampleEnvironment, sampleEnvPreset);
						}
						ControlledAcquisition acquisition = getExperiment().findControlledAcquisition(searchMap);
						if (acquisition != null) {
							ScanTableModel tableModel = new ScanTableModel(table, getExperiment(), acquisition);
							table.setModel(tableModel);
							tableModelMap.put(acquisition, tableModel);
						}
					}		
				});
				
				// Listen to sample environment preset non-structural changes
				final PropertyChangeListener sampleEnvPresetListener = new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						SafeUIRunner.asyncExec(new SafeRunnable() {
							@Override
							public void run() throws Exception {
								// Refresh as this is non-structural change
								sampleEnvContexts.get(env).comboViewer.refresh();
							}
						});
					}					
				};
				
				// Listen to sample environment non-structural + preset structural changes
				PropertyChangeListener sampleEnvironmentChangeListener = new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent event) {
						// Change label
						sampleEnvContexts.get(env).label.setText(env.getControllerId());
						// Update presets
						sampleEnvContexts.get(env).comboViewer.setInput(env.getPresets().toArray());
						// Default selection
						if (env.getPresets().size() > 0) {
							sampleEnvContexts.get(env).comboViewer.setSelection(new StructuredSelection(env.getPresets().get(0)));
						}
						// Listen to sample environment preset value changes
						for (SampleEnvironmentPreset preset : env.getPresets()) {
							preset.removePropertyChangeListener(sampleEnvPresetListener);
							preset.addPropertyChangeListener(sampleEnvPresetListener);
						};
					}
				};
				env.addPropertyChangeListener(sampleEnvironmentChangeListener);
				sampleEnvContexts.get(env).sampleEnvironmentChangeListener = sampleEnvironmentChangeListener;
				
				/*************************************************************
				 * Default settings
				 *************************************************************/
				// Default selection
				if (env.getPresets().size() > 0) {
					comboViewer.setSelection(new StructuredSelection(env.getPresets().get(0)));
				}
			}
			
			// Auto track button
			Button autoTrackButton = getToolkit().createButton(parent, "Auto track", SWT.CHECK);
			GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).grab(true, false).applyTo(autoTrackButton);
			autoTrackButton.setSelection(true);
		}
		
		private void resetSampleEnviromentBar(final Composite parent) {
			SafeUIRunner.asyncExec(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					// Dispose all widgets
					for (Control child : parent.getChildren()) {
						child.dispose();
					}
					// Dispose all listeners
					for (SampleEnvironmentUIContext context : sampleEnvContexts.values()) {
						context.dispose();
					}
					// Clear UI cache
					sampleEnvContexts.clear();
					// Recreate UI
					createSampleEnvironmentBar(parent);
					// Refreh UI
					parent.getParent().getParent().layout(true, true);
//					// UI refresh hack for wizard viewer
//					Composite topControl = controlledEnvComposite.getParent().getParent().getParent().getParent(); 
//					if (topControl instanceof ScrolledForm) {
//						ScrolledForm scrolledForm = (ScrolledForm) topControl;
//						scrolledForm.reflow(true);
//					}
				}
			});
		}
		
		private void createMainArea(final Composite parent) {
			parent.setLayout(new FillLayout());
			table = new KTable(parent, SWT.BORDER | SWT.FULL_SELECTION | SWTX.AUTO_SCROLL);
			getToolkit().adapt(table);
			final ScanTableModel tableModel = new ScanTableModel(table, getExperiment(), getExperiment().getNormalAcquisition());
			tableModelMap.put(getExperiment().getNormalAcquisition(), tableModel);
			table.setModel(tableModel);
			
			// Table selection listener
			table.addCellSelectionListener(new KTableCellSelectionAdapter() {
				public void cellSelected(int col, int row, int statemask) {
					fireLiveReductionRequest(col, row);
				}
				public void fixedCellSelected(int col, int row, int statemask) {
					fireLiveReductionRequest(col, row);
				}
			});
			
			// Listen to config structural changes
			getExperiment().addPropertyChangeListener("instrumentConfigs", new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					SafeUIRunner.asyncExec(new SafeRunnable() {
						@Override
						public void run() throws Exception {
							// Manual update
							//table.setModel(tableModel);
							//tableModelMap.put(tableModel.getAcquisition(), tableModel);
							
							// Update model
							Map<SampleEnvironment, SampleEnvironmentPreset> searchMap = new HashMap<SampleEnvironment, SampleEnvironmentPreset>();
							for (SampleEnvironmentUIContext context : sampleEnvContexts.values()) {
								if (context.comboViewer == null) {
									// Not ready
									return;
								}
								Object selection =  ((IStructuredSelection) context.comboViewer.getSelection()).getFirstElement();
								if (selection == null) {
									// Not ready
									return;
								}
								SampleEnvironmentPreset sampleEnvPreset = (SampleEnvironmentPreset) selection;
								searchMap.put(context.sampleEnvironment, sampleEnvPreset);
							}
							ControlledAcquisition acquisition = getExperiment().findControlledAcquisition(searchMap);
							if (acquisition != null) {
								ScanTableModel tableModel = new ScanTableModel(table, getExperiment(), acquisition);
								table.setModel(tableModel);
								tableModelMap.put(acquisition, tableModel);
							}
						}
					});
				}				
			});
		}

		private void createTableButtonArea(Composite parent) {
			parent.setLayout(new GridLayout(7, false));
			
			Button upButton = getToolkit().createButton(parent, "UP", SWT.PUSH);
			upButton.setImage(InternalImage.UP.getImage());
			GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).grab(true, false).applyTo(upButton);
			
			Button downButton = getToolkit().createButton(parent, "DOWN", SWT.PUSH);
			downButton.setImage(InternalImage.DOWN.getImage());
			GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).grab(false, false).applyTo(downButton);
			
			Label separator = getToolkit().createLabel(parent, "       ");
			GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).grab(false, false).applyTo(separator);
			
			Button duplicateButton = getToolkit().createButton(parent, "Duplicate", SWT.PUSH);
			duplicateButton.setImage(InternalImage.SPLIT.getImage());
			GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).grab(false, false).applyTo(duplicateButton);
			
			Button removeButton = getToolkit().createButton(parent, "Remove", SWT.PUSH);
			removeButton.setImage(InternalImage.DELETE.getImage());
			GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).grab(false, false).applyTo(removeButton);
			
			separator = getToolkit().createLabel(parent, "       ");
			GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).grab(false, false).applyTo(separator);
			
			Button restoreButton = getToolkit().createButton(parent, "Restore", SWT.PUSH);
			restoreButton.setImage(InternalImage.UNDO.getImage());
			GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).grab(false, false).applyTo(restoreButton);	
		}
		
		private void createStatusArea(Composite parent) {
			GridLayoutFactory.swtDefaults().numColumns(7).applyTo(parent);
			
			/*****************************************************************
			 * Labels row
			 *****************************************************************/
			Label label = getToolkit().createLabel(parent, "");
			GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).grab(true, false).applyTo(label);
			
			label = getToolkit().createLabel(parent, "Data collection time");
			label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
			
			label = getToolkit().createLabel(parent, "");
			
			label = getToolkit().createLabel(parent, "Configuration time");
			label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
			
			label = getToolkit().createLabel(parent, "");
			
			label = getToolkit().createLabel(parent, "Total Estimated Time");
			label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
			
			label = getToolkit().createLabel(parent, "");
			
			/*****************************************************************
			 * Texts row
			 *****************************************************************/
			label = getToolkit().createLabel(parent, "");
			GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).grab(true, false).applyTo(label);
			
			collectionText = getToolkit().createText(parent, "--", SWT.READ_ONLY | SWT.RIGHT);
			GridDataFactory.fillDefaults().applyTo(collectionText);
			
			label = getToolkit().createLabel(parent, " + ");
			
			configText = getToolkit().createText(parent, "--", SWT.READ_ONLY | SWT.RIGHT);
			GridDataFactory.fillDefaults().applyTo(configText);
			
			label = getToolkit().createLabel(parent, " = ");
			
			totalText = getToolkit().createText(parent, "--", SWT.READ_ONLY | SWT.RIGHT);
			GridDataFactory.fillDefaults().applyTo(totalText);
			
			Button updateButton = getToolkit().createButton(parent, "Update", SWT.PUSH);
			updateButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					upateTimeEstimation();
				}
			});
		}
		
		private void upateTimeEstimation() {
			SafeUIRunner.asyncExec(new SafeRunnable() {
				public void run() throws Exception {
					long runtTime = ExperimentModelUtils.calculateEstimatedRunTime(getExperiment());
					collectionText.setText(StringUtils.formatTime(runtTime));
					long configTime = ExperimentModelUtils.calculateEstimatedConfigTime(getExperiment());
					configText.setText(StringUtils.formatTime(configTime));
					long totalTime = runtTime + configTime;
					totalText.setText(StringUtils.formatTime(totalTime));
				}
			});		
		}
		
		private void fireLiveReductionRequest(int col, int row) {
			AcquisitionSetting setting = ((ScanTableModel) table.getModel()).getAcquisitionSetting(col, row);
			if (setting != null) {
				List<AcquisitionSetting> settingToProcess = new ArrayList<AcquisitionSetting>();
				settingToProcess.add(setting);
				// [2009-11-12] To be removed later
//				ExperimentResult result = ExperimentResultUtils.createExperimentResult(getExperiment(), settingToProcess);
//				GTPlatform.getDirectoryService().bind(ExperimentResult.class.getName(), result);
				// Update
				ExperimentUserReport report = ExperimentUserReportUtils.createExperimentUserReport(getExperiment(), setting);
				ServiceUtils.getService(IDirectoryService.class).bind(ExperimentUserReport.class.getName(), report);
			}
		}
		
		
		
/*****************************************************************************
 * 
 * UI update logic
 * 
 *****************************************************************************/
		
		// Entry method to update UI
		private void updateUI(Acquisition acquisition) {
			if (table != null && !table.isDisposed()) {
				if (acquisition instanceof ControlledAcquisition) {
					updateControlledAcquisitionUI((ControlledAcquisition) acquisition);
				} else {
					updateNormalAcquisitionUI(acquisition);
				}
			}
		}
		
		// Updates UI for normal acquisition
		private void updateControlledAcquisitionUI(ControlledAcquisition acquisition) {
			for (Entry<SampleEnvironment, SampleEnvironmentPreset> entry: acquisition.getEnvSettings().entrySet()) {
				SampleEnvironment env = entry.getKey();
				final SampleEnvironmentPreset preset = entry.getValue();
				final SampleEnvironmentUIContext uiContext = sampleEnvContexts.get(env);
				SafeUIRunner.asyncExec(new SafeRunnable() {
					@Override
					public void run() throws Exception {
						ISelection selection = new StructuredSelection(preset);
						int newNumber = preset.getNumber();
						if (newNumber == currentNumber + 1){
							int selectedNumber = ((SampleEnvironmentPreset) ((StructuredSelection) uiContext.comboViewer.getSelection()).getFirstElement()).getNumber();
							if (selectedNumber != currentNumber) {
								Object obj = uiContext.comboViewer.getElementAt(currentNumber - 1);
								if (obj != null) {
									uiContext.comboViewer.setSelection(new StructuredSelection(obj));
								}
							}
							URI reportFolderURI = new URI(SystemProperties.REPORT_LOCATION.getValue());
							final File reportFolder = EFS.getStore(reportFolderURI).toLocalFile(EFS.NONE, new NullProgressMonitor());
							exportImageReport(reportFolder);
						}
						uiContext.comboViewer.setSelection(selection);
						currentNumber = newNumber;
					}
				});
			}
		}
		
		// Updates UI for normal acquisition
		private void updateNormalAcquisitionUI(Acquisition acquisition) {
			final ScanTableModel model = tableModelMap.get(acquisition);
			SafeUIRunner.asyncExec(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					table.setModel(model);
					System.out.println("Table updated");
				}
			});
		}
		
/*****************************************************************************
 * 
 * UI dispose logic
 * 
 *****************************************************************************/
		
		/* (non-Javadoc)
		 * @see org.gumtree.workflow.ui.AbstractTaskView#dispose()
		 */
		public void dispose() {
			if (modeListener != null) {
				getExperiment().removePropertyChangeListener(Experiment.PROP_CONTROLLED_ACQUISITION, modeListener);
				modeListener = null;
			}
			if (sampleEnvironmentsListener != null) {
				getExperiment().removePropertyChangeListener(Experiment.PROP_SAMPLE_ENVIRONMENTS, sampleEnvironmentsListener);
				sampleEnvironmentsListener = null;
			}
			if (sampleEnvContexts != null) {
				sampleEnvContexts.clear();
				sampleEnvContexts = null;
			}
			if (getWorkflow() != null) {
				getWorkflow().removeEventListener(workfloEventHandler);
				workfloEventHandler = null;
			}
			table = null;
			collectionText = null;
			configText = null;
			totalText = null;
			super.dispose();
		}
		
/*****************************************************************************
 * 
 * Context model
 * 
 *****************************************************************************/
		
		// UI context model
		private class SampleEnvironmentUIContext {
			private SampleEnvironment sampleEnvironment;
			private Label label;
			private ComboViewer comboViewer;
			private PropertyChangeListener sampleEnvironmentChangeListener;
			private void dispose() {
				if (sampleEnvironmentChangeListener != null) {
					sampleEnvironment.removePropertyChangeListener(sampleEnvironmentChangeListener);
					sampleEnvironmentChangeListener = null;
				}
			}
		}
	}
	
}
