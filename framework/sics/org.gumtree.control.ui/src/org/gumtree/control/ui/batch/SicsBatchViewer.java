/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.control.ui.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.EditorInputTransfer.EditorInputData;
import org.eclipse.ui.part.FileEditorInput;
import org.gumtree.control.batch.BatchStatus;
import org.gumtree.control.batch.IBatchListener;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.model.PropertyConstants.ControllerState;
import org.gumtree.control.ui.viewer.InternalImage;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.workbench.AbstractPartControlProvider;
import org.gumtree.ui.widgets.TimerWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SicsBatchViewer extends AbstractPartControlProvider {

	private static final String PATH_FILENAME_NOTIFYER = "/experiment/datafile";

	private static final String DEFAULT_SCRIPT_NAME = "GumTreeScript";
	
	private static final Logger logger = LoggerFactory.getLogger(SicsBatchViewer.class);

	private Text statusText;

	private List<String> commands;

	private StyledText commandText;

	private Text scriptNameText;

	private Text datafileText;

	private Button validateButton;

	private Button controlButton;

	private TimerWidget timerWidget;

	private ISicsControllerListener filenameListener;

	private ISicsController filenameNotifyer;

	private BatchListener batchListener;

	private Shell parentShell;
	
	private FormToolkit toolkit;
	
	public void createControl(Composite parent) {
		parent.setLayout(new GridLayout());
		parentShell = parent.getShell();

		/*********************************************************************
		 * Display message when SICS is not ready
		 *********************************************************************/
		if (!SicsManager.getSicsProxy().isConnected()) {
			Label label = getToolkit().createLabel(parent, "SICS is not connected.");
			GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, true).applyTo(label);
			return;
		}

		/*********************************************************************
		 * SICS status area
		 *********************************************************************/
		Composite sicsStatusComposite = getToolkit().createComposite(parent);
		sicsStatusComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false));
		createBatchStatusGroup(sicsStatusComposite);

		/*********************************************************************
		 * File location area
		 *********************************************************************/
		Composite fileLocationComposite =getToolkit().createComposite(parent);
		fileLocationComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false));
		createFileLocationGroup(fileLocationComposite);

		/*********************************************************************
		 * Batch status area
		 *********************************************************************/
		Composite batchStatusComposite = getToolkit().createComposite(parent);
		batchStatusComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true));
		createBatchContentGroup(batchStatusComposite);

		/*********************************************************************
		 * Batch control area
		 *********************************************************************/
		Composite batchControlComposite = getToolkit().createComposite(parent);
		batchControlComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false));
		createBatchControlGroup(batchControlComposite);

		// Update UI with initial SICS status
		updateUI(SicsManager.getBatchControl().getStatus());

		// add filename listener
		filenameNotifyer = SicsManager.getSicsModel().findController(PATH_FILENAME_NOTIFYER);
		if(filenameNotifyer != null) {
			filenameListener = new ISicsControllerListener() {
				
				@Override
				public void updateValue(Object oldValue, Object newValue) {
					final File file = new File(String.valueOf(newValue).trim());
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						public void run() {
							datafileText.setText(file.getName());
						}
					});
				}
				
				@Override
				public void updateTarget(Object oldValue, Object newValue) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void updateState(ControllerState oldState, ControllerState newState) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void updateEnabled(boolean isEnabled) {
					// TODO Auto-generated method stub
					
				}
			};
			filenameNotifyer.addControllerListener(filenameListener);
		} else {
			logger.warn("Data file notifer (" + PATH_FILENAME_NOTIFYER + ") is not found in SICS.");
		}

		batchListener = new BatchListener();
		SicsManager.getBatchControl().addListener(batchListener);
		}

	private void createBatchStatusGroup(Composite parent) {
		parent.setLayout(new FillLayout());
		
		Group group = new Group(parent, SWT.NONE);
		group.setText("Batch Status");
		group.setLayout(new GridLayout(2, false));
		getToolkit().adapt(group);
		
		statusText = getToolkit().createText(group, "", SWT.READ_ONLY | SWT.CENTER);
		statusText.setText("READY");
		statusText.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(statusText);
		
		timerWidget = new TimerWidget(group, SWT.NONE);
		}


	private void createFileLocationGroup(Composite parent) {
		parent.setLayout(new FillLayout());
		
		Group group = new Group(parent, SWT.NONE);
		group.setText("Files Location");
		group.setLayout(new GridLayout(2, false));
		getToolkit().adapt(group);
		
		getToolkit().createLabel(group, "Script file name: ");
		
		scriptNameText = getToolkit().createText(group, "", SWT.READ_ONLY | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(scriptNameText);

		getToolkit().createLabel(group, "Data file name: ");
		
		datafileText = getToolkit().createText(group, "", SWT.READ_ONLY | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(datafileText);
	}

	private void createBatchContentGroup(Composite parent) {
		parent.setLayout(new FillLayout());
		Group group = new Group(parent, SWT.NONE);
		getToolkit().adapt(group);
		group.setText("Batch Content");
		group.setLayout(new FillLayout());
		commandText = new StyledText(group, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
		// Add drop support
		int operations = DND.DROP_MOVE | DND.DROP_COPY;
		DropTarget dropTarget = new DropTarget(commandText, operations);
		dropTarget.setTransfer(new Transfer[] { FileTransfer.getInstance(), EditorInputTransfer.getInstance() });
		dropTarget.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent event) {
				if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
					String[] files = (String[]) event.data;
					if (files.length == 1) {
						setCommandTextFromFile(files[0]);
					}
				} else if (EditorInputTransfer.getInstance().isSupportedType(event.currentDataType) && 
						event.data instanceof EditorInputData[]) {
					// [GT-51] Making DnD to work with remote system explorer
					EditorInputData[] inputDatas = ((EditorInputData[]) event.data);
					if (inputDatas.length == 1) {
						IEditorInput input = inputDatas[0].input;
						if (input instanceof FileEditorInput) {
							IFile file = ((FileEditorInput) input).getFile();
							try {
								InputStream in = file.getContents();
								BufferedReader reader = new BufferedReader(new InputStreamReader(in));
								setCommandTextFromReader(reader, file.getLocation().toOSString());
							} catch (CoreException e) {
								logger.error("Failed to load file " + file.toString() + " from editor input.", e);
							}
						}
					}
				}
			}
		});
	}

	private void createBatchControlGroup(Composite parent) {
		parent.setLayout(new FillLayout());
		Group group = new Group(parent, SWT.NONE);
		group.setText("Batch Control");
		group.setLayout(new FillLayout(SWT.HORIZONTAL));
		getToolkit().adapt(group);

		validateButton = getToolkit().createButton(group, "Open Validator", SWT.PUSH);
		validateButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			ValidationDialog dialog = new ValidationDialog(parentShell);
				dialog.setTitle("Batch Validation");
				if (commands != null) {
					dialog.setCommands(commands.toArray(new String[commands.size()]));
				}
				dialog.open();
			}
		});

		controlButton = getToolkit().createButton(group, "Run Batch", SWT.PUSH);
		controlButton.setImage(InternalImage.START.getImage());
		controlButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				BatchStatus status = SicsManager.getBatchControl().getStatus();
				if (status.equals(BatchStatus.IDLE)) {
					start();
				} else if (status.equals(BatchStatus.EXECUTING)) {
					interrupt();
				}
			}
		});
	}

	private void setCommandTextFromFile(String filename) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			setCommandTextFromReader(reader, filename);
		} catch (FileNotFoundException e) {
			logger.error("Cannot find selected batch tcl file " + filename, e);
			MessageDialog.openError(parentShell,
						"Error loading batch tcl file",
						"Cannot find selected batch tcl file " + filename);
		}
	}

	private void setCommandTextFromReader(BufferedReader reader, String filename) {
		try {
			String line = null;
			commandText.setText("");
			commands = new ArrayList<String>();
			while ((line = reader.readLine()) != null) {
				if (line.length() == 0 || line.equals("\n")) {
					continue;
				}
				commandText.append(line + "\n");
				commands.add(line);
			}
			reader.close();
			scriptNameText.setText(filename);
		} catch (IOException e) {
			logger.error("Cannot load selected batch tcl file " + filename, e);
			MessageDialog.openError(parentShell,
					"Error loading batch tcl file",
					"Cannot load selected batch tcl file " + filename);
		}
	}
	
	public void setCommandText(final String text) {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				commandText.setText(text);
				commands = new ArrayList<String>();
				String[] lines = text.split("\n");
				for (String line : lines) {
					commands.add(line);
				}
				scriptNameText.setText("");
			}			
		});
	}

	private void updateUI(final BatchStatus status) {
		if (isDisposed()) {
			return;
		}
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (status.equals(BatchStatus.DISCONNECTED)) {
					statusText.setText("DISCONNECTED");
					statusText.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
					controlButton.setEnabled(false);
					timerWidget.clearTimerUI();
				} else if (status.equals(BatchStatus.IDLE)) {
					statusText.setText("READY");
					statusText.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
					controlButton.setImage(InternalImage.START.getImage());
					controlButton.setText("Run");
					controlButton.setEnabled(true);
					timerWidget.stopTimerUI();
				} else if (status.equals(BatchStatus.EXECUTING)) {
					statusText.setText("RUNNING");
					statusText.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
					controlButton.setImage(InternalImage.STOP.getImage());
					controlButton.setText("Interrupt");
					timerWidget.startTimerUI();
				}
			}			
		});
	}

	private class BatchListener implements IBatchListener {
		public void charExecuted(final int start, final int end) {
		}

		public void lineExecuted(final int line) {
			logger.debug("lineExecuted " + line);
			if (commandText != null && !commandText.isDisposed()) {
				SafeUIRunner.asyncExec(new SafeRunnable() {
					public void run() throws Exception {
						commandText.setStyleRange(null);
						int wordCount = 0;
						for (int i = 0; i < line; i++) {
							wordCount += commands.get(i)
									.length() + 1;
						}
						StyleRange styleRange = new StyleRange();
						styleRange.start = 0;
						styleRange.length = wordCount;
						styleRange.background = PlatformUI
								.getWorkbench()
								.getDisplay()
								.getSystemColor(
										SWT.COLOR_YELLOW);
						commandText
								.setStyleRange(styleRange);
					}					
				});
			}
		}

		public void statusChanged(final BatchStatus newStatus) {
			if (!isDisposed()) {
				updateUI(newStatus);
			}
		}

		@Override
		public void rangeExecuted(String rangeText) {
			
		}
		
		public void lineExecutionError(int line) {
		}

		@Override
		public void start() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void stop() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void scriptChanged(String scriptName) {
			
		}
		
	}
	
	private FormToolkit getToolkit() {
		if (toolkit == null) {
			toolkit = new FormToolkit(Display.getDefault());
		}
		return toolkit;
	}
	
	public void start() {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				BatchStatus status = SicsManager.getBatchControl().getStatus();
				String scriptName = DEFAULT_SCRIPT_NAME;
				// [GUMTREE-76] Send file name instead of the whole path
				if (scriptNameText.getText() != null && scriptNameText.getText().length() != 0) {
					File scriptFile = new File(scriptNameText.getText());
					scriptName = scriptFile.getName();
					// Replace space characters to underscore so the
					// whole file name can be preserved.
					scriptName = scriptName.replaceAll("\\s", "_");
					// It must run with script name
					if(scriptName == null) {
						return;
					}
				}
				if (status.equals(BatchStatus.IDLE)) {
					if (commands != null && commands.size() > 0) {
						try {
							SicsManager.getBatchControl().run(commands.toArray(new String[commands.size()]), scriptName);
							
							// Resets highlight colour
							StyleRange styleRange = new StyleRange();
							styleRange.start = 0;
							styleRange.length = commandText.getCharCount();
							styleRange.background = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_WHITE);
							commandText.setStyleRange(styleRange);
						} catch (SicsException e1) {
							logger.error("Error in running batch from control view.", e1);
						}
					}
				}
			}			
		});

	}
	
	public void interrupt() {
		BatchStatus status = SicsManager.getBatchControl().getStatus();
		if (status.equals(BatchStatus.EXECUTING)) {
			try {
				SicsManager.getBatchControl().interrupt();
			} catch (SicsException e1) {
				logger.error("Error in interrupting batch from control view.", e1);
			}
		}
	}
	
	public void dispose() {
		// remove filename listener
		if (filenameNotifyer != null) {
			filenameNotifyer.removeControllerListener(filenameListener);
			filenameNotifyer = null;
		}
		if (batchListener != null) {
			SicsManager.getBatchControl().removeListener(batchListener);
			batchListener = null;
		}
		if (toolkit != null) {
			toolkit.dispose();
			toolkit = null;
		}
		parentShell = null;
	}

}
