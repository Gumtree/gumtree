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

package org.gumtree.gumnix.sics.batch.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.gumtree.gumnix.sics.batch.ui.buffer.BatchBufferQueueViewer;
import org.gumtree.gumnix.sics.batch.ui.buffer.IBatchBuffer;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.util.bean.AbstractModelObject;
import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.viewer2.AbstractWorkflowViewerComponent;
import org.gumtree.workflow.ui.viewer2.IWorkflowViewerComponent;
import org.gumtree.workflow.ui.viewer2.WorkflowComposerViewer;

public class VisualBatchBufferComposer extends AbstractWorkflowViewerComponent {
	
	// Temporary object for registering change listener
	public final static String SHOW_RUN_QUEUE_PROPERTY_ID = "gumtree.workflow.showRunQueue";
	private IBatchBuffer buffer;
	private boolean showRunQueue = true;
	
	// Monitoring buffer name
	private PropertyChangeListener listener;
	
	private Group composerGroup;
	
	public VisualBatchBufferComposer(Composite parent, int style) {
		super(parent, style);
		try {
			String showRunQueueProperty = System.getProperty(SHOW_RUN_QUEUE_PROPERTY_ID);
			showRunQueue = Boolean.valueOf(showRunQueueProperty);
		} catch (Exception e) {
		}
		listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				SafeUIRunner.asyncExec(new SafeRunnable() {
					public void run() throws Exception {
						if (composerGroup == null || buffer == null) {
							return;
						}
						composerGroup.setText(buffer.getName());
					}
				});
			}
		};
	}

	protected void componentDispose() {
		// Note: this is for final cleanup
		if (buffer != null) {
			((AbstractModelObject) buffer).removePropertyChangeListener("name",
					listener);
			listener = null;
			buffer = null;
		}
		composerGroup = null;
	}
	
	protected void createUI() {
		if (isDisposed()) {
			return;
		}
		GridLayoutFactory.swtDefaults().applyTo(this);
		
		SashForm sashForm = new SashForm(this, SWT.HORIZONTAL);
		getToolkit().adapt(sashForm);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(sashForm);
		
		composerGroup = new Group(sashForm, SWT.NONE);
		String bufferName = getWorkflow().getContext().get(
				VisualBatchBuffer.PROP_BUFFER_NAME, String.class);
		if (bufferName == null) {
			bufferName = "Task Editor";
		}
		composerGroup.setText(bufferName);
		composerGroup.setLayout(new FillLayout());
		getToolkit().adapt(composerGroup);
		IWorkflowViewerComponent composerViewer = new WorkflowComposerViewer(composerGroup, SWT.Hide);
		configureViewerComponent(composerViewer);
		
		Group batchGroup = new Group(sashForm, SWT.NONE);
		batchGroup.setText("Run Queue");
		batchGroup.setLayout(new FillLayout());
		getToolkit().adapt(batchGroup);
		BatchBufferQueueViewer queueViewer = new BatchBufferQueueViewer(
				batchGroup, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		queueViewer.afterParametersSet();
		queueViewer.getViewer().addDoubleClickListener(new IDoubleClickListener() {			
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selections = (IStructuredSelection) event.getSelection();
				if (selections.size() != 1) {
					return;
				}
				IBatchBuffer buffer = (IBatchBuffer) selections.getFirstElement();
				if (buffer.getSource() instanceof IWorkflow) {
					// Remove old listener
							if (VisualBatchBufferComposer.this.buffer != null) {
								((AbstractModelObject) VisualBatchBufferComposer.this.buffer)
										.removePropertyChangeListener("name",
												listener);
							}
					// Swap workflow
					getWorkflowViewer().setWorkflow((IWorkflow) buffer.getSource());
					// Register new listener
					VisualBatchBufferComposer.this.buffer = buffer;
					((AbstractModelObject) VisualBatchBufferComposer.this.buffer)
							.addPropertyChangeListener("name", listener);
				}
			}
		});
		if (showRunQueue) {
			sashForm.setWeights(new int[] { 9, 1 });
		} else {
			sashForm.setWeights(new int[] { 10, 0 });
		}
//		sashForm.setMaximizedControl(composerGroup);
		IWorkflowViewerComponent controlViewer = new VisualBatchBufferControlViewer(this, SWT.NONE);
		configureViewerComponent(controlViewer);
		GridDataFactory.fillDefaults().grab(true, false).applyTo((Control) controlViewer);
	}

	protected void refreshUI() {
		if (isDisposed()) {
			return;
		}
		for (Control child : getChildren()) {
			child.dispose();
		}
		// hack -- don't do this as it will de-register the buffer listener
//		componentDispose();
		createUI();
	}
	
}
