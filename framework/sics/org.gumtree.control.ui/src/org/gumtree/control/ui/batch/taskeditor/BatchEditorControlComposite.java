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

package org.gumtree.control.ui.batch.taskeditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.gumtree.control.batch.IBatchScript;
import org.gumtree.control.batch.tasks.ISicsBatchTask;
import org.gumtree.control.batch.tasks.ISicsCommand;
import org.gumtree.control.batch.tasks.ISicsCommandBlock;
import org.gumtree.control.ui.batch.IBatchManager;
import org.gumtree.control.ui.batch.VisualBatchScript;
import org.gumtree.control.ui.batch.command.AbstractSicsCommand;
import org.gumtree.control.ui.batch.command.SicsCommand;
import org.gumtree.control.ui.viewer.InternalImage;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.workflow.ui.ITask;
import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.TaskState;
import org.gumtree.workflow.ui.events.TaskEvent;
import org.gumtree.workflow.ui.events.WorkflowEvent;
import org.gumtree.workflow.ui.events.WorkflowStructuralEvent;
import org.gumtree.workflow.ui.util.WorkflowFactory;
import org.gumtree.workflow.ui.viewer2.AbstractWorkflowViewerComponent;
import org.gumtree.workflow.ui.viewer2.WorkflowComposerViewer;

public class BatchEditorControlComposite extends AbstractWorkflowViewerComponent {

	public static final String EXPERIMENT_PROJECT = "Experiment";
	public static final String GUMTREE_FOLDER = "GumtreeOnly";
	public static final String AUTOSAVE_FOLDER = "AutoSaves";
	private static final String PROP_SICS_UNIQUE_BATCH_NAME = "gumtree.sics.uniqueBatchName";
	private static final String PROP_TIME_ESTIMATION_ENABLED = "gumtree.workflow.timeEstimationEnabled";
	private static final String PROP_NEW_BUTTON_ENABLED = "gumtree.workflow.newButtonEnabled";
	private static final String PROP_APPEND_BUTTON_ENABLED = "gumtree.workflow.appendButtonEnabled";
	private static final String PROP_LOAD_BUTTON_ENABLED = "gumtree.workflow.loadButtonEnabled";

	private Label estimationText;
	protected static String fileDialogPath;
	private IEventHandler<WorkflowEvent> workfloEventHandler;
	private IEventHandler<TaskEvent> taskEventHandler;
	private boolean uniqueBatchName = true;
	private boolean isTimeEstimationEnabled = true;
	private boolean isNewButtonEnabed = true;
	private boolean isAppendButtonEnabed = true;
	private boolean isLoadButtonEnabed = true;

	public BatchEditorControlComposite(Composite parent, int style) {
		super(parent, style);
		try {
			uniqueBatchName = Boolean.valueOf(System.getProperty(PROP_SICS_UNIQUE_BATCH_NAME));
		} catch (Exception e) {
		}
		try {
			isTimeEstimationEnabled = Boolean.valueOf(System.getProperty(PROP_TIME_ESTIMATION_ENABLED));
		} catch (Exception e) {
		}
		try {
			isNewButtonEnabed = Boolean.valueOf(System.getProperty(PROP_NEW_BUTTON_ENABLED));
		} catch (Exception e) {
		}
		try {
			isAppendButtonEnabed = Boolean.valueOf(System.getProperty(PROP_APPEND_BUTTON_ENABLED));
		} catch (Exception e) {
		}
		try {
			isLoadButtonEnabed = Boolean.valueOf(System.getProperty(PROP_LOAD_BUTTON_ENABLED));
		} catch (Exception e) {
		}
	}

	protected void componentDispose() {
		getWorkflow().removeEventListener(workfloEventHandler);	
		for (ITask task : getWorkflow().getTasks()) {
			task.removeEventListener(taskEventHandler);
		}
	}
	
