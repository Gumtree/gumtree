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

package au.gov.ansto.bragg.quokka.ui.internal;

import org.eclipse.ui.PlatformUI;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.ui.service.launcher.AbstractLauncher;
import org.gumtree.ui.service.launcher.LauncherException;
import org.gumtree.ui.service.multimonitor.IMultiMonitorManager;

public class QuokkaAnalysisLauncher extends AbstractLauncher {

	private static final String ID_PERSPECTIVE_DEFAULT = "org.gumtree.ui.isee.workbenchPerspective";
	
	public QuokkaAnalysisLauncher() {
	}

	@Override
	public void launch() throws LauncherException {
		// Open control perspective on window 2
		IMultiMonitorManager mmManager = ServiceUtils.getService(IMultiMonitorManager.class);
		int numberOfOpenWindows = PlatformUI.getWorkbench().getWorkbenchWindowCount();
		mmManager.openWorkbenchWindow(ID_PERSPECTIVE_DEFAULT, 2, false);
		mmManager.showPerspectiveOnOpenedWindow(QuokkaAnalysisPerspective.PERSPECTIVE_ID, numberOfOpenWindows, 
				2, true);
	}

}
