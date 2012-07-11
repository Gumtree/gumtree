package au.gov.ansto.bragg.quokka.core.internal;

import org.gumtree.util.ISystemProperty;
import org.gumtree.util.SystemProperty;

public final class QuokkaCoreProperties {

	public static final ISystemProperty SICS_SIMULATION_MODE = new SystemProperty(
			"gumtree.quokka.sicsSimulationMode", "false");
	
	/**
	 * Expected monitor rate for workflow estimation calculation
	 */
	public static final ISystemProperty EXPECTED_MONITOR_RATE = new SystemProperty(
			"quokka.scan.expectedMonitorRate", "48000");

	/**
	 * Auto export report location
	 */
	public static final ISystemProperty REPORT_LOCATION = new SystemProperty(
			"quokka.scan.report.location", "");
	
	private QuokkaCoreProperties() {
		super();
	}
	
}
