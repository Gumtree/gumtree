package org.gumtree.cs.sics.core.tests;

import org.gumtree.sics.io.ISicsConnectionContext;
import org.gumtree.sics.io.SicsConnectionContext;
import org.gumtree.sics.io.SicsRole;
import org.gumtree.util.eclipse.OsgiUtils;

public class SicsTestUtils {

//	private static final String MODEL_FILE = "data/wombat20070403.hipadaba";
//
//	private static final String INSTRUMENT_NAME = GTPlatform.getCommandLineOptions().getOptionValue(OPTION_INSTRUMENT_NAME);
//
//	private static SICS model;
//
	
	private static ISicsConnectionContext context;
//
//	static {
//		try {
//			URI fileURI = GTPlatform.find(SicsCoreTests.PLUGIN_ID, MODEL_FILE).toURI();
//			model = SicsUtils.loadSICSModel(fileURI);
//		} catch (Exception e) {
//			Assert.fail("Cannot load data file.");
//		}
//	}
//
//	// Always reload a fresh model from file
//	public static SICS getDefaultOfflineSicsModel() {
//		return model;
//	}

	public static final String PLUGIN_ID = "org.gumtree.sics.core.tests";
	
	public static ISicsConnectionContext createConnectionContext() {
		SicsConnectionContext context = new SicsConnectionContext("localhost",
				60002, SicsRole.USER, "sydney");
		return context;
	}
	
//	public static ISicsConnectionContext getConnectionContext() {
//		if(context == null) {
//			context = new SicsConnectionContext(
//					System.getProperty("sics.host.test"),
//					Integer.parseInt(System.getProperty("sics.port.test")),
//					SicsRole.getRole(SystemProperties.SICS_ROLE.getValue()),
//					SystemProperties.SICS_PASSWORD.getValue());
//			Assert.assertNotNull(context.getHost());
//			Assert.assertNotNull(context.getPort());
//			Assert.assertNotNull(context.getRole());
//			Assert.assertNotNull(context.getPassword());
//		}
//		return context;
//	}

//	public static String getInstrumentName() {
//		return INSTRUMENT_NAME;
//	}

//	public static String getInstrumentSpecificPath(String relativePath) {
//		Assert.assertNotNull(relativePath);
//		return "/" + getInstrumentName() + relativePath;
//	}
	
	public static void startEventAdmin() throws Exception {
		OsgiUtils.startBundle("org.eclipse.equinox.event");
	}

	private SicsTestUtils() {
		super();
	}

}
