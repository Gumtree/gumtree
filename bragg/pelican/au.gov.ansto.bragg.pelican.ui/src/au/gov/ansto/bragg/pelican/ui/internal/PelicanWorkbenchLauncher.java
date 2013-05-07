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

package au.gov.ansto.bragg.pelican.ui.internal;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.gumtree.ui.service.launcher.AbstractLauncher;
import org.gumtree.ui.service.launcher.LauncherException;
import org.gumtree.ui.service.multimonitor.IMultiMonitorManager;
import org.gumtree.ui.service.multimonitor.support.MultiMonitorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.nbi.ui.scripting.ScriptPageRegister;
import au.gov.ansto.bragg.nbi.ui.scripting.ScriptingPerspective;

public class PelicanWorkbenchLauncher extends AbstractLauncher {

	
	private static final String ID_PERSPECTIVE_SCRIPTING = "au.gov.ansto.bragg.nbi.ui.scripting.ScriptingPerspective";
	
	private static final String ID_PERSPECTIVE_EXPERIMENT = "au.gov.ansto.bragg.pelican.ui.TCLRunnerPerspective";
	
	// Use the default as buffer to hold the editor
	private static final String ID_PERSPECTIVE_DEFAULT = "au.gov.ansto.bragg.nbi.ui.EmptyPerspective";

	private static final String ID_PERSPECTIVE_STATUS = "au.gov.ansto.bragg.pelican.ui.PelicanStatusPerspective";
	
	private static Logger logger = LoggerFactory.getLogger(PelicanWorkbenchLauncher.class);
	
	
	public PelicanWorkbenchLauncher() {
	}

	public void launch() throws LauncherException {
		{			
			// TODO: move this logic to experiment UI manager service
			final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (activeWorkbenchWindow instanceof WorkbenchWindow) {
//				activeWorkbenchWindow.getActivePage().closeAllPerspectives(true, false);
				IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
				if (page != null) {
					IPerspectiveDescriptor[] perspectives = page.getOpenPerspectives();
					for (IPerspectiveDescriptor perspective : perspectives) {
						try {
							System.err.println(page.getPerspective().getId());
							if (!ID_PERSPECTIVE_EXPERIMENT.equals(perspective.getId())){
								activeWorkbenchWindow.getActivePage().closePerspective(page.getPerspective(), false, true);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
//			IMultiMonitorManager mmManager = new MultiMonitorManager();
//			
//			mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_EXPERIMENT, 0, 0, mmManager.isMultiMonitorSystem());
//			
//			if (PlatformUI.getWorkbench().getWorkbenchWindowCount() < 2) {
//				// open new window as editor buffer
//				mmManager.openWorkbenchWindow(ID_PERSPECTIVE_DEFAULT, 1, true);
//			}
//			
//			mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_SCRIPTING, 1, 1, mmManager.isMultiMonitorSystem());
//
//			activeWorkbenchWindow.addPerspectiveListener(new IPerspectiveListener() {
//				
//				@Override
//				public void perspectiveChanged(IWorkbenchPage page,
//						IPerspectiveDescriptor perspective, String changeId) {
//					if (perspective.getId().equals(ID_PERSPECTIVE_SCRIPTING)) {
//						page.hideView(page.findViewReference("org.gumtree.app.workbench.cruisePanel"));
//					} else if (perspective.getId().equals(ID_PERSPECTIVE_EXPERIMENT)) {
//						try {
//							page.showView("org.gumtree.app.workbench.cruisePanel");
//						} catch (PartInitException e) {
//							e.printStackTrace();
//						}
//					}
//				}
//				
//				@Override
//				public void perspectiveActivated(IWorkbenchPage page,
//						IPerspectiveDescriptor perspective) {
//				}
//			});
//			IPerspectiveListener listener = new IPerspectiveListener() {
//				
//				@Override
//				public void perspectiveChanged(IWorkbenchPage page,
//						IPerspectiveDescriptor perspective, String changeId) {
//					if (perspective.getId().equals(ID_PERSPECTIVE_SCRIPTING)) {
//						page.hideView(page.findViewReference("org.gumtree.app.workbench.cruisePanel"));
//					} else if (perspective.getId().equals(ID_PERSPECTIVE_EXPERIMENT)) {
//						try {
////							page.showView("org.gumtree.app.workbench.cruisePanel", null, IWorkbenchPage.VIEW_ACTIVATE);
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//				}
//				
//				@Override
//				public void perspectiveActivated(IWorkbenchPage page,
//						IPerspectiveDescriptor perspective) {
//				}
//			};
			
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
				mmManager.openWorkbenchWindow(ID_PERSPECTIVE_SCRIPTING, 1, true);
			}
//			// position it
			ScriptPageRegister register = new ScriptPageRegister();
			try {
				ScriptingPerspective.registerViews(register);
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_STATUS, 1, 1, mmManager.isMultiMonitorSystem());
//			mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_SCRIPTING, 1, 1, mmManager.isMultiMonitorSystem());
//			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//			for (IPerspectiveDescriptor perspective: page.getOpenPerspectives()) {
//				if (ID_PERSPECTIVE_STATUS.equals(perspective.getId())){
//					page.setPerspective(perspective);
//				}
//			}
			
//			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
//			for (IWorkbenchWindow window : windows) {
//				window.addPerspectiveListener(listener);
//			}
			
		}
	}

}