	protected void createUI() {
		GridLayoutFactory.swtDefaults().numColumns(6).margins(0, 0).applyTo(this);
		
		if (isTimeEstimationEnabled) {
			Group estimationGroup = new Group(this, SWT.NONE);
			estimationGroup.setBackground(getBackground());
			GridLayoutFactory.fillDefaults().applyTo(estimationGroup);
			GridDataFactory.swtDefaults().minSize(150, SWT.DEFAULT).applyTo(estimationGroup);
	//		Label estimationLabel = getToolkit().createLabel(estimationGroup, "Est:");
	//		GridDataFactory.swtDefaults().applyTo(estimationLabel);
			estimationGroup.setText("Estimation:");
			estimationText = getToolkit().createLabel(estimationGroup, "-");
			estimationText.setToolTipText(null);
			GridDataFactory.swtDefaults().hint(140, SWT.DEFAULT).applyTo(estimationText);
			
		}
		
		if (isNewButtonEnabed) {
			/*********************************************************************
			 * New
			 *********************************************************************/
			Button newButton = getToolkit().createButton(this, "Clear", SWT.PUSH);
			newButton.setToolTipText("Clear current task contents.");
			newButton.setImage(InternalImage.FILE.getImage());
			GridDataFactory.fillDefaults().grab(true, false).applyTo(newButton);
			newButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					int numberOfTasks = getWorkflow().getTasks().size();
					if (numberOfTasks == 0 || MessageDialog.openConfirm(getShell(), 
							"Confirm clearing", "Are you sure to clear all the " 
							+ "tasks?")) {
						getWorkflowViewer().setWorkflow(WorkflowFactory.createEmptyWorkflow());
					}
				}
			});
		}
		
		/*********************************************************************
		 * Add
		 *********************************************************************/
		Button addButton = getToolkit().createButton(this, "Put to Buffer Queue", SWT.PUSH);
		addButton.setToolTipText("Put tasks into run queue.");
		addButton.setImage(InternalImage.QUEUE.getImage());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(addButton);
		addButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// Ensure the current workflow is valid
				if (getWorkflow() == null) {
					return;
				}
				ISicsBatchTask batchScript = getWorkflow().getContext()
						.getSingleValue(ISicsBatchTask.class);
				if (batchScript == null) {
					return;
				}
				// Ask for buffer name input
				InputDialog dialog = new InputDialog(getShell(),
						"New Batch Buffer", "Enter new batch buffer name:",
						"Batch" + (uniqueBatchName ? String.valueOf(System.currentTimeMillis()).substring(6) : ""), new IInputValidator() {
							public String isValid(String newText) {
								if (newText == null || newText.length() == 0) {
									return "Buffer name is empty";
								}
								return null;
							}
						});
				if (dialog.open() == Window.CANCEL) {
					return;
				}
				// Add to the queue
				IBatchManager manager = ServiceUtils.getService(IBatchManager.class);
				IBatchScript buffer = new VisualBatchScript(dialog.getValue(), getWorkflow());
				float time = 0;
				float counts = 0;
				for (ITask task : getWorkflow().getTasks()) {
					Object model = task.getDataModel();
					if (model instanceof ISicsCommandBlock) {
						ISicsCommand[] commands = ((ISicsCommandBlock) model).getCommands();
						if (commands != null && commands.length > 0) {
							ISicsCommand command = commands[0];
							if (command instanceof AbstractSicsCommand) {
								if ("secs".equals(((AbstractSicsCommand) command).getEstimationUnits())) {
									time += ((AbstractSicsCommand) command).getEstimatedTime();
								} else if ("cts".equals(((AbstractSicsCommand) command).getEstimationUnits())) {
									counts += ((AbstractSicsCommand) command).getEstimatedTime();
								}
							}
						}
					}
				}
				if (counts == 0 && time > 0) {
					buffer.setTimeEstimation((int) time);
				}
				manager.getBatchBufferQueue().add(buffer);
				
				String filename = "EXP" + getBatchDateString() + "_" + dialog.getValue()
								+ ".wml";
				saveTempWorkflow(filename);
				// Clean the viewer by swapping to a new workflow 
