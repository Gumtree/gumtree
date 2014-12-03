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

import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.gumtree.gumnix.sics.control.ISicsController;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.internal.ui.editors.SicsEditorInput;
import org.gumtree.gumnix.sics.ui.SicsUIConstants;
import org.gumtree.gumnix.sics.ui.SicsUIProperties;
import org.gumtree.gumnix.sics.ui.controlview.INodeSet;
import org.gumtree.gumnix.sics.ui.controlview.NodeSet;
import org.gumtree.ui.service.launcher.AbstractLauncher;
import org.gumtree.ui.service.launcher.LauncherException;
import org.gumtree.ui.service.multimonitor.IMultiMonitorManager;
import org.gumtree.ui.service.multimonitor.support.MultiMonitorManager;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;
import org.gumtree.util.string.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuokkaWorkbenchLauncher extends AbstractLauncher {

	
	private static final String ID_PERSPECTIVE_EXPERIMENT = "au.gov.ansto.bragg.quokka.ui.scanPerspective";
	private static final String ID_PERSPECTIVE_SCRIPTING = "au.gov.ansto.bragg.nbi.ui.scripting.ScriptingPerspective";
	private static final String ID_PERSPECTIVE_DEFAULT = "au.gov.ansto.bragg.nbi.ui.EmptyPerspective";
	private static final String ID_PERSPECTIVE_SICS = "au.gov.ansto.bragg.nbi.ui.SICSExperimentPerspective";
//	private static final String[] IGNORE_PERSPECTIVES = new String[] {
//         "au.gov.ansto.bragg.quokka.ui.scanPerspective", 
//         "au.gov.ansto.bragg.kakadu.ui.KakaduPerspective",
//         "org.eclipse.dltk.tcl.ui.TclPerspective", 
//         "org.eclipse.dltk.tcl.TclBrowsingPerspective",
//         "au.gov.ansto.bragg.quokka.ui.analysis", 
//         "org.eclipse.mylyn.tasks.ui.perspectives.planning",
//         "au.gov.ansto.bragg.quokka.ui.alignmentPerspective", 
//         "org.python.pydev.ui.PythonPerspective",
//         }; 

	private static Logger logger = LoggerFactory.getLogger(QuokkaWorkbenchLauncher.class);
	
//	private static boolean isCoolBarVisable = true;
	
	public QuokkaWorkbenchLauncher() {
	}

	private void hideMenus(WorkbenchWindow window){
		WorkbenchWindow workbenchWin = (WorkbenchWindow)PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		MenuManager menuManager = ((WorkbenchWindow) window).getMenuManager();
		IContributionItem[] items = menuManager.getItems();

		for(IContributionItem item : items) {
		  item.setVisible(false);
		}
		menuManager.setVisible(false);
	    
//	        IHandlerService service = (IHandlerService) window.getService(IHandlerService.class);
//	        if (service != null)
//				try {
//					service.executeCommand("org.eclipse.ui.ToggleCoolbarAction",
//					        null);
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} 
	    
	    IContributionItem[] menubarItems = ((WorkbenchWindow) window).getMenuBarManager().getItems();
	    for (IContributionItem item : menubarItems) {
	    	item.setVisible(false);
	    }
	    ((WorkbenchWindow) window).getMenuBarManager().setVisible(false);
	    
	    IToolBarManager toolbarManager = ((WorkbenchWindow) window).getToolBarManager2();
	    IContributionItem[] barItems = toolbarManager.getItems();
	    for (IContributionItem item : barItems) {
	    	item.setVisible(false);
	    }
//	    toolbarManager.removeAll();
//	    toolbarManager.update(true);
	    
	    IContributionItem[] coolbarItems = ((WorkbenchWindow) window).getCoolBarManager2().getItems();
	    for (IContributionItem item : coolbarItems) {
//	    	if (!item.getId().equals("org.eclipse.debug.ui.launch.toolbar") && !item.getId().equals("org.eclipse.debug.ui.launchActionSet")
//	    			&& !item.getId().equals("org.eclipse.debug.ui.launch.toolbar")){
//	    		item.setVisible(false);
//	    	}
//	    	if (	false
//	    			|| item.getId().equals("group.file") 
//	    			|| item.getId().equals("org.eclipse.ui.workbench.file")
//	    			|| item.getId().equals("additions") 
//	    			|| item.getId().equals("org.eclipse.debug.ui.main.toolbar") 
//	    			|| item.getId().equals("org.eclipse.wst.xml.ui.design.DesignToolBar") 
//	    			|| item.getId().equals("org.eclipse.wst.xml.ui.perspective.NewFileToolBar")
//	    			|| item.getId().equals("org.eclipse.jdt.debug.ui.JavaSnippetToolbarActions")
//	    			|| item.getId().equals("org.eclipse.debug.ui.launchActionSet")
//	    			|| item.getId().equals("org.eclipse.dltk.tcl.ui.debug.consoleActionSet")
//	    			|| item.getId().equals("org.eclipse.search.searchActionSet")
//	    			|| item.getId().equals("group.nav")
//	    			|| item.getId().equals("org.eclipse.ui.workbench.navigate")
//	    			|| item.getId().equals("group.editor")
//	    			|| item.getId().equals("group.help")
//	    			|| item.getId().equals("org.eclipse.ui.workbench.help")
//	    			|| item.getId().equals("org.eclipse.mylyn.tasks.ui.trim.container")
//	    			) {
	    		item.setVisible(false);
//	    	}
	    }	
	    
//	    removeUnWantedPerspectives();
	}

	/**
	 * Removes the unwanted perspectives from your RCP application
	 */
