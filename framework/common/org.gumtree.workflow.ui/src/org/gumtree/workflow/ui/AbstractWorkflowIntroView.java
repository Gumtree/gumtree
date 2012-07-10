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

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractWorkflowIntroView implements IWorkflowIntroView {

	private static Logger logger = LoggerFactory.getLogger(AbstractWorkflowIntroView.class);
	
	private FormToolkit toolkit;
	
	private boolean isDisposed;

	// Subclass should override this method 
	public void handleStartAction() {
	}
	
	protected FormToolkit getToolkit() {
		if (toolkit == null) {
			toolkit = new FormToolkit(Display.getDefault());
		}
		return toolkit;
	}

	public boolean isDisposed() {
		return isDisposed;
	}
	
	public void dispose() {
		if (isDisposed()) {
			logger.info("Unnecessary dispose method call.");
			return;
		}
		isDisposed = true;
		if (toolkit != null) {
			toolkit.dispose();
			toolkit = null;
		}
	}
	
	public void setFocus() {
	}
	
}
