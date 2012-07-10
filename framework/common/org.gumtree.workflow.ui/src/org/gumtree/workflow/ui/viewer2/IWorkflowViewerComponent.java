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

import org.gumtree.core.object.IConfigurable;
import org.gumtree.workflow.ui.IWorkflow;

public interface IWorkflowViewerComponent extends IConfigurable {

	public IWorkflow getWorkflow();
	
	public void setWorkflow(IWorkflow workflow);

	public IWorkflowViewer getWorkflowViewer();
	
	public void setWorkflowViewer(IWorkflowViewer workflowViewer);
	
}
