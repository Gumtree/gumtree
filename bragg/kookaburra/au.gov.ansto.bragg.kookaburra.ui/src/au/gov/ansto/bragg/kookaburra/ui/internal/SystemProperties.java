package au.gov.ansto.bragg.kookaburra.ui.internal;

import org.gumtree.util.ISystemProperty;
import org.gumtree.util.SystemProperty;

public final class SystemProperties {

	/**
	 * Auto export report location
	 */
	public static final ISystemProperty REPORT_LOCATION = new SystemProperty(
			"kookaburra.scan.report.location", "");

	/**
	 * Export flag
	 */
	public static final ISystemProperty AUTO_EXPORT = new SystemProperty(
			"kookaburra.scan.report.autoExport", "true");

	/**
	 * Workspace folder for storing workspace instrument configuration
	 */
	public static final ISystemProperty CONFIG_FOLDER = new SystemProperty(
			"gumtree.kookaburra.workflow.configFolder", "/Kookaburra/Instrument_Config");

	private SystemProperties() {
		super();
	}
	
}
