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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.control.batch.BatchStatus;
import org.gumtree.control.batch.IBatchControl;
import org.gumtree.control.batch.IBatchListener;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.ui.batch.SicsBatchViewer;
import org.gumtree.control.ui.batch.model.IControlBatchScript;
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
		if (!(input instanceof IControlBatchScript)) {
			return null;
		}
		
		// Creates listener
		batchListener = new IBatchListener() {
			@Override
			public void charExecuted(int start, int end) {
			}
			@Override
			public void lineExecuted(int line) {
			}
			@Override
			public void lineExecutionError(int line) {
			}
			@Override
			public void start() {
			}@Override
			public void stop() {
			}
			@Override
			public void scriptChanged(String scriptName) {
				// TODO Auto-generated method stub
				
			}@Override
			public void rangeExecuted(String rangeText) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void statusChanged(BatchStatus newStatus) {
				if (newStatus == BatchStatus.EXECUTING) {
					runDetected = true;
				} else {
					runDetected = false;
				}
				batchStatus = newStatus;
			}			
		};
		
		// Adds listener
		final IBatchControl batchControl = SicsManager.getBatchControl();
		batchStatus = batchControl.getStatus();
		batchControl.addListener(batchListener);
		
		// Run
		if (batchViewer != null) {
			// Make sure it is idle
			if (!(batchStatus.equals(BatchStatus.IDLE) || batchStatus.equals(BatchStatus.IDLE))) {
				batchControl.removeListener(batchListener);
				throw new WorkflowException("Batch control is not ready!");
			}
			// Prepare script
			String script = ((IControlBatchScript) input).toScript();
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
				batchControl.removeListener(batchListener);
				throw new WorkflowException("Batch control is not responding!");
			}
			// Wait until it has finished
			LoopRunner.run(new ILoopExitCondition() {
				public boolean getExitCondition() {
					return !batchStatus.equals(BatchStatus.EXECUTING);
				}
			}, LoopRunner.NO_TIME_OUT, 1000);
		}
		
		// Removes listener
		batchControl.removeListener(batchListener);
		
		return null;
	}

	protected void handleStop() {
		if (batchViewer != null) {
			batchViewer.interrupt();
		}
	}
	
	public Class<?>[] getInputTypes() {
		return new Class[] { IControlBatchScript.class };
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
