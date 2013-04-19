package au.gov.ansto.bragg.kookaburra.core.internal;

import org.gumtree.util.ISystemProperty;
import org.gumtree.util.SystemProperty;

public final class KookaburraCoreProperties {

	public static final ISystemProperty SICS_SIMULATION_MODE = new SystemProperty(
			"gumtree.kookaburra.sicsSimulationMode", "false");
	
	/**
	 * Expected monitor rate for workflow estimation calculation
	 */
	public static final ISystemProperty EXPECTED_MONITOR_RATE = new SystemProperty(
			"kookaburra.scan.expectedMonitorRate", "48000");

	/**
	 * Auto export report location
	 */
	public static final ISystemProperty REPORT_LOCATION = new SystemProperty(
			"kookaburra.scan.report.location", "");
	
	private KookaburraCoreProperties() {
		super();
	}
	
}
