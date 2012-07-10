package org.gumtree.gumnix.sics.ui;

import org.gumtree.util.ISystemProperty;
import org.gumtree.util.SystemProperty;

public final class SicsUIProperties {

	// TODO: supply default image
	public static final ISystemProperty INSTRUMENT_LOGIN_IMAGE = new SystemProperty(
			"gumtree.sics.loginImage", "");
	
	public static final ISystemProperty FILTER_PATH = new SystemProperty(
			"gumtree.sics.filterPath", "");
		
	private SicsUIProperties() {
		super();
	}
	
}