//				getWorkflowViewer().setWorkflow(WorkflowFactory.createEmptyWorkflow());
			}
		});
		
		/*********************************************************************
		 * Save (routine filled by Norman 17/03/10)
		 *********************************************************************/

		Button saveButton = getToolkit().createButton(this, "Save", SWT.PUSH);
		saveButton.setToolTipText("Save tasks into a file.");
		saveButton.setImage(InternalImage.SAVE.getImage());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(saveButton);
		saveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
//                Shell parentShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
//                SaveAsDialog dialog = new SaveAsDialog(parentShell);
                FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
                if (fileDialogPath == null){
 					IWorkspace workspace= ResourcesPlugin.getWorkspace();
 					IWorkspaceRoot root = workspace.getRoot();
 					dialog.setFilterPath(root.getLocation().toOSString());
 				} else {
 					dialog.setFilterPath(fileDialogPath);
 				}
                dialog.setFilterExtensions(new String[]{"*.wml"});
                String dialogReturn = dialog.open();
                String filePath = dialog.getFileName();
                if (dialogReturn != null && filePath.trim().length() > 0) {
                	filePath = dialog.getFilterPath() + "/" + filePath;
              	  // Fix file extension
                	if (!filePath.endsWith(".wml")) {
                		filePath += ".wml";
                	}
                	File pickedFile = new File(filePath);
//                	if (pickedFile.getName()) {
//                		filePath = filePath.addFileExtension("wml");
//                	}
                	if (pickedFile.exists()) {
                		if (!MessageDialog.openConfirm(getShell(), "Confirm overwriting", "File " 
                				+ pickedFile.getPath() + " exists. Do you wish to overwrite?")) {
                			return;
                		}
                	}
                	try {
                		OutputStream out = new FileOutputStream(pickedFile);
                		WorkflowFactory.saveWorkflow(getWorkflow(), out);
                		out.flush();
                		out.close();
                		// Refresh UI
//                		pickedFile.getParent().refreshLocal(1, new NullProgressMonitor());
                    	IWorkspace workspace= ResourcesPlugin.getWorkspace();
                    	workspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
                    	fileDialogPath = pickedFile.getParent();
                	} catch (Exception error) {
                		MessageDialog.openError(getShell(), "Error", "Cannot save file " + 
                				filePath + ": " + error.getLocalizedMessage());
                	}
                }
			}
		});

		/*********************************************************************
		 * Load (routine filled by Norman 17/03/10)
		 *********************************************************************/
