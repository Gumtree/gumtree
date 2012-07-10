package org.gumtree.gumnix.sics.core;

import java.net.URL;

public interface IInstrumentProfile {

	/**
	 * ConfigProperty is a set of predefined properties for
	 * the instrument profile extension point.
	 *
	 * @since 1.0
	 */
	public enum ConfigProperty {
		DEFAULT_HOST("defaultHost"),
		DEFAULT_PORT("defaultPort"),
		DEFAULT_TELNET_PORT("defaultTelnetPort"),
		VALIDATION_HOST("validationHost"),
		VALIDATION_PORT("validationPort");

		private ConfigProperty(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		private String name;
	}

	public String getId();

	public String getLabel();

	public String getProperty(String name);

	public String getProperty(ConfigProperty configProperty);

	public URL getImageURL();

	public boolean isDefault();

}
