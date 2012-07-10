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

package org.gumtree.ui.dashboard.app;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.gumtree.ui.dashboard.model.Dashboard;
import org.gumtree.ui.dashboard.model.DashboardModelUtils;
import org.gumtree.ui.dashboard.viewer.DashboardViewer;
import org.gumtree.ui.dashboard.viewer.IDashboardViewer;

public class DashboardWindow extends ApplicationWindow {

//	private String dashboardFile;
	
	private IDashboardViewer viewer;
	
	public DashboardWindow() {
		super(null);
	}

	public DashboardWindow(String dashboardFile) {
		super(null);
//		this.dashboardFile = dashboardFile;
	}
	
	/**
	 * Runs the application
	 */
	public void run() {
		// Don't return from open() until window closes
		setBlockOnOpen(true);

		// Add menu bar
		addMenuBar();
		
		// Open the main window
		open();

		// Dispose the display
		Display.getCurrent().dispose();
	}

	protected Control createContents(Composite parent) {
		// Create a Hello, World label
		viewer = new DashboardViewer(parent, SWT.NONE);
		return (Control) viewer;
	}

	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager();
		
		IMenuManager fileMenu = new MenuManager("&File");
		fileMenu.add(new Action("&Open") {
			public void run() {
				FileDialog dialog = new FileDialog(getShell());
				String filename = dialog.open();
				if (filename != null) {
					openDashboard(filename);
				}
			}
		});
		fileMenu.add(new Action("E&xit") {
			public void run() {
				DashboardWindow.this.close();
			}
		});
		menuManager.add(fileMenu);
		
		return menuManager;
	}
    
	public boolean close() {
		viewer = null;
		return super.close();
	}
	
	private void openDashboard(String filename) {
		try {
			Dashboard model = DashboardModelUtils.loadModel(new FileInputStream(filename));
			viewer.setModel(model);
			viewer.afterParametersSet();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		if (args.length == 1) {
			// Pass dashboard file
			new DashboardWindow(args[0]).run();
		} else {
			// Start with no default dashboard 
			new DashboardWindow().run();	
		}
	}

}