//		Button loadButton = getToolkit().createButton(this, "Load Tasks", SWT.PUSH);
//		loadButton.setToolTipText("Overwrite current tasks by loading tasks from a file.");
//		loadButton.setImage(InternalImage.LOAD.getImage());
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(loadButton);
//		loadButton.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
// 				if (fileDialogPath == null){
// 					IWorkspace workspace= ResourcesPlugin.getWorkspace();
// 					IWorkspaceRoot root = workspace.getRoot();
// 					dialog.setFilterPath(root.getLocation().toOSString());
// 				}else
// 					dialog.setFilterPath(fileDialogPath);
// 				dialog.setFilterExtensions(new String[]{"*.wml"});
// 				dialog.open();
//				String filePath = dialog.getFilterPath() + "/" + dialog.getFileName();
//				File pickedFile = new File(filePath);
//				if (!pickedFile.exists() || !pickedFile.isFile())
//					return;
//				fileDialogPath = pickedFile.getParent();
//				if (filePath != null) {
//					try {
//						InputStream input = new FileInputStream(filePath);
//						IWorkflow workflow = WorkflowFactory.createWorkflow(input);
//						input.close();
//						getWorkflowViewer().setWorkflow(workflow);
//						refreshUI();
//					} catch (Exception error) {
//						MessageDialog.openError(getShell(), "Error", "Cannot open file " 
//								+ filePath + ": " + error.getLocalizedMessage());
//					}
//				}
//			}
//		});
		
		if (isAppendButtonEnabed) {
			/*********************************************************************
			 * Append (routine filled by Norman 17/03/10) to end of workflow
			 *********************************************************************/
			Button appendButton = getToolkit().createButton(this, "Append", SWT.PUSH);
			appendButton.setToolTipText("Load tasks from a file and append them to the end of " +
					"current tasks.");
			appendButton.setImage(InternalImage.APPEND.getImage());
			GridDataFactory.fillDefaults().grab(true, false).applyTo(appendButton);
			appendButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
	 				if (fileDialogPath == null){
	 					IWorkspace workspace= ResourcesPlugin.getWorkspace();
	 					IWorkspaceRoot root = workspace.getRoot();
	 					dialog.setFilterPath(root.getLocation().toOSString());
	 				}else
	 					dialog.setFilterPath(fileDialogPath);
	 				dialog.setFilterExtensions(new String[]{"*.wml"});
	 				dialog.open();
					String filePath = dialog.getFilterPath() + "/" + dialog.getFileName();
					File pickedFile = new File(filePath);
					if (!pickedFile.exists() || !pickedFile.isFile())
						return;
					fileDialogPath = pickedFile.getParent();
					if (filePath != null) {
						try {
							InputStream input = new FileInputStream(filePath);
							IWorkflow workflow = WorkflowFactory.createWorkflow(input);
							input.close();
	//						getWorkflowViewer().setWorkflow(workflow);
							getWorkflow().insertTasks(getWorkflow().getTasks().size(), 
									workflow.getTasks());
							refreshUI();
						} catch (Exception error) {
							MessageDialog.openError(getShell(), "Error", "Cannot open file " 
									+ filePath + ": " + error.getLocalizedMessage());
						}
					}
				}
			});
		}
		
		if (isLoadButtonEnabed) {
			/*********************************************************************
			 * Append (routine filled by Norman 17/03/10) to end of workflow
			 *********************************************************************/
			Button loadButton = getToolkit().createButton(this, "Load", SWT.PUSH);
			loadButton.setToolTipText("Load tasks from a file that overwrite the " +
					"current tasks.");
			loadButton.setImage(InternalImage.APPEND.getImage());
			GridDataFactory.fillDefaults().grab(true, false).applyTo(loadButton);
			loadButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
	 				if (fileDialogPath == null){
	 					IWorkspace workspace= ResourcesPlugin.getWorkspace();
	 					IWorkspaceRoot root = workspace.getRoot();
	 					dialog.setFilterPath(root.getLocation().toOSString());
	 				}else
	 					dialog.setFilterPath(fileDialogPath);
	 				dialog.setFilterExtensions(new String[]{"*.wml"});
	 				dialog.open();
					String filePath = dialog.getFilterPath() + "/" + dialog.getFileName();
					File pickedFile = new File(filePath);
					if (!pickedFile.exists() || !pickedFile.isFile())
						return;
					fileDialogPath = pickedFile.getParent();
					if (filePath != null) {
						try {
							InputStream input = new FileInputStream(filePath);
							IWorkflow workflow = WorkflowFactory.createWorkflow(input);
							input.close();
							getWorkflowViewer().setWorkflow(workflow);
//							getWorkflow().insertTasks(getWorkflow().getTasks().size(), 
//									workflow.getTasks());
							refreshUI();
						} catch (Exception error) {
							MessageDialog.openError(getShell(), "Error", "Cannot open file " 
									+ filePath + ": " + error.getLocalizedMessage());
						}
					}
				}
			});
		}
		
		
		/*********************************************************************
		 * Print (routine filled by Norman 24/01/11) to end of workflow
		 *********************************************************************/
		Button printButton = getToolkit().createButton(this, "Print", SWT.PUSH);
		printButton.setToolTipText("Print out the tasks");
		printButton.setImage(InternalImage.PRINT.getImage());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(printButton);
		final Composite adapter = getParent();
		printButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PrintDialog dialog = new PrintDialog(getShell());
				// Opens a dialog and let use user select the 
				// target printer and configure various settings.
				PrinterData printerData = dialog.open();
				if(printerData != null) { // If a printer is selected
					// Creates a printer.
					Printer printer = new Printer(printerData);

					// Starts the print job.
					if(printer.startJob("Text")) {
//						GC gc = new GC(printer);

						Rectangle clientArea = printer.getClientArea();
						Rectangle trim = printer.computeTrim(0, 0, 0, 0);
						Point dpi = printer.getDPI();
						int leftMargin = (int) (dpi.x * 0.75 + trim.x); // one inch from left side of paper
						int rightMargin = clientArea.width - (int) (dpi.x * 0.75) + trim.x 
							+ trim.width; // one inch from right side of paper
						int topMargin = (int) (dpi.y * 0.75 + trim.y); // one inch from top edge of paper
						int bottomMargin = clientArea.height - (int) (dpi.y * 0.75) + trim.y 
							+ trim.height;

//						printImage(printer, adapter, leftMargin, topMargin,
//				                rightMargin, bottomMargin);
						printText(printer, leftMargin, topMargin,
				                rightMargin, bottomMargin);

						
					}

					// Ends the job.
//					printer.endJob();

					// Disposes the printer object after use. 
					printer.dispose();
				}
			}

		});
		
		addListeners();
		getParent().layout(true, true);
	}

	private void addListeners() {
		workfloEventHandler = new IEventHandler<WorkflowEvent>() {
			public void handleEvent(WorkflowEvent event) {
				// Re-render on workflow structural change
				if (event instanceof WorkflowStructuralEvent) {
					SafeUIRunner.asyncExec(new SafeRunnable() {
						public void run() throws Exception {
							addTaskEventListener();
							updateEstimation();
						}

					});
				}
			}
		};
		getWorkflow().addEventListener(workfloEventHandler);
		
		taskEventHandler = new IEventHandler<TaskEvent>() {
			public void handleEvent(final TaskEvent event) {
				SafeUIRunner.asyncExec(new SafeRunnable() {
					public void run() throws Exception {
						if (isDisposed()) {
							return;
						}
						if (event.getState() == TaskState.UPDATED) {
							updateEstimation();
						} 
					}

				});
			}
		};
		
	}

	private void addTaskEventListener() {
		for (ITask task : getWorkflow().getTasks()) {
			task.removeEventListener(taskEventHandler);
			task.addEventListener(taskEventHandler);
		}
	}
	
	private void updateEstimation() {
		if (!isTimeEstimationEnabled) {
			return;
		}
		float time = 0;
		float counts = 0;
		for (ITask task : getWorkflow().getTasks()) {
			Object model = task.getDataModel();
			if (model instanceof ISicsCommandBlock) {
				ISicsCommand[] commands = ((ISicsCommandBlock) model).getCommands();
				if (commands != null && commands.length > 0) {
					ISicsCommand command = commands[0];
					if (command instanceof SicsCommand) {
						if ("secs".equals(((AbstractSicsCommand) command).getEstimationUnits())) {
							time += ((AbstractSicsCommand) command).getEstimatedTime();
						} else if ("cts".equals(((AbstractSicsCommand) command).getEstimationUnits())) {
							counts += ((AbstractSicsCommand) command).getEstimatedTime();
						}
					}
				}
			}
		}
		String estimation = "";
		if (time > 0) {
			estimation += getTimeString(time);
		}
		if (counts > 0) {
			if (time > 0) {
				estimation += " + ";
			}
			estimation += getCountString(counts) + " counts";
		}
		estimationText.setText(estimation);
		estimationText.setToolTipText(estimation);
	}

	private String getTimeString(float seconds){
		if (seconds < 360)
			return String.valueOf((int) seconds) + " seconds";
		if (seconds < 3600 * 2)
			return String.valueOf((int) Math.ceil(seconds / 60)) + " minutes";
		if (seconds < 3600 * 5){
			double remainder = Math.IEEEremainder(seconds, 3600);
			if (remainder < 0)
				remainder = 3600 + remainder;
			int minites = (int) (remainder / 60);
			return String.valueOf((int) (seconds / 3600)) + " hours " + (
					minites > 1 ? String.valueOf(minites) + " minutes" : "");
		}
		return String.valueOf((int) Math.ceil(seconds / 3600)) + " hours";
	}

	private String getCountString(float counts) {
		if (counts > 1e6) {
			float kCounts = counts / 1000000;
			if (((int) kCounts) == kCounts) {
				return String.format("%d", (int) kCounts) + "M";
			}
			return String.format("%.1f", kCounts) + "M";
		}
		if (counts > 1e3) {
			float kCounts = counts / 1000;
			if (((int) kCounts) == kCounts) {
				return String.format("%d", (int) kCounts) + "K";
			}
			return String.format("%.1f", kCounts) + "K";
		}
		return String.format("%d", (int) counts);
	}
	
	private Composite findTaskForm(Composite parent) {
		Composite part = null;
		Control[] controls = parent.getChildren();
		for (Control control : controls) {
			if (control instanceof SashForm) {
				part = (SashForm) control;
				break;
			}
		}
		if (part == null) {
			return null;
		}
		controls = part.getChildren();
		part = null;
		for (Control control : controls) {
			if (control instanceof Group) {
				part = (Group) control;
				break;
			}
		}
		if (part == null) {
			return null;
		}
		controls = part.getChildren();
		part = null;
		for (Control control : controls) {
			if (control instanceof WorkflowComposerViewer) {
				part = (WorkflowComposerViewer) control;
				break;
			}
		}
		if (part == null) {
			return null;
		}
		controls = part.getChildren();
		part = null;
		for (Control control : controls) {
			if (control instanceof ScrolledForm) {
				ScrolledForm form = (ScrolledForm) control;
				return form.getBody();
			}
		}
		return null;
	}

	private void printText(Printer printer, int leftMargin,
			int topMargin, int rightMargin, int bottomMargin) {
		String text = "";
		int index = 1;
		for (ITask task : getWorkflow().getTasks()) {
			text += "#" + index + "-" + task.getLabel() + "\n";
			if (task instanceof AbstractCommandBlockTask) {
				ISicsCommandBlock block = (ISicsCommandBlock) task.getDataModel();
				for (ISicsCommand command : block.getCommands()) {
					if (command instanceof SicsCommand) {
						text += ((AbstractSicsCommand) command).getPrintable() + "\n";
					} else {
						text += command.toScript() + "\n";
					}
				}
			} else {
				text += task.getDataModel().toString() + "\n";
			}
			index++;
		}
		System.out.println(text);
		print(printer, text, leftMargin, topMargin, rightMargin, bottomMargin);
	}
	
	void print(Printer printer, String text, int leftMargin,
			int topMargin, int rightMargin, int bottomMargin) {
//		if (printer.startJob("Text")) { // the string is the job name - shows up
			// in the printer's job list
//			Rectangle clientArea = printer.getClientArea();
//			Rectangle trim = printer.computeTrim(0, 0, 0, 0);
//			Point dpi = printer.getDPI();
//			leftMargin = dpi.x + trim.x; // one inch from left side of paper
//			rightMargin = clientArea.width - dpi.x + trim.x + trim.width; // one
//			// inch
//			// from
//			// right
//			// side
//			// of
//			// paper
//			topMargin = dpi.y + trim.y; // one inch from top edge of paper
//			bottomMargin = clientArea.height - dpi.y + trim.y + trim.height; // one
//			// inch
//			// from
//			// bottom
//			// edge
//			// of
//			// paper

			/* Create a buffer for computing tab width. */
			int tabSize = 4; // is tab width a user setting in your UI?
			StringBuffer tabBuffer = new StringBuffer(tabSize);
			for (int i = 0; i < tabSize; i++)
				tabBuffer.append(' ');
			String tabs = tabBuffer.toString();

			/*
			 * Create printer GC, and create and set the printer font &
			 * foreground color.
			 */
			GC gc = new GC(printer);
			Font font = new Font(printer, "Courier", 10, SWT.NORMAL);

			FontData fontData = font.getFontData()[0];
			Font printerFont = new Font(printer, fontData.getName(), fontData
					.getHeight(), fontData.getStyle());
			gc.setFont(printerFont);
			int tabWidth = gc.stringExtent(tabs).x;
			int lineHeight = gc.getFontMetrics().getHeight() + 12;

			Color printerForegroundColor = printer.getSystemColor(SWT.COLOR_BLACK);
			gc.setForeground(printerForegroundColor);

			Color printerBackgroundColor = printer.getSystemColor(SWT.COLOR_WHITE);
			gc.setBackground(printerBackgroundColor);

			/* Print text to current gc using word wrap */
			printTextPage(printer, gc, text, leftMargin,
					topMargin, rightMargin, bottomMargin, tabWidth, lineHeight);
			printer.endJob();

			/* Cleanup graphics resources used in printing */
			printerFont.dispose();
			font.dispose();
			printerForegroundColor.dispose();
			printerBackgroundColor.dispose();
			gc.dispose();
//		}
	}

	void printTextPage(Printer printer, GC gc, String text, int leftMargin,
			int topMargin, int rightMargin, int bottomMargin, int tabWidth, int lineHeight) {
		printer.startPage();
		StringBuffer wordBuffer = new StringBuffer();
		int x = leftMargin;
		int y = topMargin;
		int index = 0;
		int end = text.length();
		while (index < end) {
			char c = text.charAt(index);
			index++;
			if (c != 0) {
				if (c == 0x0a || c == 0x0d) {
					if (c == 0x0d && index < end
							&& text.charAt(index) == 0x0a) {
						index++; // if this is cr-lf, skip the lf
					}
					if (wordBuffer.length() > 0) {
						String word = wordBuffer.toString();
						int wordWidth = gc.stringExtent(word).x;
						if (x + wordWidth > rightMargin) {
							/* word doesn't fit on current line, so wrap */
							x = leftMargin;
							y += lineHeight;
							if (y + lineHeight > bottomMargin) {
								printer.endPage();
								if (index + 1 < end) {
									y = topMargin;
									printer.startPage();
								}
							}
						}
						gc.drawString(word, x, y, false);
						x += wordWidth;
						wordBuffer = new StringBuffer();
					}
					x = leftMargin;
					y += lineHeight;
					if (y + lineHeight > bottomMargin) {
						printer.endPage();
						if (index + 1 < end) {
							y = topMargin;
							printer.startPage();
						}
					}
				} else {
					if (c != '\t') {
						wordBuffer.append(c);
					}
					if (Character.isWhitespace(c)) {
						if (wordBuffer.length() > 0) {
							String word = wordBuffer.toString();
							int wordWidth = gc.stringExtent(word).x;
							if (x + wordWidth > rightMargin) {
								/* word doesn't fit on current line, so wrap */
								x = leftMargin;
								y += lineHeight;
								if (y + lineHeight > bottomMargin) {
									printer.endPage();
									if (index + 1 < end) {
										y = topMargin;
										printer.startPage();
									}
								}
							}
							gc.drawString(word, x, y, false);
							x += wordWidth;
							wordBuffer = new StringBuffer();
						}
						if (c == '\t') {
							x += tabWidth;
						}
					}
				}
			}
		}
		if (y + lineHeight <= bottomMargin) {
			printer.endPage();
		}
	}

