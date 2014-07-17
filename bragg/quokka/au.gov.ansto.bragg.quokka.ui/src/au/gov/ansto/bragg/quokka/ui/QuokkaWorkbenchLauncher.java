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
import org.eclipse.swt.widgets.Display;
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

	private static Logger logger = LoggerFactory.getLogger(QuokkaWorkbenchLauncher.class);
	
//	private static boolean isCoolBarVisable = true;
	
	public QuokkaWorkbenchLauncher() {
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
		}
	}

}
