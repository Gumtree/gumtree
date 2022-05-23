package org.gumtree.control.test;

public class ConstantSetup {

	public static boolean USE_LOCAL_SERVER = true;
//	public static String REMOTE_SERVER_ADDRESS = "tcp://ics1-bilby-test.nbi.ansto.gov.au:5555";
//	public static String REMOTE_PUBLISHER_ADDRESS = "tcp://ics1-bilby-test.nbi.ansto.gov.au:5556";
	public static String REMOTE_SERVER_ADDRESS = "tcp://137.157.204.8:5555";
	public static String REMOTE_PUBLISHER_ADDRESS = "tcp://137.157.204.8:5566";
	
	public static String LOCAL_SERVER_ADDRESS = "tcp://localhost:5555";
	public static String LOCAL_PUBLISHER_ADDRESS = "tcp://localhost:5566";
	
	public static String VALIDATOR_SERVER_ADDRESS = "tcp://localhost:5577";
	public static String VALIDATOR_PUBLISHER_ADDRESS = "tcp://localhost:5588";
	
	
//	public static enum SERVER_STATUS {IDLE, DRIVING, COUNTING, PAUSED}

}
