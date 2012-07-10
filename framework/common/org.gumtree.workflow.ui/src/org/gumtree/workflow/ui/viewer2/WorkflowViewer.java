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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class WorkflowViewer extends AbstractWorkflowViewer {

	public WorkflowViewer(Composite parent, int style) {
		super(parent, style);
	}
	
	protected IWorkflowViewerComponent createToolViewer(Composite parent) {
		return new TaskLibraryViewer(parent, SWT.NONE);
	}

}
