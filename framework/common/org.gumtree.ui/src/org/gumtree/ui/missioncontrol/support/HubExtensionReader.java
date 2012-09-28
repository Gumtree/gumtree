package org.gumtree.ui.missioncontrol.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.gumtree.ui.internal.Activator;
import org.gumtree.ui.missioncontrol.IHub;
import org.gumtree.ui.tasklet.ITasklet;
import org.gumtree.ui.tasklet.support.Tasklet;
import org.gumtree.util.eclipse.ExtensionRegistryReader;
import org.gumtree.util.string.StringUtils;

public class HubExtensionReader extends ExtensionRegistryReader {

	public static String EXTENSION_HUBS = "hubs";

	public static String ELEMENT_HUB = "hub";

	public static String ATTRIBUTE_LABEL = "label";

	public static String EXTENTION_POINT_HUBS = Activator.PLUGIN_ID + "."
			+ EXTENSION_HUBS;

	private IExtensionRegistry extensionRegistry;

	private List<IHub> registeredHubs;

	public HubExtensionReader() {
		super(Activator.getDefault());
	}

	@Override
	protected boolean readElement(IConfigurationElement element) {
		if (element.getName().equals(ELEMENT_HUB)) {
			IHub hub = new Hub();
			hub.setLabel(element.getAttribute(ATTRIBUTE_LABEL));
			registeredHubs.add(hub);
		}
		return true;
	}

	public IExtensionRegistry getExtensionRegistry() {
		if (extensionRegistry == null) {
			extensionRegistry = Platform.getExtensionRegistry();
		}
		return extensionRegistry;
	}

	public void setExtensionRegistry(IExtensionRegistry extensionRegistry) {
		this.extensionRegistry = extensionRegistry;
	}

	// This is not thread safe
	public List<IHub> getRegisteredHubs() {
		if (registeredHubs == null) {
			registeredHubs = new ArrayList<IHub>(2);
			readRegistry(getExtensionRegistry(), Activator.PLUGIN_ID,
					EXTENSION_HUBS);
		}
		return Collections.unmodifiableList(registeredHubs);
	}

}
