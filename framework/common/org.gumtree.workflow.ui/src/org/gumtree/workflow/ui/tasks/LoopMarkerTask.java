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

package org.gumtree.workflow.ui.tasks;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.workflow.ui.AbstractMarkerTask;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.MarkerType;
import org.gumtree.workflow.ui.WorkflowException;

public class LoopMarkerTask extends AbstractMarkerTask {

	public MarkerType getMarkerType() {
		return MarkerType.START;
	}
	
	public void setMarkerType(MarkerType type) {
	}
	
	@Override
	protected Object createModelInstance() {
		return null;
	}

	@Override
	protected ITaskView createViewInstance() {
		return new LoopMarkerTaskView();
	}

	@Override
	protected Object run(Object input) throws WorkflowException {
		return input;
	}

	private class LoopMarkerTaskView extends AbstractTaskView {
		public void createPartControl(Composite parent) {
			parent.setLayout(new FillLayout());
			getToolkit().createLabel(parent, "", SWT.SEPARATOR | SWT.HORIZONTAL);
		}		
	}
	
}
