/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package au.gov.ansto.bragg.quokka.ui;

public final class QuokkaUIConstants {

	/**
	 * Unique ID for Quokka scan view.
	 */
	public static final String ID_VIEW_QUOKKA_SCAN = "au.gov.ansto.bragg.quokka.ui.scanView";
	
	public static final String ID_VIEW_QUOKKA_MSW = "au.gov.ansto.bragg.quokka.msw.views.QuokkaMswView";
	
	/**
	 * Unique ID for Quokka scan perspective.
	 */
	public static final String ID_PERSPECTIVE_QUOKKA_SCAN = "au.gov.ansto.bragg.quokka.ui.scanPerspective";
	
	/**
	 * Property ID for plugin that contains the scan config.
	 */
	public static final String PROP_SCAN_CONFIG_PLUGIN = "gumtree.quokka.scan.config.plugin";
	
	/**
	 * Property ID for scan config location.
	 */
	public static final String PROP_SCAN_CONFIG_PATH = "gumtree.quokka.scan.config.path";
	
	/**
	 * Path for user defined templates.
	 */
	public static final String PATH_TEMPLATES = "/templates";
	
	private QuokkaUIConstants() {
		super();
	}
	
}
