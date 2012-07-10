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

package org.gumtree.ui.util.workbench;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public abstract class ViewLaunchAction extends Action {

	public ViewLaunchAction() {
		super();
	}

	public ViewLaunchAction(String text, ImageDescriptor image) {
		super(text, image);
	}

	public void run() {
		if(getViewId() == null) {
			return;
		}
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					window.getActivePage().showView(getViewId());
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public abstract String getViewId();

}
