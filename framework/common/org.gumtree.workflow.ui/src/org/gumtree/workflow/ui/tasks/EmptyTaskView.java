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

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.workflow.ui.AbstractTaskView;

/**
 * Empty task view is a generic view for task with default UI.
 * 
 * @since 1.0
 */
public class EmptyTaskView extends AbstractTaskView {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.gumtree.ui.util.IPartControlProvider#createPartControl(org.eclipse
	 * .swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		getToolkit().createLabel(parent, "");
	}

}
