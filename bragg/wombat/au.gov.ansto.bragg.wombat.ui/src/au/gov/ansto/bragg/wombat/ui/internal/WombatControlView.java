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

package au.gov.ansto.bragg.wombat.ui.internal;

import java.io.InputStream;
import java.net.URI;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.gumnix.sics.internal.ui.controlview.ControlViewer;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.ui.dashboard.model.DashboardModelUtils;
import org.gumtree.ui.dashboard.viewer.DashboardViewer;
import org.gumtree.ui.dashboard.viewer.IDashboardViewer;

public class WombatControlView extends ViewPart {

	public WombatControlView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
		
		TabItem userMotorsTab = new TabItem(tabFolder, SWT.NONE);
		userMotorsTab.setText("User Motors");
		IDashboardViewer dashboardViewer = new DashboardViewer(tabFolder, SWT.NONE);
		
		IDataAccessManager dam = ServiceUtils.getService(IDataAccessManager.class);
		InputStream in = dam.get(URI.create("bundle://au.gov.ansto.bragg.wombat/dashboards/UserMotor.xml"), InputStream.class);
		dashboardViewer.setModel(DashboardModelUtils.loadModel(in));
		userMotorsTab.setControl((DashboardViewer) dashboardViewer);
		
		TabItem allMotorsTab = new TabItem(tabFolder, SWT.NONE);
		allMotorsTab.setText("All Motors");
		ControlViewer viewer = new ControlViewer();
		viewer.createPartControl(tabFolder, null);
		allMotorsTab.setControl(viewer.getTreeViewer().getControl());
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
