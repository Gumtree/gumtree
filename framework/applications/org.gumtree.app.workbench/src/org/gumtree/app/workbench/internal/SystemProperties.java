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

package org.gumtree.app.workbench.internal;

import org.gumtree.util.ISystemProperty;
import org.gumtree.util.SystemProperty;

public final class SystemProperties {

	/**
	 * The class URI for the panel 
	 */
	public static final ISystemProperty SIDEBAR_PART_URI = new SystemProperty(
			"gumtree.sidebar.partUri", "bundleclass://org.gumtree.app.workbench/org.gumtree.app.workbench.support.SidebarWidget");
			
	/**
	 * Create sidebar on the workbench
	 */
	public static final ISystemProperty CREATE_SIDEBAR = new SystemProperty(
			"gumtree.workbench.createSidebar", "false");

	/**
	 * Show sidebar on the workbench
	 */
	public static final ISystemProperty SHOW_SIDEBAR_ON_STARTUP = new SystemProperty(
			"gumtree.workbench.showSidebarOnStartUp", "false");

	/**
	 * Fixed width of the sidebar
	 */
	public static final ISystemProperty SIDEBAR_WIDTH = new SystemProperty(
			"gumtree.workbench.sideBarWidth", "150");

	/**
	 * Fixed width of the sidebar
	 */
	public static final ISystemProperty SIDEBAR_PERSPECTIVE_WIDTH = new SystemProperty(
			"gumtree.workbench.sideBar.perspectiveWidth", "60");
	
	
	/**
	 * A comma separated list of ordered perspectives for display 
	 */
	public static final ISystemProperty SIDEBAR_PERSPECTIVE_ORDER = new SystemProperty(
			"gumtree.workbench.sideBar.perspectiveOrder", "");

	/**
	 * Restore workbench layout or not
	 */
	public static final ISystemProperty RESTORE_WORKBENCH = new SystemProperty(
			"gumtree.workbench.restoreWorkbench", "false");

	private SystemProperties() {
		super();
	}
	
}
