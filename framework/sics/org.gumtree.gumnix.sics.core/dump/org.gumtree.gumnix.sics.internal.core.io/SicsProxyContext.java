package org.gumtree.gumnix.sics.internal.core.io;

import java.util.Properties;

import org.gumtree.gumnix.sics.core.io.ISicsProxyContext;

public class SicsProxyContext implements ISicsProxyContext {
	private static String PROPERTY_HOST = "host";

	private static String PROPERTY_PORT = "port";

	private static String PROPERTY_LOGIN = "login";

	private static String PROPERTY_PASSWORD = "password";

	private static String PROPERTY_INTEREST_LOGIN = "interest.login";

	private static String PROPERTY_INTEREST_PASSWORD = "interest.password";

	private Properties properties;

	public SicsProxyContext(Properties properties) {
		this.properties = properties;
	}

	public String getHost() {
		return properties.getProperty(PROPERTY_HOST);
	}

	public String getInterestLogin() {
		return properties.getProperty(PROPERTY_INTEREST_LOGIN);
	}

	public String getInterestPassword() {
		return properties.getProperty(PROPERTY_INTEREST_PASSWORD);
	}

	public String getLogin() {
		return properties.getProperty(PROPERTY_LOGIN);
	}

	public String getPassword() {
		return properties.getProperty(PROPERTY_PASSWORD);
	}

	public int getPort() {
		return Integer.parseInt(properties.getProperty(PROPERTY_PORT));
	}
}
