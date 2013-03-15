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

package au.gov.ansto.bragg.quokka.ui;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.gumtree.ui.service.launcher.AbstractLauncher;
import org.gumtree.ui.service.launcher.LauncherException;
import org.gumtree.ui.service.multimonitor.IMultiMonitorManager;
import org.gumtree.ui.service.multimonitor.support.MultiMonitorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuokkaWorkbenchLauncher extends AbstractLauncher {

	
	private static final String ID_PERSPECTIVE_EXPERIMENT = "au.gov.ansto.bragg.quokka.ui.scanPerspective";
	private static final String ID_PERSPECTIVE_SCRIPTING = "au.gov.ansto.bragg.nbi.ui.scripting.ScriptingPerspective";
	private static final String ID_PERSPECTIVE_DEFAULT = "au.gov.ansto.bragg.nbi.ui.EmptyPerspective";
	private static final String ID_PERSPECTIVE_SICS = "au.gov.ansto.bragg.nbi.ui.SICSExperimentPerspective";

	private static Logger logger = LoggerFactory.getLogger(QuokkaWorkbenchLauncher.class);
	
//	private static boolean isCoolBarVisable = true;
	
	public QuokkaWorkbenchLauncher() {
	}

	public void launch() throws LauncherException {
		{			
			// TODO: move this logic to experiment UI manager service
			
			final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (activeWorkbenchWindow instanceof WorkbenchWindow) {
				((WorkbenchWindow) activeWorkbenchWindow).setCoolBarVisible(false);
				activeWorkbenchWindow.getActivePage().closeAllPerspectives(true, false);
			}
//			IMultiMonitorManager mmManager = ServiceUtils.getService(IMultiMonitorManager.class);
			IMultiMonitorManager mmManager = new MultiMonitorManager();
			
			// Attempt to close intro
			mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_EXPERIMENT, 0, 0, mmManager.isMultiMonitorSystem());
//			try {
//				activeWorkbenchWindow.getActivePage().showView("org.gumtree.app.workbench.cruisePanel", null, IWorkbenchPage.VIEW_ACTIVATE);
//			} catch (PartInitException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			if (PlatformUI.getWorkbench().getWorkbenchWindowCount() < 2) {
				// open new window as editor buffer
				mmManager.openWorkbenchWindow(ID_PERSPECTIVE_SICS, 1, true);
			}
//			// position it
			mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_SCRIPTING, 1, 1, mmManager.isMultiMonitorSystem());
		}
	}

}
