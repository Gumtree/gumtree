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
package org.gumtree.data.ui.part;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.data.ui.viewers.DatasetBrowser;
import org.gumtree.data.ui.viewers.DatasetViewer;

/**
 * @author nxi
 *
 */
public class DatasetBrowserView extends ViewPart {

	private DatasetBrowser browser;
	public final static String VIEW_ID = "org.gumtree.data.ui.DatasetBrowserView";
	private static DatasetBrowserView instance;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		DatasetViewer viewer = new DatasetViewer(parent, SWT.NONE);
		setBrowser(viewer.getDatasetBrowser());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	/**
	 * @param browser the browser to set
	 */
	protected void setBrowser(DatasetBrowser browser) {
		this.browser = browser;
	}

	/**
	 * @return the browser
	 */
	public DatasetBrowser getBrowser() {
		return browser;
	}

	public static DatasetBrowserView getInstance() {
		Display.getDefault().asyncExec(new Runnable(){

			public void run() {
				// TODO Auto-generated method stub
				try{
					IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					final IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();

					
					instance = (DatasetBrowserView) workbenchPage.showView(
								VIEW_ID, "id", IWorkbenchPage.VIEW_VISIBLE);
				}catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}});
		int sleepTime = 0;
		while((instance == null || instance.getBrowser() == null) && sleepTime < 2000){
			try {
				Thread.currentThread().sleep(200);
				sleepTime += 200;
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return instance;
		
	}
}
