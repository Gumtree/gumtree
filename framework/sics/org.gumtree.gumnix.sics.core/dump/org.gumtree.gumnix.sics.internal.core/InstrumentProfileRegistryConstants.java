package org.gumtree.gumnix.sics.internal.core;


public class InstrumentProfileRegistryConstants {

	public static String EXTENSION_INSTRUMENT_PROFILES = "instrumentProfiles";

	public static String ELEMENT_PROFILE = "profile";

	public static String ATTRIBUTE_IMAGE = "image";
	
	public static String ELEMENT_PROPERTY = "property";
	
	public static String ATTRIBUTE_NAME = "name";
	
	public static String ATTRIBUTE_VALUE = "value";
	
	public static String EXTENTION_POINT_INSTRUMENTS = Activator.PLUGIN_ID
			+ "." + EXTENSION_INSTRUMENT_PROFILES;

	private InstrumentProfileRegistryConstants() {
		super();
	}
	
}
