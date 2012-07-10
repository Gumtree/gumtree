package org.gumtree.ui.terminal.support;

import org.gumtree.ui.internal.Activator;

public class CommunicationAdapterRegistryConstants {

	public static final String EXTENSION_COMMUNICATION_ADAPTERS = "communicationAdapters";

	public static final String ELEMENT_COMMUNICATION_ADAPTER = "communicationAdapter";

	public static final String EXTENTION_POINT_COMMUNICATION_ADAPTERS = Activator.PLUGIN_ID
			+ "." + EXTENSION_COMMUNICATION_ADAPTERS;

	private CommunicationAdapterRegistryConstants() {
		super();
	}

}
