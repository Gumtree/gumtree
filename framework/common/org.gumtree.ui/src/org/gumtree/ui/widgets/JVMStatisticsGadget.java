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

package org.gumtree.ui.widgets;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.forms.FormControlWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A gadget to monitor JVM memory footprint.
 * 
 * @author Tony Lam
 *
 */
public class JVMStatisticsGadget extends FormControlWidget {
	
	private static Logger logger = LoggerFactory.getLogger(JVMStatisticsGadget.class);
	
	private static final int UPDATE_INTERVAL = 1000;
	
	private Label processorLabel;
	
	private Label totalMemoryLabel;
	
	private Label freeMemoryLabel;
	
	private Job job;
	
	private boolean shouldContinue = true;

	public JVMStatisticsGadget(Composite parent, int style) {
		super(parent, style);
	}

	public void afterParametersSet() {
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);
		
		// Row 1
		Label label = new Label(this, SWT.NONE);
		label.setText("Processors: ");
		label.setForeground(getForeground());
		label.setBackground(getBackground());
		
		processorLabel = new Label(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(processorLabel);
		processorLabel.setForeground(getForeground());
		processorLabel.setBackground(getBackground());
		
		// Row 2
		label = new Label(this, SWT.NONE);
		label.setText("Total Memory: ");
		label.setForeground(getForeground());
		label.setBackground(getBackground());
		
		totalMemoryLabel = new Label(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(totalMemoryLabel);
		totalMemoryLabel.setForeground(getForeground());
		totalMemoryLabel.setBackground(getBackground());
		
		// Row 3
		label = new Label(this, SWT.NONE);
		label.setText("Free Memory: ");
		label.setForeground(getForeground());
		label.setBackground(getBackground());
		
		freeMemoryLabel = new Label(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(freeMemoryLabel);
		freeMemoryLabel.setForeground(getForeground());
		freeMemoryLabel.setBackground(getBackground());

		this.layout(true, true);
		
		// Schedule update
		job = new Job("JVM Monitoring Job") {
			protected IStatus run(IProgressMonitor monitor) {
				SafeUIRunner.asyncExec(new SafeRunnable() {
					public void run() throws Exception {
						updateUI();
					}
				});
				if (shouldContinue) {
					schedule(UPDATE_INTERVAL);
				} else {
					logger.debug("JVM monitoring is stopped");
				}
				return Status.OK_STATUS;
			}
			
		};
		job.schedule();		
	}
	
	protected void widgetDispose() {
		if (job != null) {
			job.cancel();
			job = null;
		}
		shouldContinue = false;
		processorLabel = null;
		totalMemoryLabel = null;
		freeMemoryLabel = null;
	}
	
	private void updateUI() {
		// Print data
		Runtime runtime = Runtime.getRuntime();
		printText(processorLabel, runtime.availableProcessors() + "");
		printText(totalMemoryLabel, runtime.totalMemory() / (1024 * 1024) + "MB");
		printText(freeMemoryLabel, runtime.freeMemory() / (1024 * 1024) + "MB");
		
		// Refresh UI
		if (processorLabel != null && !processorLabel.isDisposed()) {
			processorLabel.getParent().layout(true);	
		}
	}
	
	// Helper function to print text in a safer way
	private static void printText(Label label, String text) {
		if (label != null && !label.isDisposed()) {
			label.setText(text);
		}
	}
	
}