//	private void removeUnWantedPerspectives() {
//		IPerspectiveRegistry perspectiveRegistry = PlatformUI.getWorkbench().getPerspectiveRegistry();
//		IPerspectiveDescriptor[] perspectiveDescriptors = perspectiveRegistry.getPerspectives();
//		for (IPerspectiveDescriptor des : perspectiveDescriptors) {
//			System.err.println(des);
//		}
//		List<String> ignoredPerspectives = Arrays.asList(IGNORE_PERSPECTIVES);
//		List<IPerspectiveDescriptor> removePerspectiveDesc = new ArrayList<IPerspectiveDescriptor>();
//
//		// Add the perspective descriptors with the matching perspective ids to the list
//		for (IPerspectiveDescriptor perspectiveDescriptor : perspectiveDescriptors) {
//			if(ignoredPerspectives.contains(perspectiveDescriptor.getId())) {
//				removePerspectiveDesc.add(perspectiveDescriptor);
//			}
//		}
//
//		for (IPerspectiveDescriptor des : removePerspectiveDesc) {
//			perspectiveRegistry.deletePerspective(des);
//		}
//		// If the list is non-empty then remove all such perspectives from the IExtensionChangeHandler
////        if(perspectiveRegistry instanceof IExtensionChangeHandler && !removePerspectiveDesc.isEmpty()) {
////            IExtensionChangeHandler extChgHandler = (IExtensionChangeHandler) perspectiveRegistry;
////            extChgHandler.removeExtension(null, removePerspectiveDesc.toArray());
////        }
//	}

	public void launch() throws LauncherException {
		{			
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
			
			final IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			for (IWorkbenchWindow window : windows){
				if (window != null && window != activeWorkbenchWindow) {
					window.close();
				}
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
			
			
			Thread thread = new Thread(new Runnable(){

				@Override
				public void run() {
					LoopRunner.run(new ILoopExitCondition() {

						@Override
						public boolean getExitCondition() {
							if (SicsCore.getDefaultProxy() != null && SicsCore.getDefaultProxy().isConnected() && SicsCore.getSicsController() != null){
								try {
									Thread.sleep(12000);
								} catch (InterruptedException e) {
								}
								ISicsController sicsController = SicsCore.getSicsController();
								String filterPath = SicsUIProperties.FILTER_PATH.getValue();
								if (!StringUtils.isEmpty(filterPath)) {
									try {
										IFileStore filterFolder = EFS.getStore(new URI(filterPath));
										IFileStore child = filterFolder.getChild("CommissioningFilter.xml");
										INodeSet nodeSet = NodeSet.read(child.openInputStream(EFS.NONE, new NullProgressMonitor()));
										final SicsEditorInput input = new SicsEditorInput((ISicsController)sicsController, nodeSet);
										IWorkbenchWindow[] existingWindows = PlatformUI.getWorkbench().getWorkbenchWindows();
										if (existingWindows.length > 0) {
											IWorkbenchPage[] pages = existingWindows[existingWindows.length - 1].getPages();
											if (pages.length > 0) {
												final IWorkbenchPage page = pages[0];
												Display.getDefault().asyncExec(new Runnable() {

													@Override
													public void run() {
														try {
															page.openEditor(input, SicsUIConstants.ID_EDITOR_SICS_CONTROL);
														} catch (PartInitException e) {
															// TODO Auto-generated catch block
															e.printStackTrace();
														}
													}
												});
											}
										}
										
									}catch (Exception e){
										e.printStackTrace();
									}
								}
								return true;
							}
							return false;
						}
					}, -1, 1000);
				}

			});
			thread.start();
			
			hideMenus((WorkbenchWindow) activeWorkbenchWindow);
		}
	}

	
}
