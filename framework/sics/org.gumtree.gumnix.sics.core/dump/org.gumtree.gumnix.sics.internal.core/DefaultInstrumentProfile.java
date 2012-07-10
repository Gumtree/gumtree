package org.gumtree.gumnix.sics.internal.core;

import java.net.URL;

import org.gumtree.gumnix.sics.core.IInstrumentProfile;

public class DefaultInstrumentProfile implements IInstrumentProfile {

	private static final String ID = "DefaultInstrumentProfile";

	private static final String LABEL = "Default";

	protected DefaultInstrumentProfile() {
	}

	public String getId() {
		return ID;
	}

	public URL getImageURL() {
		return null;
	}

	public String getLabel() {
		return LABEL;
	}

	public String getProperty(String name) {
		return null;
	}

	public String getProperty(ConfigProperty configProperty) {
		// testing
		if(configProperty.equals(ConfigProperty.DEFAULT_HOST)) {
			return "localhost";
		}
		return null;
	}

	public boolean isDefault() {
		return true;
	}

}
