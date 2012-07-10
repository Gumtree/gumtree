package org.gumtree.pydev.configurator;

import org.gumtree.util.ISystemProperty;
import org.gumtree.util.SystemProperty;

public class PydevConfiguratorProperties {

	public final static ISystemProperty PYDEV_INCLUDE_BUNDLES = new SystemProperty(
			"gumtree.pydev.includeBundles", "");

	private PydevConfiguratorProperties() {
		super();
	}
	
}
