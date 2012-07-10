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

package org.gumtree.gumnix.sics.internal.ui;

/**
 * Collection of string contants used by UI.
 * <p>
 * This class is not intended to be implemented by clients.
 * <p>
 *
 * @since 1.0
 */
public final class WorkbenchUIConstants {
	/**
	 * Unique identifer for the resource perspective.
	 */
	public static final String ID_PERSPECTIVE_WORKBENCH = "org.gumtree.ui.isee.workbenchPerspective";

	/**
	 * Unique identifer for the project exlporer view.
	 */
	public static final String ID_VIEW_PROJECT_EXPLORER = "org.eclipse.ui.navigator.ProjectExplorer";

	/**
	 * Unique identifer for the remote system view.
	 */
	public static final String ID_VIEW_REMOTE_SYSTEM = "org.eclipse.rse.ui.view.systemView";
	
	/**
	 * Unique identifer for the workbench action shortcut view.
	 */
//	public static final String ID_VIEW_ACTION_SHORTCUT = "org.gumtree.ui.isee.shortcutView";

	/**
	 * Unique identifer for the workbench explorer view.
	 */
//	public static final String ID_VIEW_WORKBENCH_EXPLORER = "org.gumtree.ui.isee.workbenchExplorer";

	/**
	 * Unique identifer for the browser view.
	 */
	public static final String ID_VIEW_BROWSER = "org.gumtree.ui.isee.browserView";

	/**
	 * Unique identifer for the file system shortcut view.
	 */
	public static final String ID_VIEW_FILE_SYSTEM_SHORTCUT = "org.gumtree.ui.isee.fileSystemShortcutsView";

	/**
	 * Unique identifer for the dash board view.
	 */
	public static final String ID_VIEW_DASH_BOARD = "org.gumtree.ui.isee.dashBaordView";

	/**
	 * Private constructor to block instance creation.
	 */
	private WorkbenchUIConstants() {
		super();
	}
}
