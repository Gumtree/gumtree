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

package org.gumtree.workflow.ui.tasks;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;
import org.gumtree.workflow.ui.AbstractTask;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;

public class TextInputTask extends AbstractTask {

	// Used by run() method only
	private String textResult;
	
	@Override
	protected Object createModelInstance() {
		return null;
	}

	@Override
	protected ITaskView createViewInstance() {
		return new TextInputTaskView();
	}

	@Override
	protected Object run(Object input) {
		textResult = null;
		/*********************************************************************
		 * Open dialog
		 *********************************************************************/
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				InputDialog inputDialog = new InputDialog(PlatformUI
						.getWorkbench().getActiveWorkbenchWindow().getShell(),
						"Input text", "Text:", "", null);
				if (inputDialog.open() == Window.OK) {
					textResult = inputDialog.getValue();
				} else {
					textResult = "";
				}
			}			
		});
		/*********************************************************************
		 * Wait for result
		 *********************************************************************/
		// TODO: more efficient wait mechanism
		LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return textResult != null;
			}
		}, LoopRunner.NO_TIME_OUT, 500);
		/*********************************************************************
		 * Store result
		 *********************************************************************/
		return textResult;
	}

	protected void handleInterrupt() {
	}
	
	private class TextInputTaskView extends AbstractTaskView {

		public void createPartControl(Composite parent) {
			getToolkit().createLabel(parent, "Dialog will open to ask for text.");
		}
		
	}

	public Class<?>[] getInputTypes() {
		return null;
	}

	public Class<?>[] getOutputTypes() {
		return null;
	}
	
}
