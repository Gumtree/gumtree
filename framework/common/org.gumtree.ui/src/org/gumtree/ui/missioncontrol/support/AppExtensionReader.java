package org.gumtree.ui.missioncontrol.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.gumtree.ui.internal.Activator;
import org.gumtree.ui.missioncontrol.IApp;
import org.gumtree.util.eclipse.ExtensionRegistryReader;

public class AppExtensionReader extends ExtensionRegistryReader {

	public static String EXTENSION_APPS = "apps";

	public static String ELEMENT_APP = "app";

	public static String ATTRIBUTE_LABEL = "label";

	public static String EXTENTION_POINT_APPS = Activator.PLUGIN_ID + "."
			+ EXTENSION_APPS;

	private IExtensionRegistry extensionRegistry;

	private List<IApp> registeredApps;

	public AppExtensionReader() {
		super(Activator.getDefault());
	}

	@Override
	protected boolean readElement(IConfigurationElement element) {
		if (element.getName().equals(EXTENSION_APPS)) {
			IApp app = new App();
			app.setLabel(element.getAttribute(ATTRIBUTE_LABEL));
			registeredApps.add(app);
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
	public List<IApp> getRegisteredApps() {
		if (registeredApps == null) {
			registeredApps = new ArrayList<IApp>(2);
			readRegistry(getExtensionRegistry(), Activator.PLUGIN_ID,
					EXTENSION_APPS);
		}
		return Collections.unmodifiableList(registeredApps);
	}

}
