package org.gumtree.jython.core;

import org.gumtree.util.ISystemProperty;
import org.gumtree.util.SystemProperty;

public final class JythonCoreProperties {

	public final static ISystemProperty JYTHON_EXCLUDE_BUNDLES = new SystemProperty(
			"gumtree.scripting.jython.excludeBundles", "");
	
	public final static ISystemProperty JYTHON_INITIALISATION_SCRIPTS = new SystemProperty(
			"gumtree.scripting.jython.initialisationScripts", "");

	private JythonCoreProperties() {
		super();
	}

}
