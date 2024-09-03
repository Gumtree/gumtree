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

package au.gov.ansto.bragg.wombat.ui.internal;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.ui.service.launcher.AbstractLauncher;
import org.gumtree.ui.service.launcher.LauncherException;
import org.gumtree.ui.service.multimonitor.IMultiMonitorManager;

public class WombatAnalysisLauncher extends AbstractLauncher {

	private static final String ID_PERSPECTIVE_DEFAULT = "org.gumtree.ui.isee.workbenchPerspective";
	public static final String ID_PERSPECTIVE_SCRIPTING = "au.gov.ansto.bragg.nbi.ui.scripting.ScriptingPerspective";

	public WombatAnalysisLauncher() {
	}

	@Override
	public void launch() throws LauncherException {
		
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (IWorkbenchWindow window : windows) {
			IWorkbenchPage[] pages = window.getPages();
			for (IWorkbenchPage page : pages) {
				IPerspectiveDescriptor[] perspectives = page.getOpenPerspectives();
				for (IPerspectiveDescriptor perspective : perspectives) {
					if (perspective.getId().equals(
							ID_PERSPECTIVE_SCRIPTING)) {
						page.setPerspective(perspective);
//						window.setActivePage(page);
						window.getShell().setActive();
						return;
					}
				}
			}
		}
		
		// Open control perspective on window 2
		IMultiMonitorManager mmManager = ServiceUtils.getService(IMultiMonitorManager.class);
		int numberOfOpenWindows = PlatformUI.getWorkbench().getWorkbenchWindowCount();
		mmManager.openWorkbenchWindow(ID_PERSPECTIVE_DEFAULT, 1, false);
		mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_SCRIPTING, 
				numberOfOpenWindows, 1, true);
	}

}