//	void printWordBuffer() {
//		if (wordBuffer.length() > 0) {
//			String word = wordBuffer.toString();
//			int wordWidth = gc.stringExtent(word).x;
//			if (x + wordWidth > rightMargin) {
//				/* word doesn't fit on current line, so wrap */
//				newline();
//			}
//			gc.drawString(word, x, y, false);
//			x += wordWidth;
//			wordBuffer = new StringBuffer();
//		}
//	}
//
//	void newline(int x) {
//		x = leftMargin;
//		y += lineHeight;
//		if (y + lineHeight > bottomMargin) {
//			printer.endPage();
//			if (index + 1 < end) {
//				y = topMargin;
//				printer.startPage();
//			}
//		}
//	}


	private void printImage(Printer printer, Composite adapter, int leftMargin,
			int topMargin, int rightMargin, int bottomMargin) {

		Composite taskForm = findTaskForm(adapter);
		Rectangle bounds = taskForm.getBounds();
		if (bounds.height < 900) {
			Composite parent = taskForm.getParent().getParent();
			if (parent.getBounds().height != bounds.height) {
				bounds.height = 950;
				taskForm.setBounds(bounds);
			}
		}
		final Image image = new Image(printer, taskForm.getBounds());
		taskForm.redraw(bounds.x, bounds.y, bounds.width, bounds.height, true);
		GC gc2 = new GC(image);
		taskForm.print(gc2);
		gc2.dispose();

		int imageWidth = image.getBounds().width;
		int imageHeight = image.getBounds().height;
		int pageWidth = rightMargin - leftMargin;
		int pageHeight = bottomMargin - topMargin;
		double widthRatio = 1. * pageWidth / imageWidth;
		int imagePageHeight = (int) (pageHeight / widthRatio);
		int docHeight = (int) (imageHeight * widthRatio);
		int pageNumber = (int) Math.ceil(1. * docHeight / pageHeight);
		int finalPageHeight = (int) (pageHeight * (1 - pageNumber + 
				1. * docHeight / pageHeight));
		int finalHeight = (int) (finalPageHeight / widthRatio);

		for (int i = 0; i < pageNumber; i ++) {
			// Starts a new page.
			int startY = imagePageHeight * i;
			if(printer.startPage()) {
				GC gc = new GC(printer);
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
				String text = sdf.format(cal.getTime());
				text += " Page:" + (i + 1) + "/" + pageNumber;
				gc.drawString(text, 50, 30);
				gc.drawImage(image, 0, startY, imageWidth, 
						i < pageNumber - 1 ? imagePageHeight : finalHeight,
						leftMargin, topMargin, pageWidth, 
						i < pageNumber - 1 ? pageHeight : finalPageHeight);

				// Finishes the page. 
				printer.endPage();
				gc.dispose();
			}
		}

		
	}

	protected void refreshUI() {
		if (isDisposed()) {
			return;
		}
		for (Control child : getChildren()) {
			child.dispose();
		}
		getWorkflow().removeEventListener(workfloEventHandler);	
		createUI();
	}
	
	protected void saveTempWorkflow(String filename){
		
		try {
			IFolder folder = SicsVisualBatchEditor.getProjectFolder(
					EXPERIMENT_PROJECT, AUTOSAVE_FOLDER);
			IFile file = folder.getFile(filename);
//			if (file.exists())
//				throw new FitterException("function with the same name already exists");
    		  OutputStream out = new FileOutputStream(file.getLocation().toFile());
    		  WorkflowFactory.saveWorkflow(getWorkflow(), out);
    		  out.flush();
    		  out.close();
    		  // Refresh UI
    		  file.getParent().refreshLocal(1, new NullProgressMonitor());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getBatchDateString() {
		Date date = Calendar.getInstance().getTime();
		return new SimpleDateFormat("yyMMddHHmmss").format(date);
	}
	
}
