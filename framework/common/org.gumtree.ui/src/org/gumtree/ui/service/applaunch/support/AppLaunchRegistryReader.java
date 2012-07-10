package org.gumtree.ui.service.applaunch.support;

import static org.gumtree.ui.service.applaunch.support.AppLaunchRegistryConstants.ELEMENT_APP_LAUNCH;
import static org.gumtree.ui.service.applaunch.support.AppLaunchRegistryConstants.EXTENSION_APP_LAUNCHES;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.gumtree.ui.internal.Activator;
import org.gumtree.util.eclipse.ExtensionRegistryReader;

public class AppLaunchRegistryReader extends ExtensionRegistryReader {

	private AppLaunchRegistry registry;
	
	protected AppLaunchRegistryReader(AppLaunchRegistry registry) {
		super(Activator.getDefault());
		this.registry = registry;
	}
	
	@Override
	protected boolean readElement(IConfigurationElement element) {
		if (element.getName().equals(ELEMENT_APP_LAUNCH)) {
			registry.addAppLaunchDescriptor(new AppLaunchDescriptor(element));
			return true;
		}
		return false;
	}

	protected void readAppLaunches() {
		IExtensionRegistry in = Platform.getExtensionRegistry();
		readRegistry(in, "org.gumtree.ui", EXTENSION_APP_LAUNCHES);
	}
	
}
