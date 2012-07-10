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

package org.gumtree.ui.service.launcher.support;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.PlatformUI;
import org.gumtree.ui.service.launcher.AbstractLauncher;
import org.gumtree.ui.service.launcher.LauncherException;
import org.gumtree.ui.util.SafeUIRunner;

public class ViewLauncher extends AbstractLauncher implements IExecutableExtension {

	private String viewId;
	
	public ViewLauncher() {
		super();
	}

	public void launch() throws LauncherException {
		if (viewId == null) {
			return;
		}
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().showView(viewId);
			}
		});
	}

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		// propertyName == "class"
		viewId = (String)data;
	}
	
}
