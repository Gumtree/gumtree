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

package org.gumtree.gumnix.sics.batch.ui.buffer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ResourceSelectionDialog;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.gumnix.sics.batch.ui.internal.InternalImage;
import org.gumtree.gumnix.sics.core.IInstrumentReadyManager;
import org.gumtree.gumnix.sics.core.IInstrumentReadyStatus;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.ISicsProxy;
import org.gumtree.gumnix.sics.io.ISicsProxyListener;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.swt.IDNDHandler;
import org.gumtree.ui.widgets.AutoScrollStyledText;
import org.gumtree.ui.widgets.TimerWidget;
import org.gumtree.util.bean.AbstractModelObject;
import org.gumtree.widgets.swt.forms.ExtendedFormComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchRunnerPage extends ExtendedFormComposite {

	private static final String KEY_PREV_DOC_LEN = "previousDocLength";
	
	private static final String CLEAR_LOT_FOR_NEW_SCRIPT = "gumtree.sics.clearLogForNewScript";
	
	private static final String PROP_REMOVE_DOUBLELINEFEED = "gumtree.sics.escapeDoubleLineFeed";
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private static final Logger logger = LoggerFactory.getLogger(BatchRunnerPage.class);
	
	private IBatchBufferManager batchBufferManager;

	private IDNDHandler<IBatchBufferManager> dndHandler;
	
	private IEventHandler<BatchBufferManagerEvent> eventHandler;
	
	private ISicsProxyListener batchChannelListener;
	
	private UIContext context;
	
	private boolean checkInstrumentReady;
	
	private boolean clearLog;
	
	private SashForm sashForm;
	
	private boolean needRemoveDoubleLinefeed = false;
	
	// Listener to the batch buffer queue and auto run
	private PropertyChangeListener propertyChangeListener;
	
	public BatchRunnerPage(Composite parent, int style) {
		super(parent, style);
		// Listen to batch buffer manager
		eventHandler = new IEventHandler<BatchBufferManagerEvent>() {
			public void handleEvent(final BatchBufferManagerEvent event) {
				if (event instanceof BatchBufferManagerStatusEvent) {
					logger.warn("process batch event: " + ((BatchBufferManagerStatusEvent) event).getStatus());
					updateStatus(((BatchBufferManagerStatusEvent) event).getStatus());
					String message = ((BatchBufferManagerStatusEvent) event).getMessage();
					if (message != null) {
						updateLogText(message);
					}
				}
			}
		};
		// Listen to sics batch log
		batchChannelListener = new SicsProxyListenerAdapter() {
			public void messageReceived(String message, String channelId) {
				if (channelId.equals(ISicsProxy.CHANNEL_RAW_BATCH)) {
					updateLogText(message);
				}
			}
		};
		// Update UI for queue
		propertyChangeListener = new PropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent event) {
				SafeUIRunner.asyncExec(new SafeRunnable() {
					public void run() throws Exception {
						if (context == null || isDisposed()) {
							return;
						}
						if (event.getPropertyName().equals("autoRun")) {
							context.autoRunButton.setSelection((Boolean) event.getNewValue());
						}
					}
				});
			}
		};
		
		clearLog = true;
		String prop = System.getProperty(CLEAR_LOT_FOR_NEW_SCRIPT);
		if (prop != null) {
			clearLog = Boolean.parseBoolean(prop);
		}
		prop = System.getProperty(PROP_REMOVE_DOUBLELINEFEED);
		if (prop != null) {
			try {
				needRemoveDoubleLinefeed = Boolean.valueOf(prop);
			} catch (Exception e) {
			}
		}
	}

	@PostConstruct
	protected void render() {
		// UI context for internal storage
		context = new UIContext();
		
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(this);
		
		// Control area
		Composite runControlArea = getToolkit().createComposite(this);
		GridDataFactory.fillDefaults().grab(false, true).applyTo(runControlArea);
		createRunControlContent(runControlArea);
		
		// Editor and log area
		Composite runMainArea = getToolkit().createComposite(this);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(runMainArea);
		createRunMainContent(runMainArea);
		
		getParent().layout(true, true);
	}
	
	private void createRunMainContent(Composite parent) {
		parent.setLayout(new FillLayout());
		sashForm = new SashForm(parent, SWT.VERTICAL);
		getToolkit().adapt(sashForm);

		// Preview
		Group previewGroup = new Group(sashForm, SWT.NONE);
		previewGroup.setText("Preview");
		getToolkit().adapt(previewGroup);
		previewGroup.setLayout(new FillLayout());
		previewGroup.setFont(JFaceResources.getBannerFont());
		context.previewText = new AutoScrollStyledText(previewGroup, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		context.previewText.setText("");
		context.previewText.setFont(JFaceResources.getBannerFont());

		// Editor
		Group editorGroup = new Group(sashForm, SWT.NONE);
		editorGroup.setText("Buffer");
		getToolkit().adapt(editorGroup);
		editorGroup.setLayout(new FillLayout());
		editorGroup.setFont(JFaceResources.getBannerFont());
		context.editorText = new AutoScrollStyledText(editorGroup, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		context.editorText.setText("");
		context.editorText.setFont(JFaceResources.getBannerFont());
		context.editorText.setData(KEY_PREV_DOC_LEN, 0);
		
		// Log
		Group logGroup = new Group(sashForm, SWT.NONE);
		logGroup.setText("Log");
		getToolkit().adapt(logGroup);
		logGroup.setLayout(new FillLayout());
		logGroup.setFont(JFaceResources.getBannerFont());
		context.logText = new AutoScrollStyledText(logGroup, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		context.logText.setFont(JFaceResources.getBannerFont());
		context.logText.setData(KEY_PREV_DOC_LEN, 0);
		
		sashForm.setWeights(new int[] { 0, 1, 1});
	}
	
	private void createRunControlContent(Composite parent) {
		GridLayoutFactory.swtDefaults().applyTo(parent);
		
		// Timer
		context.timerWidget = new TimerWidget(parent, SWT.NONE);
		getToolkit().adapt(context.timerWidget);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(context.timerWidget);
		
		// Status
		context.statusLabel = getToolkit().createLabel(parent, "", SWT.CENTER);
		context.statusLabel.setFont(JFaceResources.getBannerFont());
		GridDataFactory.fillDefaults().grab(false, false).hint(SWT.DEFAULT, 50).applyTo(context.statusLabel);
		
		// Current buffer
		Group currentGroup = new Group(parent, SWT.NONE);
		currentGroup.setText("Executing Buffer");
		getToolkit().adapt(currentGroup);
		GridLayoutFactory.fillDefaults().applyTo(currentGroup);
		GridDataFactory.fillDefaults().grab(false, false).applyTo(currentGroup);
		context.currentBuffer = getToolkit().createText(currentGroup, "", SWT.READ_ONLY);
		context.currentBuffer.setFont(JFaceResources.getBannerFont());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(context.currentBuffer);
		
		// Buffer queue
		Group queueGroup = new Group(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).spacing(2, 2).applyTo(queueGroup);
		queueGroup.setText("Buffer Queue");
		getToolkit().adapt(queueGroup);
		GridDataFactory.fillDefaults().grab(false, true).applyTo(queueGroup);
		context.queueViewer = new BatchBufferQueueViewer(queueGroup);
		context.queueViewer.setManager(getBatchBufferManager());
		context.queueViewer.afterParametersSet();
		GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(context.queueViewer);
		
		context.queueViewer.getViewer().addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = context.queueViewer.getViewer().getSelection();
				if (selection instanceof IStructuredSelection){
					Object obj = ((IStructuredSelection) selection).getFirstElement();
					if (obj instanceof IBatchBuffer) {
						context.previewText.setText(((IBatchBuffer) obj).getContent());
						sashForm.setWeights(new int[]{1, 1, 1});
					}
				}
			}
		});
		

		final Listener focusListener = new Listener() {
			public void handleEvent(Event event)
			{
//				if (!(event.widget instanceof Control))
//				{
//					return;
//				}

				boolean isPreviewChild = false;
				if (context == null || context.queueViewer == null || context.queueViewer.getViewer() == null 
						|| context.queueViewer.isDisposed()) {
					return;
				}
				for (Control c = (Control) event.widget; c != null; c = c.getParent())
				{
					if (c == context.queueViewer.getViewer().getControl())
					{
						isPreviewChild = true;
						break;
					}
					if (c == context.previewText){
						isPreviewChild = true;
						break;
					}
				}

				if (!isPreviewChild)
				{
					sashForm.setWeights(new int[]{0, 1, 1});
				}
			}
		};

		getDisplay().addFilter(SWT.FocusIn, focusListener);
		getDisplay().addFilter(SWT.FocusOut, focusListener);

		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				getDisplay().removeFilter(SWT.FocusIn, focusListener);
				getDisplay().removeFilter(SWT.FocusOut, focusListener);
			}
		});
		
		Button addWorkspaceButton = getToolkit().createButton(queueGroup, "Workspace", SWT.PUSH);
		addWorkspaceButton.setImage(InternalImage.ADD.getImage());
		addWorkspaceButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ResourceSelectionDialog dialog = new ResourceSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(), "");
				int returnCode = dialog.open();
				if (returnCode == Window.CANCEL) {
					return;
				}
				for (Object result : dialog.getResult()) {
					if (result instanceof IFile) {
						IFile file = (IFile) result;
						if (file.getName().endsWith(".tcl")) {
							IBatchBuffer buffer = new ResourceBasedBatchBuffer(
									file.getName(), file.getLocationURI());
							logger.warn("add workspace file " + buffer.getName());
							getBatchBufferManager().getBatchBufferQueue().add(buffer);
						}
					}
				}
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).applyTo(addWorkspaceButton);
		
		Button addFileSystemButton = getToolkit().createButton(queueGroup, "File System", SWT.PUSH);
		addFileSystemButton.setImage(InternalImage.ADD.getImage());
		addFileSystemButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				String result = dialog.open();
				if (result == null) {
					return;
				}
				// Only one (or zero) file is selected from the dialog
				File file = new File(result);
				if (file.getName().endsWith(".tcl")) {
					IBatchBuffer buffer = new ResourceBasedBatchBuffer(file
							.getName(), file.toURI());
					logger.warn("add local file " + buffer.getName());
					getBatchBufferManager().getBatchBufferQueue().add(buffer);
				}
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).applyTo(addFileSystemButton);
		
		Button removeButton = getToolkit().createButton(queueGroup, "Remove", SWT.PUSH);
		removeButton.setImage(InternalImage.DELETE.getImage());
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selections = (IStructuredSelection) context.queueViewer.getViewer().getSelection();
				logger.warn("Remove button clicked");
				getBatchBufferManager().getBatchBufferQueue().removeAll(selections.toList());
			}
		});
		GridDataFactory.fillDefaults().grab(false, false).span(2, 1).applyTo(removeButton);
		
