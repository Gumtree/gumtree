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
package org.gumtree.gumnix.sics.ui.widgets;

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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.EditorInputTransfer.EditorInputData;
import org.eclipse.ui.part.FileEditorInput;
import org.gumtree.gumnix.sics.control.IBatchListener;
import org.gumtree.gumnix.sics.control.ISicsBatchControl.BatchStatus;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.control.events.DynamicControllerListenerAdapter;
import org.gumtree.gumnix.sics.control.events.IDynamicControllerListener;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.internal.ui.Activator;
import org.gumtree.gumnix.sics.internal.ui.batch.ControlView;
import org.gumtree.gumnix.sics.internal.ui.batch.ValidationDialog;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.ui.widgets.TimerWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

/**
 * @author nxi
 * Created on 17/02/2009
 */
public class BatchControlComposite extends Composite {
	private static final Image ICON_START = Activator
	.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
			"icons/full/etool16/run_exc.gif").createImage();

private static final Image ICON_STOP = Activator.imageDescriptorFromPlugin(
	Activator.PLUGIN_ID, "icons/full/elcl16/terminate_co.gif")
	.createImage();

private static final String PATH_FILENAME_NOTIFYER = "/experiment/datafile";

private static Logger logger;

private Composite parent;

private XStream xstream;

private Text statusText;

private List<String> commands;

private StyledText commandText;

private Text scriptNameText;

private Text datafileText;

private Button validateButton;

private Button controlButton;

private TimerWidget timerWidget;

private IDynamicControllerListener filenameListener;

private IComponentController filenameNotifyer;

private BatchListener batchListener;

	/**
	 * @param parent
	 * @param style
	 */
	public BatchControlComposite(final Composite parent, int style) {
		super(parent, style);
		this.parent = parent;
		parent.setLayout(new GridLayout());
		
		/*********************************************************************
		 * Display message when SICS is not ready
		 *********************************************************************/
		if (!SicsCore.getDefaultProxy().isConnected()) {
			Label label = new Label(parent, SWT.NONE);
			label.setText("SICS is not connected.");
			GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, true).applyTo(label);
			return;
		}

		Composite sicsStatusComposite = new Composite(parent, SWT.NONE);
		sicsStatusComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false));
		createBatchStatusGroup(sicsStatusComposite);

//		Composite fileLocationComposite = new Composite(parent, SWT.NONE);
//		fileLocationComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
//				true, false));
//		createFileLocationGroup(fileLocationComposite);

		Composite batchStatusComposite = new Composite(parent, SWT.NONE);
		batchStatusComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true));
		createBatchContentGroup(batchStatusComposite);

		Composite batchControlComposite = new Composite(parent, SWT.NONE);
		batchControlComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false));
		createBatchControlGroup(batchControlComposite);

		updateUI(SicsCore.getSicsManager().control().batch().getStatus());

		// add filename listener
		filenameNotifyer = SicsCore.getSicsController().findComponentController(PATH_FILENAME_NOTIFYER);
		if(filenameNotifyer != null) {
			filenameListener = new DynamicControllerListenerAdapter() {
				public void valueChanged(IDynamicController controller, IComponentData newValue) {
					final File file = new File(newValue.getStringData().trim());
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						public void run() {
							datafileText.setText(file.getName());
						}
					});
				}
			};
			filenameNotifyer.addComponentListener(filenameListener);
		} else {
			getLogger().warn("Data file notifer (" + PATH_FILENAME_NOTIFYER + ") is not found in SICS.");
		}

		batchListener = new BatchListener();
		SicsCore.getSicsManager().control().batch().addListener(batchListener);
	}

	private void createBatchStatusGroup(Composite parent) {
		parent.setLayout(new FillLayout());
		Group group = new Group(parent, SWT.NONE);
		group.setText("Batch Status");
		group.setLayout(new GridLayout(2, false));
		Display display = PlatformUI.getWorkbench().getDisplay();
		statusText = new Text(group, SWT.READ_ONLY | SWT.CENTER);
		statusText.setText("READY");
		statusText.setBackground(display.getSystemColor(SWT.COLOR_GREEN));
		statusText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		timerWidget = new TimerWidget(group, SWT.NONE);
	}


	private void createFileLocationGroup(Composite parent) {
		parent.setLayout(new FillLayout());
		Group group = new Group(parent, SWT.NONE);
		group.setText("Files Location");
		group.setLayout(new GridLayout(2, false));
		Label label = new Label(group, SWT.NONE);
		label.setText("Script file name: ");
		scriptNameText = new Text(group, SWT.READ_ONLY | SWT.BORDER);
		scriptNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		label = new Label(group, SWT.NONE);
		label.setText("Data file name: ");
		datafileText = new Text(group, SWT.READ_ONLY | SWT.BORDER);
		datafileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}

	private void createBatchContentGroup(Composite parent) {
		parent.setLayout(new FillLayout());
		Group group = new Group(parent, SWT.NONE);
		group.setText("Batch Content");
		group.setLayout(new FillLayout());
		commandText = new StyledText(group, SWT.BORDER 
				| SWT.H_SCROLL | SWT.V_SCROLL);
		// Add drop support
		int operations = DND.DROP_MOVE | DND.DROP_COPY;
		DropTarget dropTarget = new DropTarget(commandText, operations);
		dropTarget.setTransfer(new Transfer[] { FileTransfer.getInstance(), EditorInputTransfer.getInstance() });
		dropTarget.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent event) {
				if (FileTransfer.getInstance().isSupportedType(
						event.currentDataType)) {
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
								getLogger().error("Failed to load file " + file.toString() + " from editor input.", e);
							}
						}
					}
				}
			}
		});
	}
	

	private void setCommandTextFromFile(String filename) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			setCommandTextFromReader(reader, filename);
		} catch (FileNotFoundException e) {
			getLogger().error("Cannot find selected batch tcl file " + filename, e);
			MessageDialog.openError(parent.getShell(),
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
			getLogger().error("Cannot load selected batch tcl file " + filename, e);
			MessageDialog.openError(parent.getShell(),
					"Error loading batch tcl file",
					"Cannot load selected batch tcl file " + filename);
		}
	}

	private XStream getXStream() {
		if (xstream == null) {
			xstream = new XStream();
		}
		return xstream;
	}

	private void updateUI(final BatchStatus status) {
		final Display display = PlatformUI.getWorkbench().getDisplay();
		if (display.isDisposed() || statusText == null
				|| statusText.isDisposed()) {
			return;
		}
		display.asyncExec(new Runnable() {
			public void run() {
				if (status.equals(BatchStatus.DISCONNECTED)) {
					statusText.setText("DISCONNECTED");
					statusText.setBackground(display
							.getSystemColor(SWT.COLOR_RED));
					controlButton.setEnabled(false);
					timerWidget.clearTimerUI();
				} else if (status.equals(BatchStatus.IDLE)
						|| status.equals(BatchStatus.READY)) {
					statusText.setText("READY");
					statusText.setBackground(display
							.getSystemColor(SWT.COLOR_GREEN));
					controlButton.setImage(ICON_START);
					controlButton.setText("Run");
					controlButton.setEnabled(true);
					timerWidget.stopTimerUI();
				} else if (status.equals(BatchStatus.RUNNING)) {
					statusText.setText("RUNNING");
					statusText.setBackground(display
							.getSystemColor(SWT.COLOR_YELLOW));
					controlButton.setImage(ICON_STOP);
					controlButton.setText("Interrupt");
					timerWidget.startTimerUI();
				}
			}
		});
	}
	
	private void createBatchControlGroup(final Composite parent) {
		parent.setLayout(new FillLayout());
		Group group = new Group(parent, SWT.NONE);
		group.setText("Batch Control");
		group.setLayout(new FillLayout(SWT.HORIZONTAL));

		validateButton = new Button(group, SWT.PUSH);
		validateButton.setText("Open Validator");
		validateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ValidationDialog dialog = new ValidationDialog(parent.getShell());
				dialog.setTitle("Batch Validation");
				if (commands != null) {
					dialog.setCommands(commands.toArray(new String[commands.size()]));
				}
				dialog.open();
			}
		});

		controlButton = new Button(group, SWT.PUSH);
		controlButton.setText("Run Batch");
		controlButton.setImage(ICON_START);
		controlButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				BatchStatus status = SicsCore.getSicsManager().control().batch()
						.getStatus();
				// [GUMTREE-76] Send file name instead of the whole path
