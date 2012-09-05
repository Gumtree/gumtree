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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.forms.FormControlWidget;
import org.gumtree.widgets.swt.util.UIResources;

public class BatchBufferGadget extends FormControlWidget {

	private static final String TEXT_NO_RUNNING_BUFFER = "No running buffer";
	
	private IBatchBufferManager manager;
	
	private IEventHandler<BatchBufferManagerEvent> managerEventHandler;
	
	private Label statusLabel;
	
	private Label currentBufferLabel;
	
	public BatchBufferGadget(Composite parent, int style) {
		super(parent, style);
	}

	private void createUI() {
		GridLayoutFactory.swtDefaults().applyTo(this);
		statusLabel = getToolkit().createLabel(this, "\n\n\n", SWT.CENTER);
		statusLabel.setFont(UIResources.getDefaultFont(SWT.BOLD));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(statusLabel);
		
		currentBufferLabel = getToolkit().createLabel(this, TEXT_NO_RUNNING_BUFFER, SWT.NONE);
		currentBufferLabel.setForeground(getForeground());
		currentBufferLabel.setBackground(getBackground());
		currentBufferLabel.setFont(UIResources.getDefaultFont(SWT.BOLD));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(currentBufferLabel);
	}
	
	private void updateStatus(final BatchBufferManagerStatus status) {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (isDisposed()) {
					return;
				}
				statusLabel.setText("\n" + status.name() + "\n\n");
				if (status.equals(BatchBufferManagerStatus.DISCONNECTED)) {
					statusLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
					currentBufferLabel.setText(TEXT_NO_RUNNING_BUFFER);
				} else if (status.equals(BatchBufferManagerStatus.IDLE)) {
					statusLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
					currentBufferLabel.setText(TEXT_NO_RUNNING_BUFFER);
				} else if (status.equals(BatchBufferManagerStatus.PREPARING)) {
					statusLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
				} else if (status.equals(BatchBufferManagerStatus.EXECUTING)) {
					statusLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
				}
			}
		});
	}
	
	private void updateExecutingBuffer(final String buffername) {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (isDisposed()) {
					return;
				}
				currentBufferLabel.setText("Running: " + buffername);
			}
		});
	}
	
	protected void widgetDispose() {
		if (manager != null) {
			manager.removeEventHandler(managerEventHandler);
			managerEventHandler = null;
			manager = null;
		}
		statusLabel = null;
		currentBufferLabel = null;
	}

	public void afterParametersSet() {
		// Create UI
		createUI();
		// Fetch initial value
		manager = ServiceUtils.getService(IBatchBufferManager.class);
		updateStatus(manager.getStatus());
		// Setup listener
		managerEventHandler = new IEventHandler<BatchBufferManagerEvent>() {
			public void handleEvent(BatchBufferManagerEvent event) {
				if (event instanceof BatchBufferManagerStatusEvent) {
					updateStatus(((BatchBufferManagerStatusEvent) event)
							.getStatus());
				} else if (event instanceof BatchBufferManagerExecutionEvent) {
					updateExecutingBuffer(((BatchBufferManagerExecutionEvent) event)
							.getBuffername());
				}
			}
		};
		manager.addEventHandler(managerEventHandler);
	}

}