//		Button pauseButton = getToolkit().createButton(queueGroup, "Pause", SWT.PUSH);
//		pauseButton.setImage(InternalImage.PAUSE.getImage());

		context.autoRunButton = getToolkit().createButton(parent, "PLAY", SWT.TOGGLE);
		context.autoRunButton.setImage(InternalImage.PLAY.getImage());
		context.autoRunButton.setFont(JFaceResources.getBannerFont());
		context.autoRunButton.setSelection(getBatchBufferManager().isAutoRun());
		GridDataFactory.fillDefaults().grab(false, false).span(2, 1).applyTo(context.autoRunButton);
		final boolean[] previousSelection = new boolean[] { context.autoRunButton.getSelection() };
		context.autoRunButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// Check instrument state
				logger.warn("run/pause button clicked");
				if (checkInstrumentReady && !getBatchBufferManager().isAutoRun()) {
					IInstrumentReadyManager manager = ServiceUtils.getService(IInstrumentReadyManager.class);
					IInstrumentReadyStatus status = manager.isInstrumentReady();
					if (!status.isInstrumentReady()) {
						// Prepare message
						StringBuilder builder = new StringBuilder();
						builder.append("Instrument is not ready due to the following reasons:\n");
						for (String message : status.getMessages()) {
							builder.append("  * " + message + "\n");
						}
						// Pop up dialog
						MessageDialog.openWarning(getShell(), "Instrument is not ready", builder.toString());
						// Disable run
						getBatchBufferManager().setAutoRun(false);
						((Button) e.widget).setSelection(false);
						logger.warn("instrument not ready, stop auto run");
						return;
					}
				} else if (getBatchBufferManager().isAutoRun()) {
					boolean confirm = MessageDialog.openConfirm(getShell(), "Confirm pausing the queue", "Please confirm if you want to pause the queue. " +
							"The current script running in SICS will not be affected. If confirmed, the rest of the scripts in the " +
							"buffer queue will not be processed.");
					if (!confirm) {
						getBatchBufferManager().setAutoRun(true);
						((Button) e.widget).setSelection(true);
						logger.warn("pausing the queue not confirmed, not pausing");
						return;
					}
				}
				boolean isAutoRun = ((Button) e.widget).getSelection();
				if (previousSelection[0] != isAutoRun) {
					if (isAutoRun) {
						context.autoRunButton.setText("Pause");
						context.autoRunButton.setImage(InternalImage.PAUSE.getImage());
						if (!clearLog) {
							context.logText.setText("");
							context.logText.resetScroll();
						}
						logger.warn("start the queue");
					} else {
						context.autoRunButton.setText("Play");
						context.autoRunButton.setImage(InternalImage.PLAY.getImage());
						logger.warn("pause the queue");
					}
					previousSelection[0] = isAutoRun;
				}
				logger.warn("auto run = " + isAutoRun);
				getBatchBufferManager().setAutoRun(isAutoRun);
				if (!isAutoRun) {
					getBatchBufferManager().resetBufferManagerStatus();
				}
			}
		});
		context.autoRunButton.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (previousSelection[0] != context.autoRunButton.getSelection()) {
					if (context.autoRunButton.getSelection()) {
						context.autoRunButton.setText("Pause");
						context.autoRunButton.setImage(InternalImage.PAUSE.getImage());
					} else {
						context.autoRunButton.setText("Play");
						context.autoRunButton.setImage(InternalImage.PLAY.getImage());
					}
					previousSelection[0] = context.autoRunButton.getSelection();
				}
			}
		});
		
		Button interruptButton = getToolkit().createButton(parent, "Interrupt", SWT.PUSH);
		interruptButton.setImage(InternalImage.INTERRUPT.getImage());
		interruptButton.setFont(JFaceResources.getBannerFont());
		interruptButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					SicsCore.getSicsController().interrupt();
					logger.warn("interrupt the batch runner");
				} catch (SicsIOException e1) {
					e1.printStackTrace();
				}
			}
		});
		GridDataFactory.fillDefaults().grab(false, false).span(2, 1).applyTo(interruptButton);
		
		Button checkInstrumentButton = getToolkit().createButton(parent, "Check instrument ready", SWT.CHECK);
		checkInstrumentButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				checkInstrumentReady = ((Button) e.widget).getSelection();
				logger.warn("checkInstrumentReady = " + checkInstrumentReady);
			}
		});
		checkInstrumentReady = true;
		checkInstrumentButton.setSelection(true);
		
		updateStatus(batchBufferManager.getStatus());
		
	}

	private void updateStatus(final BatchBufferManagerStatus status) {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (context == null || isDisposed()) {
					return;
				}
				logger.warn("update status to " + status.name());
				context.statusLabel.setText("\n" + status.name() + "\n");
				if (status.equals(BatchBufferManagerStatus.DISCONNECTED)) {
					context.statusLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
					context.currentBuffer.setText("");
					context.timerWidget.clearTimerUI();
				} else if (status.equals(BatchBufferManagerStatus.ERROR)) {
					context.statusLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
					context.currentBuffer.setText("");
					context.timerWidget.clearTimerUI();
				} else if (status.equals(BatchBufferManagerStatus.IDLE)) {
					context.statusLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
					context.currentBuffer.setText("");
					context.timerWidget.stopTimerUI();
				} else if (status.equals(BatchBufferManagerStatus.PREPARING)) {
					context.statusLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
					context.currentBuffer.setText("");
				} else if (status.equals(BatchBufferManagerStatus.EXECUTING)) {
					// Update status
					context.statusLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
					// Reset and start timer
					context.timerWidget.clearTimerUI();
					context.timerWidget.startTimerUI();
					// Update buffer name
					String buffername = getBatchBufferManager().getRunningBuffername();
					if (buffername != null) {
						context.currentBuffer.setText(getBatchBufferManager().getRunningBuffername());
					}
					// Update buffer if editor is empty
					if (context.editorText.getText().length() == 0) {
						String bufferContent = getBatchBufferManager().getRunningBufferContent();
						String rangeString = getBatchBufferManager().getRunningBufferRangeString();
						if (bufferContent != null && rangeString != null) {
							if (needRemoveDoubleLinefeed) {
								bufferContent = bufferContent.replaceAll("\n\n", "\n");
							}
							context.editorText.setText(bufferContent);
							highlightBuffer(rangeString);
						}
					}
				}
			}
		});
	}
	
	private void updateLogText(final String message) {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				// Don't update when is disposed
				if (context == null || isDisposed()) {
					return;
				}
				// Handle starting message
				if (message.startsWith("BATCHSTART=")) {
					// Update buffer (may be duplicated, but we need to ensure
					// this is available for highlight)
					String bufferContent = getBatchBufferManager().getRunningBufferContent();
					if (bufferContent != null) {
						if (needRemoveDoubleLinefeed) {
							bufferContent = bufferContent.replaceAll("\n\n", "\n");
						}
						context.editorText.setText(bufferContent);
					}
					// Clear log
					if (clearLog) {
						context.logText.setText("");
						context.logText.resetScroll();
					}
				} else if (message.startsWith("BATCHEND=")) {
					// Do nothing
				} else if (message.contains(".range = ")) {
					highlightBuffer(message);
				} else {
					// Otherwise append log message
					context.logText.append("[");
					context.logText.append(dateFormat.format(Calendar.getInstance().getTime()));
					context.logText.append("] ");
					context.logText.append(message);
					context.logText.append("\n");
				}
				// Update UI
				context.logText.autoScroll();
			}
		});
	}
	
	private void highlightBuffer(String rangeString) {
		// Highlight is not ready
		if (context.editorText.getText() == null
				|| context.editorText.getText().length() == 0) {
			return;
		}
		
		String[] charCounts = rangeString.split("=");
		StyleRange styleRange = new StyleRange();
		styleRange.start = 0;
		int length = Integer.parseInt(charCounts[2].trim());
		if (length > context.editorText.getText().length()){
			length = context.editorText.getText().length();
		}
		styleRange.length = length;
		styleRange.background = PlatformUI.getWorkbench()
				.getDisplay().getSystemColor(
						SWT.COLOR_YELLOW);
		try {
			context.editorText.setStyleRange(styleRange);			
		} catch (Exception e) {
		}
	}
	

	@Override
	protected void disposeWidget() {
		if (batchBufferManager != null) {
			batchBufferManager.removeEventHandler(eventHandler);
			batchBufferManager.getSicsManager().proxy().removeProxyListener(batchChannelListener);
			batchBufferManager = null;
			eventHandler = null;
			batchChannelListener = null;
		}
		if (context != null) {
			context.dispose();
			context = null;
		}
	}
	
	/*************************************************************************
	 * Components
	 *************************************************************************/
	
	public IBatchBufferManager getBatchBufferManager() {
		return batchBufferManager;
	}

	@Inject
	public void setBatchBufferManager(IBatchBufferManager batchBufferManager) {
		if (this.batchBufferManager != null) {
			this.batchBufferManager.removeEventHandler(eventHandler);
			this.batchBufferManager.getSicsManager().proxy().removeProxyListener(batchChannelListener);
			((AbstractModelObject) this.batchBufferManager).removePropertyChangeListener(propertyChangeListener);		
		}
		this.batchBufferManager = batchBufferManager;
		this.batchBufferManager.addEventHandler(eventHandler);
		this.batchBufferManager.getSicsManager().proxy().addProxyListener(batchChannelListener);
		((AbstractModelObject) this.batchBufferManager).addPropertyChangeListener(propertyChangeListener);
	}
	
	public IDNDHandler<IBatchBufferManager> getDndHandler() {
		return dndHandler;
	}

	@Inject
	public void setDndHandler(IDNDHandler<IBatchBufferManager> dndHandler) {
		this.dndHandler = dndHandler;
	}

	/*************************************************************************
	 * Internal model
	 *************************************************************************/
	
	private class UIContext {
		private Label statusLabel;
		private Text currentBuffer;
		private TimerWidget timerWidget;
		private BatchBufferQueueViewer queueViewer;
		private AutoScrollStyledText editorText;
		private AutoScrollStyledText previewText;
		private AutoScrollStyledText logText;
		private Button autoRunButton;
		private void dispose() {
			statusLabel = null;
			currentBuffer = null;
			timerWidget = null;
			logText = null;
			editorText = null;
			autoRunButton = null;
			previewText = null;
		}
	}

	
}