//				File scriptFile = new File(scriptNameText.getText());
//				String scriptName = scriptFile.getName();
//				// Replace space characters to underscore so the
//				// whole file name can be preserved.
//				scriptName = scriptName.replaceAll("\\s", "_");
//				// It must run with script name
//				if(scriptName == null) {
//					return;
//				}
				String scriptName = "workflow";
				String batchContent = commandText.getText();
				String[] lines = batchContent.split("\n");
				if (commands == null)
					commands = new ArrayList<String>();
				commands.clear();
				for (int i = 0; i < lines.length; i++) {
					String line = lines[i].trim();
					if (line.length() > 0){
						commands.add(line);
						System.out.println(line);
					}
				}
				if (status.equals(BatchStatus.IDLE)
						|| status.equals(BatchStatus.READY)) {
					if (commands != null && commands.size() > 0) {
						try {
							SicsCore.getSicsManager().control()
									.batch()
									.run(
											commands
													.toArray(new String[commands
															.size()]), scriptName);
							// Resets highlight colour
							StyleRange styleRange = new StyleRange();
							styleRange.start = 0;
							styleRange.length = commandText.getCharCount();
							styleRange.background = PlatformUI.getWorkbench()
									.getDisplay().getSystemColor(
											SWT.COLOR_WHITE);
							commandText.setStyleRange(styleRange);
						} catch (SicsIOException e1) {
							getLogger()
									.error(
											"Error in running batch from control view.",
											e1);
						}
					}
				} else if (status.equals(BatchStatus.RUNNING)) {
					try {
						SicsCore.getSicsManager().control().batch().interrupt();
					} catch (SicsIOException e1) {
						getLogger()
								.error(
										"Error in interrupting batch from control view.",
										e1);
					}
				}
			}
		});
	}

	public StyledText getCommandTextBox(){
		return commandText;
	}
	
	private static Logger getLogger() {
		if(logger == null) {
			logger = LoggerFactory.getLogger(ControlView.class);
		}
		return logger;
	}
	
	public void setCommand(List<String> commands){
		this.commands = commands;
	}
	
	private void retrieveCommands(){
		String text = commandText.getText();
	}

	private class BatchListener implements IBatchListener {
		public void charExecuted(final int start, final int end) {
		}

		public void lineExecuted(final int line) {
			getLogger().debug("lineExecuted " + line);
			if (commandText != null && !commandText.isDisposed()) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(
						new Runnable() {
							public void run() {
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
			if (PlatformUI.getWorkbench().getDisplay().isDisposed()
					|| statusText == null || statusText.isDisposed()) {
				return;
			}
			updateUI(newStatus);
		}

		public void lineExecutionError(int line) {
		}
	}
}
