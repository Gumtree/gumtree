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
import org.eclipse.ui.part.ViewPart;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.ui.dashboard.model.DashboardModelUtils;
import org.gumtree.ui.dashboard.viewer.DashboardViewer;
import org.gumtree.ui.dashboard.viewer.IDashboardViewer;

public class WombatDaeView extends ViewPart {

	public WombatDaeView() {
	}

	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		IDashboardViewer dashboardViewer = new DashboardViewer(parent, SWT.NONE);
		IDataAccessManager dam = ServiceUtils.getService(IDataAccessManager.class);
		InputStream in = dam.get(URI.create("bundle://au.gov.ansto.bragg.wombat/dashboards/DAE.xml"), InputStream.class);
		dashboardViewer.setModel(DashboardModelUtils.loadModel(in));
	}

	public void setFocus() {
	}

}
