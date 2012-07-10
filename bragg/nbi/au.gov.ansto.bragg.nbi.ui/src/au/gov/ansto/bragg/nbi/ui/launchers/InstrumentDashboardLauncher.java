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

package au.gov.ansto.bragg.nbi.ui.launchers;

import java.io.InputStream;
import java.net.URI;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.ui.dashboard.model.Dashboard;
import org.gumtree.ui.dashboard.model.DashboardModelUtils;
import org.gumtree.ui.dashboard.viewer.DashboardViewer;
import org.gumtree.ui.dashboard.viewer.IDashboardViewer;
import org.gumtree.ui.service.launcher.AbstractLauncher;
import org.gumtree.ui.service.launcher.LauncherException;
import org.gumtree.ui.util.workbench.ContentViewUtils;
import org.gumtree.ui.util.workbench.IViewContentContributor;

import au.gov.ansto.bragg.nbi.ui.internal.InternalImage;

public class InstrumentDashboardLauncher extends AbstractLauncher {

	private static final String PROP_INSTRUMENT_DASHBOARD = "instrument.dashboard";
	
	public InstrumentDashboardLauncher() {
	}

	public void launch() throws LauncherException {
		try {
			final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			ContentViewUtils.createContentPerspective(new IViewContentContributor() {
				public Image getTitleImage() {
					return InternalImage.DASHBOARD_16.getImage();
				}
				public String getTitle() {
					return "Dashboard";
				}
				public void dispose() {
				}
				public void createContentControl(Composite parent) {
					parent.setLayout(new FillLayout());
					// Create dashboard
					IDashboardViewer viewer = new DashboardViewer(parent, SWT.NONE);
					viewer.setModel(getModel());
					viewer.afterParametersSet();
				}
			}, window);
		} catch (WorkbenchException e) {
			throw new LauncherException("Failed to launch dashboard in content perpsective", e);
		}
	}

	public void launch(int windowId) throws LauncherException {
		try {
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			if (windowId >= windows.length)
				throw new LauncherException("window " + windowId + " is not available.");
			final IWorkbenchWindow window = windows[windowId];
			ContentViewUtils.createContentPerspective(new IViewContentContributor() {
				public Image getTitleImage() {
					return InternalImage.DASHBOARD_16.getImage();
				}
				public String getTitle() {
					return "Dashboard";
				}
				public void dispose() {
				}
				public void createContentControl(Composite parent) {
					parent.setLayout(new FillLayout());
					// Create dashboard
					IDashboardViewer viewer = new DashboardViewer(parent, SWT.NONE);
					viewer.setModel(getModel());
					viewer.afterParametersSet();
					// Hide side bar
//					IExtendedWorkbenchWindow extendedWindow = (IExtendedWorkbenchWindow) window
//							.getService(IExtendedWorkbenchWindow.class);
//					if (!extendedWindow.isSideBarHidden()) {
//						extendedWindow.hideSideBar();
//					}
				}
			}, window);
		} catch (WorkbenchException e) {
			throw new LauncherException("Failed to launch dashboard in content perpsective", e);
		}
	}
	
	private static final Dashboard getModel() {
		String dashboardConfig = System.getProperty(PROP_INSTRUMENT_DASHBOARD);
		if (dashboardConfig != null) {
			IDataAccessManager dam = ServiceUtils.getService(IDataAccessManager.class);
			return DashboardModelUtils.loadModel(dam.get(URI.create(dashboardConfig), InputStream.class));
		} else {
			return DashboardModelUtils.createEmptyDashboard();
		}
	}
	
}
