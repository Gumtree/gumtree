package org.gumtree.dae.server.internal;

import org.gumtree.util.ISystemProperty;
import org.gumtree.util.SystemProperty;

public final class SystemProperties {

	public static final ISystemProperty DAE_HOST = new SystemProperty(
			"gumtree.dae.host", "localhost");

	public static final ISystemProperty DAE_PORT = new SystemProperty(
			"gumtree.dae.port", "8081");

	public static final ISystemProperty DAE_LOGIN = new SystemProperty(
			"gumtree.dae.login", "spy");

	public static final ISystemProperty DAE_PASSWORD = new SystemProperty(
			"gumtree.dae.password", "007");

	public static final ISystemProperty DAE_PASSWORD_ENCRYPTED = new SystemProperty(
			"gumtree.dae.passwordEncrypted", "false");

	public static final ISystemProperty DAE_IMAGE_URL_PATH = new SystemProperty(
			"gumtree.dae.imageUrlPath", "/admin/openimageinformat.egi");

	public static final ISystemProperty DAE_RESTLET_CACHE_EXPIRY = new SystemProperty(
			"gumtree.dae.restlet.cacheExpiry", "3000");

	public static final ISystemProperty DAE_RESTLET_CACHE_SIZE = new SystemProperty(
			"gumtree.dae.restlet.cacheSize", "16");

	// Use standard naming
	public static final ISystemProperty HTTP_PROXY_HOST = new SystemProperty(
			"http.proxyHost", "");
	
	public static final ISystemProperty HTTP_PROXY_PORT = new SystemProperty(
			"http.proxyPort", "80");
	
	private SystemProperties() {
		super();
	}
	
}
