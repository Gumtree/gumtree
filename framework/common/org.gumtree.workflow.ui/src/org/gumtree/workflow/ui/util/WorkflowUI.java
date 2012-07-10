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

package org.gumtree.workflow.ui.util;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.internal.Internal;
import org.gumtree.workflow.ui.internal.WorkflowEditorInput;

public final class WorkflowUI {

	public static IWorkflowManager getWorkflowManager() {
		return ServiceUtils.getService(IWorkflowManager.class);
	}

	public static IWorkflowRegistry getWorkflowRegistry() {
		return ServiceUtils.getService(IWorkflowRegistry.class);
	}

	public static IWorkflowExecutor getWorkflowExecutor() {
		return ServiceUtils.getService(IWorkflowExecutor.class);
	}

	public static void openWorkflowEditor(IWorkflow workflow)
			throws PartInitException {
		IEditorInput input = new WorkflowEditorInput(workflow);
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.openEditor(input, Internal.ID_EDITOR_WORKFLOW, true);
	}

	private WorkflowUI() {
		super();
	}

}
