package org.gumtree.ui.service.launcher.support;

import static org.gumtree.ui.service.launcher.support.LauncherRegistryConstants.ELEMENT_CATEGORY;
import static org.gumtree.ui.service.launcher.support.LauncherRegistryConstants.ELEMENT_LAUNCHER;
import static org.gumtree.ui.service.launcher.support.LauncherRegistryConstants.EXTENSION_LAUNCHERS;
import static org.gumtree.util.eclipse.ExtensionRegistryConstants.ATTRIBUTE_ICON;
import static org.gumtree.util.eclipse.ExtensionRegistryConstants.ATTRIBUTE_ID;
import static org.gumtree.util.eclipse.ExtensionRegistryConstants.ATTRIBUTE_LABEL;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gumtree.ui.internal.Activator;
import org.gumtree.ui.internal.InternalImage;
import org.gumtree.ui.util.resource.UIResourceUtils;
import org.gumtree.util.eclipse.ExtensionRegistryReader;

public class LauncherRegistryReader extends ExtensionRegistryReader {

	private LauncherRegistry registry;

	protected LauncherRegistryReader(LauncherRegistry registry) {
		super(Activator.getDefault());
		this.registry = registry;
	}

	@Override
	protected boolean readElement(IConfigurationElement element) {
		if (element.getName().equals(ELEMENT_LAUNCHER)) {
			registry.addLauncherDescriptor(new LauncherDescriptor(element));
			return true;
		} else if (element.getName().equals(ELEMENT_CATEGORY)) {
			String iconFile = element.getAttribute(ATTRIBUTE_ICON);
			ImageDescriptor icon = null;
			if (iconFile != null) {
				icon = UIResourceUtils.imageDescriptorFromPlugin(
						element.getNamespaceIdentifier(), iconFile);
			}
			if (icon == null) {
				icon = InternalImage.CATEGORY.getDescriptor();
			}
			registry.addCategory(element.getAttribute(ATTRIBUTE_ID),
					element.getAttribute(ATTRIBUTE_LABEL), icon);
			return true;
		}
		return false;
	}

	protected void readLaunchers() {
		IExtensionRegistry in = Platform.getExtensionRegistry();
		readRegistry(in, "org.gumtree.ui", EXTENSION_LAUNCHERS);
	}

}
