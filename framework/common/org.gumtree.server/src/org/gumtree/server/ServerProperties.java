package org.gumtree.server;

import org.gumtree.util.ISystemProperty;
import org.gumtree.util.SystemProperty;

public final class ServerProperties {

	public static final ISystemProperty SERVER_ENABLE = new SystemProperty(
			"gumtree.server.enable", "true");
	
	public static final ISystemProperty SERVER_PORT = new SystemProperty(
			"gumtree.server.port", "true");
	
}
