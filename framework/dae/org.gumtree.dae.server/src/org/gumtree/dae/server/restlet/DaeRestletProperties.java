package org.gumtree.dae.server.restlet;

import org.gumtree.util.ISystemProperty;
import org.gumtree.util.SystemProperty;

public final class DaeRestletProperties {

	public static final ISystemProperty CACHE_EXPIRY = new SystemProperty("gumtree.dae.restletCacheExpiry", "3000");

	public static final ISystemProperty CACHE_SIZE = new SystemProperty("gumtree.dae.restletCacheSize", "16");
	
	private DaeRestletProperties() {
		super();
	}
	
}
