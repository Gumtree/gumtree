package org.gumtree.ui.service.launcher.support;

import static org.gumtree.ui.service.launcher.support.LauncherRegistryConstants.ATTRIBUTE_CATEGORY;
import static org.gumtree.ui.service.launcher.support.LauncherRegistryConstants.ATTRIBUTE_ICON_16;
import static org.gumtree.ui.service.launcher.support.LauncherRegistryConstants.ATTRIBUTE_ICON_32;
import static org.gumtree.ui.service.launcher.support.LauncherRegistryConstants.ATTRIBUTE_ICON_64;
import static org.gumtree.ui.service.launcher.support.LauncherRegistryConstants.ATTRIBUTE_QUICK_LAUNCHER;
import static org.gumtree.util.eclipse.ExtensionRegistryConstants.ATTRIBUTE_CLASS;
import static org.gumtree.util.eclipse.ExtensionRegistryConstants.ATTRIBUTE_ID;
import static org.gumtree.util.eclipse.ExtensionRegistryConstants.ATTRIBUTE_LABEL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gumtree.ui.internal.InternalImage;
import org.gumtree.ui.service.launcher.ILauncher;
import org.gumtree.ui.service.launcher.ILauncherDescriptor;
import org.gumtree.ui.service.launcher.ILauncherRegistry;
import org.gumtree.ui.service.launcher.LauncherException;
import org.gumtree.ui.util.resource.UIResourceUtils;
import org.gumtree.util.eclipse.ExtensionRegistryReader;

public class LauncherDescriptor implements ILauncherDescriptor {

	private IConfigurationElement element;

	private String id;

	private String label;

	private ImageDescriptor icon16;

	private ImageDescriptor icon32;

	private ImageDescriptor icon64;

	private String category;

	private String description;

	private Boolean quickLaunch;

	private ILauncher cache;

	protected LauncherDescriptor(IConfigurationElement element) {
		this.element = element;
	}

	public String getId() {
		if (id == null) {
			id = getElement().getAttribute(ATTRIBUTE_ID);
		}
		return id;
	}

	public String getLabel() {
		if (label == null) {
			label = getElement().getAttribute(ATTRIBUTE_LABEL);
		}
		return label;
	}

	public ImageDescriptor getIcon16() {
		if (icon16 == null) {
			String iconFile = element.getAttribute(ATTRIBUTE_ICON_16);
			if (iconFile != null) {
				icon16 = UIResourceUtils.imageDescriptorFromPlugin(
						element.getNamespaceIdentifier(), iconFile);
			}
			if (icon16 == null) {
				icon16 = InternalImage.ICON_16.getDescriptor();
			}
		}
		return icon16;
	}

	public ImageDescriptor getIcon32() {
		if (icon32 == null) {
			String iconFile = element.getAttribute(ATTRIBUTE_ICON_32);
			if (iconFile != null) {
				icon32 = UIResourceUtils.imageDescriptorFromPlugin(
						element.getNamespaceIdentifier(), iconFile);
			}
			if (icon32 == null) {
				icon32 = InternalImage.ICON_32.getDescriptor();
			}
		}
		return icon32;
	}

	public ImageDescriptor getIcon64() {
		if (icon64 == null) {
			String iconFile = element.getAttribute(ATTRIBUTE_ICON_64);
			if (iconFile != null) {
				icon64 = UIResourceUtils.imageDescriptorFromPlugin(
						element.getNamespaceIdentifier(), iconFile);
			}
			if (icon64 == null) {
				icon64 = InternalImage.ICON_64.getDescriptor();
			}
		}
		return icon64;
	}

	public String getCategory() {
		if (category == null) {
			category = getElement().getAttribute(ATTRIBUTE_CATEGORY);
			if (category == null) {
				category = ILauncherRegistry.ID_CATEGORY_OTHER;
			}
		}
		return category;
	}

	public String getDescription() {
		if (description == null) {
			description = ExtensionRegistryReader.getDescription(getElement());
		}
		return description;
	}

	public boolean isQuickLaunch() {
		if (quickLaunch == null) {
			String quickLaunchString = getElement().getAttribute(
					ATTRIBUTE_QUICK_LAUNCHER);
			quickLaunch = Boolean.valueOf(quickLaunchString);
		}
		return quickLaunch;
	}

	public ILauncher getLauncher() throws LauncherException {
		if (cache == null) {
			synchronized (this) {
				if (cache == null) {
					try {
						Object object = getElement().createExecutableExtension(
								ATTRIBUTE_CLASS);
						if (object instanceof ILauncher) {
							cache = (ILauncher) object;
							cache.setDescriptor(this);
						}
					} catch (CoreException e) {
						throw new LauncherException(
								"Could not create launcher from extension", e);
					}
				}
			}
		}
		return cache;
	}

	private IConfigurationElement getElement() {
		return element;
	}

}
