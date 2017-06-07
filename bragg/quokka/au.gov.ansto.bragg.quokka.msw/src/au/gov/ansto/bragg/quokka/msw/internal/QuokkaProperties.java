package au.gov.ansto.bragg.quokka.msw.internal;

import java.util.HashMap;
import java.util.Map;

public final class QuokkaProperties {
	// construction
	private QuokkaProperties() {
	}
	
	// properties
	public static boolean checkTertiaryShutter() {
		String property;
		
		property = System.getProperty("quokka.msw.checkTertiaryShutter");
		if (property != null)
			return Boolean.parseBoolean(property);
		
		property = System.getProperty("quokka.scan.checkTertiaryShutter");
		if (property != null)
			return Boolean.parseBoolean(property);
		
		return true;
	}
	public static String getReportLocation() {
		String property;
		
		property = System.getProperty("quokka.msw.reportLocation");
		if (property != null) {
			return property;
		}

		property = System.getProperty("quokka.scan.report.location");
		if (property != null) {
			final String file = "file://";
    		if (property.startsWith(file))
    			return property.substring(file.length());
    		else
    			return property;
		}
		
		property = System.getProperty("user.home");
		if (property != null) {
			return property + "/Desktop";
		}

		return null;
	}
	public static Long getExpectedMonitorRate() {
		return Long.getLong(
				"quokka.msw.expectedMonitorRate",
				Long.getLong("quokka.scan.expectedMonitorRate", 48000));
	}
	public static int getMaxConfigurationImport() {
		return Integer.getInteger("quokka.msw.maxConfigurationImport", 32);
	}
	public static Map<String, String> getEnvironmentTemplates() {
		String property = System.getProperty(
				"quokka.msw.environmentTemplates",
				"Dummy Motor:sics.drive(\"dummy_motor\", value);" +
				"High Voltage:sics.drive(\"highvoltage\", value);" +
				"ips120:sics.drive(\"/sample/ips120/setpoint\", value);" +
				"magnet:sics.drive(\"/sample/ma1/magnet/setpoint\", value);" +
				"tc1:sics.drive(\"/sample/tc1/setpoint\", value);" +
				"tc1 loop1:sics.drive(\"/sample/tc1/Loop1/setpoint\", value);" +
				"tc3:sics.drive(\"/sample/tc3/sensor/setpoint1\", value);" +
				"watlow_rm:sics.drive(\"/sample/watlow_rm/setpoint\", value);" +
				"xcs:sics.drive(\"/sample/xcs/humidity\", value)");

		String[] environments = property.split(";");
		Map<String, String> map = new HashMap<>(environments.length);
		for (String environment : environments) {
			int index = environment.indexOf(':');
			if (index > 0)
				map.put(environment.substring(0, index),
						environment.substring(index + 1));
		}
		return map;
	}
}
