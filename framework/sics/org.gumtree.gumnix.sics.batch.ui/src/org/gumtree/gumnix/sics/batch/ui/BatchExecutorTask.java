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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.gumnix.sics.batch.ui.model.ISicsBatchScript;
import org.gumtree.gumnix.sics.control.IBatchListener;
import org.gumtree.gumnix.sics.control.ISicsBatchControl.BatchStatus;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.ui.util.SicsBatchViewer;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;
import org.gumtree.util.LoopRunnerStatus;
import org.gumtree.workflow.ui.AbstractTask;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.WorkflowException;

public class BatchExecutorTask extends AbstractTask {

	private SicsBatchViewer batchViewer;
	
	private IBatchListener batchListener;
	
	private BatchStatus batchStatus;
	
	private boolean runDetected;
	
	@Override
	protected Object createModelInstance() {
		return null;
	}

	@Override
	protected ITaskView createViewInstance() {
		return new BatchExecutorTaskView();
	}

	@Override
	protected Object run(Object input) throws WorkflowException {
		// We relay on input, not context (don't know why...)
		if (!(input instanceof ISicsBatchScript)) {
			return null;
		}
		
		// Creates listener
		batchListener = new IBatchListener() {
			public void charExecuted(int start, int end) {
			}
			public void lineExecuted(int line) {
			}
			public void lineExecutionError(int line) {
			}
			public void statusChanged(BatchStatus newStatus) {
				if (newStatus == BatchStatus.RUNNING) {
					runDetected = true;
				} else {
					runDetected = false;
				}
				batchStatus = newStatus;
			}			
		};
		
		// Adds listener
		batchStatus = SicsCore.getSicsManager().control().batch().getStatus();
		SicsCore.getSicsManager().control().batch().addListener(batchListener);
		
		// Run
		if (batchViewer != null) {
			// Make sure it is idle
			if (!(batchStatus.equals(BatchStatus.IDLE) || batchStatus.equals(BatchStatus.READY))) {
				SicsCore.getSicsManager().control().batch().removeListener(batchListener);
				throw new WorkflowException("Batch control is not ready!");
			}
			// Prepare script
			String script = ((ISicsBatchScript) input).toScript();
			batchViewer.setCommandText(script);
			// Start
			runDetected = false;
			batchViewer.start();
			// Make sure it runs (wait for 5 sec)
			LoopRunnerStatus waitState = LoopRunner.run(new ILoopExitCondition() {
				public boolean getExitCondition() {
					return runDetected;
//					return batchStatus.equals(BatchStatus.RUNNING);
				}
			}, 5000, 1);
			if (waitState.equals(LoopRunnerStatus.TIMEOUT)) {
				SicsCore.getSicsManager().control().batch().removeListener(batchListener);
				throw new WorkflowException("Batch control is not responding!");
			}
			// Wait until it has finished
			LoopRunner.run(new ILoopExitCondition() {
				public boolean getExitCondition() {
					return !batchStatus.equals(BatchStatus.RUNNING);
				}
			}, LoopRunner.NO_TIME_OUT, 1000);
		}
		
		// Removes listener
		SicsCore.getSicsManager().control().batch().removeListener(batchListener);
		
		return null;
	}

	protected void handleStop() {
		if (batchViewer != null) {
			batchViewer.interrupt();
		}
	}
	
	public Class<?>[] getInputTypes() {
		return new Class[] { ISicsBatchScript.class };
	}

	public Class<?>[] getOutputTypes() {
		return null;
	}
	
	private class BatchExecutorTaskView extends AbstractTaskView {

		public void createPartControl(Composite parent) {
			parent.setLayout(new FillLayout());
			GridLayoutFactory.fillDefaults().margins(0, 0).applyTo(parent);
			
			Composite composite = getToolkit().createComposite(parent);
			GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 400).applyTo(composite);
			composite.setLayout(new FillLayout());
			
			batchViewer = new SicsBatchViewer();
			batchViewer.createPartControl(composite);

		}
		
	}
	
}
