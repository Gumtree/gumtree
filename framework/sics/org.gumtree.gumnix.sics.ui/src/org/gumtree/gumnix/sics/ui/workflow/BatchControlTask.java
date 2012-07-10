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

package org.gumtree.gumnix.sics.ui.workflow;


public class BatchControlTask {
	
}

//extends AbstractTask {
//
//	private static final String PATH_FILENAME_NOTIFYER = "/experiment/datafile";
//
//	private static Logger logger = LoggerFactory.getLogger(BatchControlTask.class);
//	
//	private IComponentController filenameNotifyer;
//	
//	private BatchListener batchListener;
//	
//	@Override
//	protected Object createModelInstance() {
//		return null;
//	}
//
//	@Override
//	protected ITaskView createViewInstance() {
//		return new BatchControlTaskView();
//	}
//
//	@Override
//	protected void initialise(ParameterMap parameters) throws WorkflowException {
//	}
//
//	@Override
//	protected void run() throws WorkflowException {
//	}
//
//	private class BatchControlTaskView extends AbstractTaskView {
//		
//		public void createPartControl(Composite parent) {
//			parent.setLayout(new GridLayout());
//			
//			/*****************************************************************
//			 * Display message when SICS is not ready
//			 *****************************************************************/
//			if (!SicsCore.getDefaultProxy().isConnected()) {
//				Label label = new Label(parent, SWT.NONE);
//				label.setText("SICS is not connected.");
//				GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, true).applyTo(label);
//				return;
//			}
//
//			Composite sicsStatusComposite = new Composite(parent, SWT.NONE);
//			sicsStatusComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
//					true, false));
//			createBatchStatusGroup(sicsStatusComposite);
//
//			Composite fileLocationComposite = new Composite(parent, SWT.NONE);
//			fileLocationComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
//					true, false));
//			createFileLocationGroup(fileLocationComposite);
//
//			Composite batchStatusComposite = new Composite(parent, SWT.NONE);
//			batchStatusComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
//					true, true));
//			createBatchContentGroup(batchStatusComposite);
//
//			Composite batchControlComposite = new Composite(parent, SWT.NONE);
//			batchControlComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
//					true, false));
//			createBatchControlGroup(batchControlComposite);
//
//			createActions();
//
//			updateUI(SicsCore.getSicsManager().control().batch().getStatus());
//
//			// add filename listener
//			filenameNotifyer = SicsCore.getSicsController().findComponentController(PATH_FILENAME_NOTIFYER);
//			if(filenameNotifyer != null) {
//				filenameListener = new DynamicControllerListenerAdapter() {
//					public void valueChanged(IDynamicController controller, IComponentData newValue) {
//						final File file = new File(newValue.getStringData().trim());
//						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
//							public void run() {
//								datafileText.setText(file.getName());
//							}
//						});
//					}
//				};
//				filenameNotifyer.addComponentListener(filenameListener);
//			} else {
//				getLogger().warn("Data file notifer (" + PATH_FILENAME_NOTIFYER + ") is not found in SICS.");
//			}
//
//			batchListener = new BatchListener();
//			SicsCore.getSicsManager().control().batch().addListener(batchListener);
//		}
//		
//		private void createBatchStatusGroup(Composite parent) {
//			parent.setLayout(new FillLayout());
//			Group group = new Group(parent, SWT.NONE);
//			group.setText("Batch Status");
//			group.setLayout(new GridLayout(2, false));
//			Display display = PlatformUI.getWorkbench().getDisplay();
//			statusText = new Text(group, SWT.READ_ONLY | SWT.CENTER);
//			statusText.setText("READY");
//			statusText.setBackground(display.getSystemColor(SWT.COLOR_GREEN));
//			statusText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
//			timerWidget = new TimerWidget(group, SWT.NONE);
//		}
//
//
//		private void createFileLocationGroup(Composite parent) {
//			parent.setLayout(new FillLayout());
//			Group group = new Group(parent, SWT.NONE);
//			group.setText("Files Location");
//			group.setLayout(new GridLayout(2, false));
//			Label label = new Label(group, SWT.NONE);
//			label.setText("Script file name: ");
//			scriptNameText = new Text(group, SWT.READ_ONLY | SWT.BORDER);
//			scriptNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//
//			label = new Label(group, SWT.NONE);
//			label.setText("Data file name: ");
//			datafileText = new Text(group, SWT.READ_ONLY | SWT.BORDER);
//			datafileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//		}
//
//		private void createBatchContentGroup(Composite parent) {
//			parent.setLayout(new FillLayout());
//			Group group = new Group(parent, SWT.NONE);
//			group.setText("Batch Content");
//			group.setLayout(new FillLayout());
//			commandText = new StyledText(group, SWT.BORDER | SWT.READ_ONLY
//					| SWT.H_SCROLL | SWT.V_SCROLL);
//			// Add drop support
//			int operations = DND.DROP_MOVE | DND.DROP_COPY;
//			DropTarget dropTarget = new DropTarget(commandText, operations);
//			dropTarget.setTransfer(new Transfer[] { FileTransfer.getInstance(), EditorInputTransfer.getInstance() });
//			dropTarget.addDropListener(new DropTargetAdapter() {
//				public void drop(DropTargetEvent event) {
//					if (FileTransfer.getInstance().isSupportedType(
//							event.currentDataType)) {
//						String[] files = (String[]) event.data;
//						if (files.length == 1) {
//							setCommandTextFromFile(files[0]);
//						}
//					} else if (EditorInputTransfer.getInstance().isSupportedType(event.currentDataType) && 
//							event.data instanceof EditorInputData[]) {
//						// [GT-51] Making DnD to work with remote system explorer
//						EditorInputData[] inputDatas = ((EditorInputData[]) event.data);
//						if (inputDatas.length == 1) {
//							IEditorInput input = inputDatas[0].input;
//							if (input instanceof FileEditorInput) {
//								IFile file = ((FileEditorInput) input).getFile();
//								try {
//									InputStream in = file.getContents();
//									BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//									setCommandTextFromReader(reader, file.getLocation().toOSString());
//								} catch (CoreException e) {
//									getLogger().error("Failed to load file " + file.toString() + " from editor input.", e);
//								}
//							}
//						}
//					}
//				}
//			});
//		}
//
//		private void createBatchControlGroup(Composite parent) {
//			parent.setLayout(new FillLayout());
//			Group group = new Group(parent, SWT.NONE);
//			group.setText("Batch Control");
//			group.setLayout(new FillLayout(SWT.HORIZONTAL));
//
//			validateButton = new Button(group, SWT.PUSH);
//			validateButton.setText("Open Validator");
//			validateButton.addSelectionListener(new SelectionAdapter() {
//				public void widgetSelected(SelectionEvent e) {
//					ValidationDialog dialog = new ValidationDialog(getSite().getShell());
//					dialog.setTitle("Batch Validation");
//					if (commands != null) {
//						dialog.setCommands(commands.toArray(new String[commands.size()]));
//					}
//					dialog.open();
//				}
//			});
//
//			controlButton = new Button(group, SWT.PUSH);
//			controlButton.setText("Run Batch");
//			controlButton.setImage(ICON_START);
//			controlButton.addSelectionListener(new SelectionAdapter() {
//				public void widgetSelected(SelectionEvent e) {
//					BatchStatus status = SicsCore.getSicsManager().control().batch()
//							.getStatus();
//					// [GUMTREE-76] Send file name instead of the whole path
//					File scriptFile = new File(scriptNameText.getText());
//					String scriptName = scriptFile.getName();
//					// Replace space characters to underscore so the
//					// whole file name can be preserved.
//					scriptName = scriptName.replaceAll("\\s", "_");
//					// It must run with script name
//					if(scriptName == null) {
//						return;
//					}
//					if (status.equals(BatchStatus.IDLE)
//							|| status.equals(BatchStatus.READY)) {
//						if (commands != null && commands.size() > 0) {
//							try {
//								SicsCore.getSicsManager().control()
//										.batch()
//										.run(
//												commands
//														.toArray(new String[commands
//																.size()]), scriptName);
//
//								// Resets highlight colour
//								StyleRange styleRange = new StyleRange();
//								styleRange.start = 0;
//								styleRange.length = commandText.getCharCount();
//								styleRange.background = PlatformUI.getWorkbench()
//										.getDisplay().getSystemColor(
//												SWT.COLOR_WHITE);
//								commandText.setStyleRange(styleRange);
//							} catch (SicsIOException e1) {
//								getLogger()
//										.error(
//												"Error in running batch from control view.",
//												e1);
//							}
//						}
//					} else if (status.equals(BatchStatus.RUNNING)) {
//						try {
//							SicsCore.getSicsManager().control().batch().interrupt();
//						} catch (SicsIOException e1) {
//							getLogger()
//									.error(
//											"Error in interrupting batch from control view.",
//											e1);
//						}
//					}
//				}
//			});
//		}
//		
//	}
//	
//	private class BatchListener implements IBatchListener {
//		public void charExecuted(final int start, final int end) {
//		}
//
//		public void lineExecuted(final int line) {
//			getLogger().debug("lineExecuted " + line);
//			if (commandText != null && !commandText.isDisposed()) {
//				PlatformUI.getWorkbench().getDisplay().asyncExec(
//						new Runnable() {
//							public void run() {
//								commandText.setStyleRange(null);
//								int wordCount = 0;
//								for (int i = 0; i < line; i++) {
//									wordCount += commands.get(i)
//											.length() + 1;
//								}
//								StyleRange styleRange = new StyleRange();
//								styleRange.start = 0;
//								styleRange.length = wordCount;
//								styleRange.background = PlatformUI
//										.getWorkbench()
//										.getDisplay()
//										.getSystemColor(
//												SWT.COLOR_YELLOW);
//								commandText
//										.setStyleRange(styleRange);
//							}
//
//						});
//			}
//		}
//
//		public void statusChanged(final BatchStatus newStatus) {
//			if (PlatformUI.getWorkbench().getDisplay().isDisposed()
//					|| statusText == null || statusText.isDisposed()) {
//				return;
//			}
//			updateUI(newStatus);
//		}
//	}
//	
//}
