/*******************************************************************************
 * Copyright (c) 2010 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 ******************************************************************************/
package org.gumtree.data.nexus.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.data.nexus.ui.viewers.NexusViewer;

/**
 * @author nxi
 *
 */
public class NexusBrowserView extends ViewPart {

	private NexusViewer viewer;
	public final static String VIEW_ID = "org.gumtree.data.nexus.ui.NexusBrowserView";
	private static NexusBrowserView instance;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		viewer = new NexusViewer(parent, SWT.NONE);
//		setBrowser((NexusBrowser) viewer.getDatasetBrowser());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	/**
	 * @return the browser
	 */
	public NexusViewer getNexusViewer() {
		return viewer;
	}

	public static NexusBrowserView getInstance() {
		if (instance != null && !instance.getNexusViewer().isDisposed()) {
			return instance;
		}
		// Reset instance
		instance = null;
		Display.getDefault().asyncExec(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try{
					IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					final IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
					
					instance = (NexusBrowserView) workbenchPage.showView(
								VIEW_ID, "id", IWorkbenchPage.VIEW_ACTIVATE);
				}catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}});
		int sleepTime = 0;
		while((instance == null || instance.getNexusViewer() == null) && sleepTime < 5000){
			try {
				Thread.sleep(200);
				sleepTime += 200;
			} catch (Exception e) {
				throw new NullPointerException("The view is not available");
			}
		}
		return instance;
		
	}
	

}
