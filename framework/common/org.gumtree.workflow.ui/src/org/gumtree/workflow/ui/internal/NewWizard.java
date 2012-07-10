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

package org.gumtree.workflow.ui.internal;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.util.WorkflowFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewWizard extends Wizard implements INewWizard {

	private static final Logger logger = LoggerFactory.getLogger(NewWizard.class);
	
	public NewWizard() {
		super();
	}

	@Override
	public boolean performFinish() {
		// Create a new workflow and editor input
		IWorkflow workflow = WorkflowFactory.createEmptyWorkflow();
		IEditorInput input = new WorkflowEditorInput(workflow);
		try {
			// Open a new automator editor
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().openEditor(input, Internal.ID_EDITOR_AUTOMATOR);
		} catch (PartInitException e) {
			logger.error("Failed to launch automator editor", e);
			return false;
		}
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

}
