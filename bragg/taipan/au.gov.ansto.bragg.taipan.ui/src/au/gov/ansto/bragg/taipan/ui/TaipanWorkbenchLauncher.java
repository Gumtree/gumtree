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

package au.gov.ansto.bragg.taipan.ui;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.gumtree.ui.service.launcher.AbstractLauncher;
import org.gumtree.ui.service.launcher.LauncherException;
import org.gumtree.ui.service.multimonitor.IMultiMonitorManager;
import org.gumtree.ui.service.multimonitor.support.MultiMonitorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaipanWorkbenchLauncher extends AbstractLauncher {

	
	private static final String ID_PERSPECTIVE_SCRIPTING = "au.gov.ansto.bragg.nbi.ui.scripting.ScriptingPerspective";
	
	private static final String ID_PERSPECTIVE_EXPERIMENT = "au.gov.ansto.bragg.nbi.ui.SICSExperimentPerspective";
	
	// Use the default as buffer to hold the editor
	private static final String ID_PERSPECTIVE_DEFAULT = "au.gov.ansto.bragg.nbi.ui.EmptyPerspective";
	
	private static Logger logger = LoggerFactory.getLogger(TaipanWorkbenchLauncher.class);
	
	
	public TaipanWorkbenchLauncher() {
	}

	public void launch() throws LauncherException {
		{			
			// TODO: move this logic to experiment UI manager service
			final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			final IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			for (IWorkbenchWindow window : windows){
				if (window != null && window != activeWorkbenchWindow) {
					window.close();
				}
			}
			if (activeWorkbenchWindow instanceof WorkbenchWindow) {
//				activeWorkbenchWindow.getActivePage().closeAllPerspectives(true, false);
				IWorkbenchPage[] pages = activeWorkbenchWindow.getPages();
				for (IWorkbenchPage page : pages) {
					try {
						IPerspectiveDescriptor[] perspectives = page.getOpenPerspectives();
						for (IPerspectiveDescriptor perspective : perspectives) {
							if (!ID_PERSPECTIVE_EXPERIMENT.equals(perspective.getId())){
								activeWorkbenchWindow.getActivePage().closePerspective(perspective, false, true);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			IPerspectiveListener listener = new IPerspectiveListener() {
				
				@Override
				public void perspectiveChanged(IWorkbenchPage page,
						IPerspectiveDescriptor perspective, String changeId) {
//					if (perspective.getId().equals(ID_PERSPECTIVE_SCRIPTING)) {
//						page.hideView(page.findViewReference("org.gumtree.app.workbench.cruisePanel"));
//					} else if (perspective.getId().equals(ID_PERSPECTIVE_EXPERIMENT)) {
//						try {
//							page.showView("org.gumtree.app.workbench.cruisePanel");
//						} catch (PartInitException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
					((WorkbenchWindow) page.getWorkbenchWindow()).setCoolBarVisible(false);
				}
				
				@Override
				public void perspectiveActivated(IWorkbenchPage page,
						IPerspectiveDescriptor perspective) {
				}
			};
			
			IMultiMonitorManager mmManager = new MultiMonitorManager();
			// Attempt to close intro
			mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_EXPERIMENT, 0, 0, mmManager.isMultiMonitorSystem());
			
			if (PlatformUI.getWorkbench().getWorkbenchWindowCount() < 2) {
				// open new window as editor buffer
				mmManager.openWorkbenchWindow(ID_PERSPECTIVE_DEFAULT, 1, true);
			}
//			// position it
			mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_SCRIPTING, 1, 1, mmManager.isMultiMonitorSystem());
//
//			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
//			for (IWorkbenchWindow window : windows) {
//				window.addPerspectiveListener(listener);
//			}
			
		}
	}

}
