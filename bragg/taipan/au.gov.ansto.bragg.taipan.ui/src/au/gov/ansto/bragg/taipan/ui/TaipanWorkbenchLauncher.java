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

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWindowListener;
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

import au.gov.ansto.bragg.taipan.ui.befilter.BFAnalysisPerspective;
import au.gov.ansto.bragg.taipan.ui.befilter.BFLivePerspective;

public class TaipanWorkbenchLauncher extends AbstractLauncher {

	private static final String PROP_INSTRUMENT_MODE = "gumtree.taipan.mode";
	
	private static final String ID_PERSPECTIVE_SCRIPTING = "au.gov.ansto.bragg.nbi.ui.scripting.ScriptingPerspective";
	
	private static final String ID_PERSPECTIVE_EXPERIMENT = "au.gov.ansto.bragg.nbi.ui.SICSExperimentPerspective";
	
	// Use the default as buffer to hold the editor
	private static final String ID_PERSPECTIVE_DEFAULT = "au.gov.ansto.bragg.nbi.ui.EmptyPerspective";

	private static Logger logger = LoggerFactory.getLogger(TaipanWorkbenchLauncher.class);
	
	enum TaipanMode {
		TP, 
		BF
	}
	
	public TaipanWorkbenchLauncher() {
	}

	private void hideMenus(WorkbenchWindow window){
		WorkbenchWindow workbenchWin = (WorkbenchWindow)PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		MenuManager menuManager = ((WorkbenchWindow) window).getMenuManager();
		IContributionItem[] items = menuManager.getItems();

		for(IContributionItem item : items) {
		  item.setVisible(false);
		}
		menuManager.setVisible(false);
		menuManager.setRemoveAllWhenShown(true);
	    
	    IContributionItem[] menubarItems = ((WorkbenchWindow) window).getMenuBarManager().getItems();
	    for (IContributionItem item : menubarItems) {
	    	item.setVisible(false);
	    }
	    ((WorkbenchWindow) window).getMenuBarManager().setVisible(false);
	    ((WorkbenchWindow) window).getMenuBarManager().setRemoveAllWhenShown(true);
	}
	
	public void launch() throws LauncherException {
		{		
			boolean isBfMode = false;
			try {
				String mode = System.getProperty(PROP_INSTRUMENT_MODE);
				if (mode != null && TaipanMode.valueOf(mode) == TaipanMode.BF) {
					isBfMode = true; 
				}
			} catch (Exception e) {
			}
			// TODO: move this logic to experiment UI manager service
			final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			hideMenus((WorkbenchWindow) activeWorkbenchWindow);
			
			if (activeWorkbenchWindow instanceof WorkbenchWindow) {
//				((WorkbenchWindow) activeWorkbenchWindow).setCoolBarVisible(false);
				activeWorkbenchWindow.getActivePage().closeAllPerspectives(true, false);
				activeWorkbenchWindow.addPerspectiveListener(new IPerspectiveListener() {
					
					@Override
					public void perspectiveChanged(IWorkbenchPage page,
							IPerspectiveDescriptor perspective, String changeId) {
						hideMenus((WorkbenchWindow) activeWorkbenchWindow);
					}
					
					@Override
					public void perspectiveActivated(IWorkbenchPage page,
							IPerspectiveDescriptor perspective) {
						hideMenus((WorkbenchWindow) activeWorkbenchWindow);
					}
				});
			}
			
			PlatformUI.getWorkbench().addWindowListener(new IWindowListener() {
				
				@Override
				public void windowOpened(IWorkbenchWindow window) {
					hideMenus((WorkbenchWindow) window);
				}
				
				@Override
				public void windowDeactivated(IWorkbenchWindow window) {
				}
				
				@Override
				public void windowClosed(IWorkbenchWindow window) {
				}
				
				@Override
				public void windowActivated(IWorkbenchWindow window) {
					hideMenus((WorkbenchWindow) window);
				}
			});
			
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
			
			if (isBfMode) {
				if (PlatformUI.getWorkbench().getWorkbenchWindowCount() < 2) {
					// open new window as editor buffer
					mmManager.openWorkbenchWindow(BFAnalysisPerspective.ID_PERSPECTIVE_BFANALYSIS, 1, true);
				} else {
					mmManager.showPerspectiveOnOpenedWindow(BFAnalysisPerspective.ID_PERSPECTIVE_BFANALYSIS, 1, 1, mmManager.isMultiMonitorSystem());
				}
				mmManager.showPerspectiveOnOpenedWindow(BFLivePerspective.ID_PERSPECTIVE_BFLIVE, 1, 1, mmManager.isMultiMonitorSystem());
			} else {
				if (PlatformUI.getWorkbench().getWorkbenchWindowCount() < 2) {
					// open new window as editor buffer
					mmManager.openWorkbenchWindow(ID_PERSPECTIVE_SCRIPTING, 1, true);
				} else {
					mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_SCRIPTING, 1, 1, mmManager.isMultiMonitorSystem());
				}
			}
//			// position it
//			mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_SCRIPTING, 1, 1, mmManager.isMultiMonitorSystem());
//
//			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
//			for (IWorkbenchWindow window : windows) {
//				window.addPerspectiveListener(listener);
//			}
			
		}
	}

}
