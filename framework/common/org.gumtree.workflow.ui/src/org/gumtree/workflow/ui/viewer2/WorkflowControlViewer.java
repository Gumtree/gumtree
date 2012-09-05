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

package org.gumtree.workflow.ui.viewer2;

import java.io.FileOutputStream;
import java.io.OutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.widgets.swt.util.UIResourceManager;
import org.gumtree.widgets.swt.util.UIResources;
import org.gumtree.workflow.ui.WorkflowState;
import org.gumtree.workflow.ui.events.WorkflowEvent;
import org.gumtree.workflow.ui.events.WorkflowStateEvent;
import org.gumtree.workflow.ui.events.WorkflowStructuralEvent;
import org.gumtree.workflow.ui.internal.Activator;
import org.gumtree.workflow.ui.internal.InternalImage;
import org.gumtree.workflow.ui.util.WorkflowFactory;
import org.gumtree.workflow.ui.util.WorkflowUI;
import org.gumtree.workflow.ui.util.WorkflowUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowControlViewer extends AbstractWorkflowViewerComponent {

	private static final Logger logger = LoggerFactory.getLogger(WorkflowControlViewer.class);
	
	private UIContext c;
	
	private IEventHandler<WorkflowEvent> workfloEventHandler;
	
	public WorkflowControlViewer(Composite parent, int style) {
		super(parent, style);
	}

	protected void componentDispose() {
		if (workfloEventHandler != null) {
			getWorkflow().removeEventListener(workfloEventHandler);
			workfloEventHandler = null;
		}
		c = null;
	}

	protected void createUI() {
		GridLayoutFactory.swtDefaults().numColumns(4).applyTo(this);
		c = new UIContext();
		UIResourceManager resourceManager = new UIResourceManager(Activator.PLUGIN_ID, this);
		
		// Title
		String title = WorkflowUtils.getWorkflowTitle(getWorkflow());
		Label titleLabel = getToolkit().createLabel(this, title);
		Font titleFont = resourceManager.createRelativeFont(titleLabel.getFont(), 8, SWT.BOLD);
		titleLabel.setFont(titleFont);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).grab(true, false).applyTo(titleLabel);
		
		// Start-Pause button
		c.startPauseButton = getToolkit().createLabel(this, "");
		c.startPauseButton.setImage(InternalImage.PLAY.getImage());
		c.startPauseButton.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
		c.startPauseButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				// Clear status
//				for (TaskUIContext context : taskUIContexts.values()) {
//					context.stackLayout.topControl = null;
//					context.statusComposite.layout();
//				}
				// Schedule the workflow rather than running it directly
				WorkflowUI.getWorkflowExecutor().schedule(getWorkflow());
			}
		});
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).grab(false, false).applyTo(c.startPauseButton);
		
		// Stop button
		c.stopButton = getToolkit().createLabel(this, "");
		c.stopButton.setImage(InternalImage.STOP.getImage());
		c.stopButton.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
		c.stopButton.setEnabled(false);
		c.stopButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				WorkflowUI.getWorkflowExecutor().stop(getWorkflow());
			}
		});
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).grab(false, false).applyTo(c.stopButton);
		
		// Save button
		c.saveButton = getToolkit().createLabel(this, "");
		c.saveButton.setImage(InternalImage.SAVE.getImage());
		c.saveButton.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
		c.saveButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				Shell parentShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				SaveAsDialog dialog = new SaveAsDialog(parentShell);
				dialog.open();
				IPath filePath = dialog.getResult();
				if (filePath != null) {
					// Fix file extension
					if (filePath.getFileExtension() == null) {
						filePath = filePath.addFileExtension("gwf");
					}
					IWorkspace workspace= ResourcesPlugin.getWorkspace();
					IFile file= workspace.getRoot().getFile(filePath);
					try {
						OutputStream out = new FileOutputStream(file.getLocation().toFile());
						WorkflowFactory.saveWorkflow(getWorkflow(), out);
						// Refresh UI
						file.getParent().refreshLocal(1, new NullProgressMonitor());
					} catch (Exception error) {
						logger.error("Cannot save file " + file.getName(), error);
					}
				}
			}
		});
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).grab(false, false).applyTo(c.saveButton);
		
		// Workflow event
		workfloEventHandler = new IEventHandler<WorkflowEvent>() {
			public void handleEvent(WorkflowEvent event) {
				if (event instanceof WorkflowStateEvent) {
					handleStateEvent((WorkflowStateEvent) event); 
				} else if (event instanceof WorkflowStructuralEvent) {
					handleStructuralEvent((WorkflowStructuralEvent) event);
				}
			}
			public void handleStateEvent(WorkflowStateEvent event) {
				if (event.getState().equals(WorkflowState.RUNNING) || 
						event.getState().equals(WorkflowState.SCHEDULED)) {
//					eventLogStack.clear();
					SafeUIRunner.asyncExec(new SafeRunnable() {
						public void run() throws Exception {
							c.startPauseButton.setEnabled(false);
							c.stopButton.setEnabled(true);
						}						
					});
				} else if (event.getState().equals(WorkflowState.STOPPING)) {
					SafeUIRunner.asyncExec(new SafeRunnable() {
						public void run() throws Exception {
							c.startPauseButton.setEnabled(false);
							c.stopButton.setEnabled(false);
						}						
					});
				} else  {
					SafeUIRunner.asyncExec(new SafeRunnable() {
						public void run() throws Exception {
							c.startPauseButton.setEnabled(true);
							c.stopButton.setEnabled(false);
						}						
					});
				}
				// Store in the log
//				eventLogStack.add(0, event);
//				SafeUIRunner.asyncExec(new SafeRunnable() {
//					public void run() throws Exception {
//						logViewer.setInput(eventLogStack.toArray(new IEvent[eventLogStack.size()]));
//					}
//				});
			}
			public void handleStructuralEvent(WorkflowStructuralEvent event) {
//				redrawUI();
			}
		};
		getWorkflow().addEventListener(workfloEventHandler);
		
//		taskEventHandler = new IEventHandler<TaskEvent>() {
//			public void handleEvent(TaskEvent event) {
//				updateTaskGroup(event.getPublisher(), event.getState());
//			}				
//		};
		getParent().layout(true, true);
	}
	
	protected void refreshUI() {
		if (isDisposed()) {
			return;
		}
		for (Control child : getChildren()) {
			child.dispose();
		}
		componentDispose();
		createUI();
	}
	
	/*************************************************************************
	 * Hepler classes
	 *************************************************************************/
	
	private class UIContext {
		private Label startPauseButton;
		private Label stopButton;
		private Label saveButton;
	}
	
}
