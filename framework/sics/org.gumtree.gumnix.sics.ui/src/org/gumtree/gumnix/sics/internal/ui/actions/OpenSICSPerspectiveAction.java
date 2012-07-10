/******************************************************************************* 
 * Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 *******************************************************************************/
package org.gumtree.gumnix.sics.internal.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.WorkbenchException;
import org.gumtree.gumnix.sics.internal.ui.Activator;
import org.gumtree.gumnix.sics.ui.SicsUIConstants;

/**
 * @author nxi
 * Created on 06/05/2008
 */
public class OpenSICSPerspectiveAction implements
IWorkbenchWindowActionDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction arg0) {
		// TODO Auto-generated method stub
		openSICSPersective();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction arg0, ISelection arg1) {
		// TODO Auto-generated method stub

	}

	public static void openSICSPersective() {
//		final IWorkbench workbench = PlatformUI.getWorkbench();
		final IWorkbenchWindow workbenchWindow = Activator.getDefault().
		getWorkbench().getActiveWorkbenchWindow();
		final IWorkbench workbench = workbenchWindow.getWorkbench();
		final IWorkbenchPage activePage = workbenchWindow.getActivePage();

		final IPerspectiveDescriptor sicsPerspectiveDescriptor = workbench
		.getPerspectiveRegistry().findPerspectiveWithId(
				SicsUIConstants.ID_SICS_PERSPECTIVE);
		if (sicsPerspectiveDescriptor == null) {
			MessageDialog
			.openError(
					workbenchWindow.getShell(),
					"Error opening SICS perspective",
					"SICS perspective did not installed properly. " +
			"Try to reinstall SICS plug-in.");
			return;
		}

		if (workbenchWindow != null && activePage != null) {
			activePage.getWorkbenchWindow().getWorkbench().
			getDisplay().syncExec(new Runnable(){

				public void run() {
					// TODO Auto-generated method stub
					activePage.setPerspective(
							sicsPerspectiveDescriptor);

//					final IViewReference viewReference = activePage.findViewReference(KakaduPerspective.DATA_SOURCE_VIEW_ID);
//					try {
//					activePage.hideView(viewReference);
//					activePage.showView(KakaduPerspective.DATA_SOURCE_VIEW_ID);
//					} catch (Exception e) {
//					showErrorMessage(e.getMessage());
//					}
				}

			});
		} else {
			try {
				workbench.openWorkbenchWindow(
						SicsUIConstants.ID_SICS_PERSPECTIVE,
						activePage.getInput());
			} catch (WorkbenchException e) {
				e.printStackTrace();
				MessageDialog.openError(workbenchWindow.getShell(),
						"Error opening SICS perspective", e.getMessage());
			}
		}
	}
}
