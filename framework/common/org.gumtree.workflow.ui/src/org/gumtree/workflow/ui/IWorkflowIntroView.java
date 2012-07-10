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

package org.gumtree.workflow.ui;

import org.gumtree.ui.util.workbench.IPartControlProvider;
import org.gumtree.workflow.ui.viewer.WizardWorkflowViewer;

/**
 * Workflow intro view renders the first UI for a workflow.
 *  
 * @see WizardWorkflowViewer
 * @since 1.0
 */
public interface IWorkflowIntroView extends IPartControlProvider {

	/**
	 * Handles action when start button is pressed in the intro.
	 */
	public void handleStartAction();
	
}
