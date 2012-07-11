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

package au.gov.ansto.bragg.quokka.ui.internal;

import org.gumtree.util.ISystemProperty;
import org.gumtree.util.SystemProperty;

public final class SystemProperties {

	/**
	 * Auto export report location
	 */
	public static final ISystemProperty REPORT_LOCATION = new SystemProperty(
			"quokka.scan.report.location", "");

	/**
	 * Export flag
	 */
	public static final ISystemProperty AUTO_EXPORT = new SystemProperty(
			"quokka.scan.report.autoExport", "true");

	/**
	 * Workspace folder for storing workspace instrument configuration
	 */
	public static final ISystemProperty CONFIG_FOLDER = new SystemProperty(
			"gumtree.quokka.workflow.configFolder", "/Quokka/Instrument_Config");

	private SystemProperties() {
		super();
	}

}
