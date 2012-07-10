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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.workflow.ui.AbstractTask;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;

public class InputDisplayTask extends AbstractTask {

	private Text displayText;
	
	@Override
	protected Object createModelInstance() {
		return null;
	}

	@Override
	protected ITaskView createViewInstance() {
		return new TextDisplayTaskView();
	}
	
	@Override
	protected Object run(final Object input) {
		if (input != null) {
			SafeUIRunner.asyncExec(new SafeRunnable() {
				public void run() throws Exception {
					if (!displayText.isDisposed()) {
						displayText.setText(input.toString());
					}
				}
			});
		}
		return input;
	}
	
	public Class<?>[] getInputTypes() {
		return new Class[] { Object.class };
	}
	
	public Class<?>[] getOutputTypes() {
		return new Class[] { Object.class };
	}
	
	private class TextDisplayTaskView extends AbstractTaskView {
		
		public void createPartControl(Composite parent) {
			parent.setLayout(new GridLayout());
			// Creates text widget
			displayText = getToolkit().createText(parent, "",
					SWT.READ_ONLY | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
			// Sets 100 px height
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(
					true, false).hint(SWT.DEFAULT, 100).applyTo(displayText);
		}
		
	}
	
}
