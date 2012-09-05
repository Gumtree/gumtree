package org.gumtree.ui.service.applaunch.support;

import static org.gumtree.ui.service.applaunch.support.AppLaunchRegistryConstants.ATTRIBUTE_COMMAND_ID;
import static org.gumtree.ui.service.applaunch.support.AppLaunchRegistryConstants.ATTRIBUTE_ICON_64;
import static org.gumtree.ui.service.applaunch.support.AppLaunchRegistryConstants.ELEMENT_PARAMETER;
import static org.gumtree.util.eclipse.ExtensionRegistryConstants.ATTRIBUTE_ID;
import static org.gumtree.util.eclipse.ExtensionRegistryConstants.ATTRIBUTE_LABEL;
import static org.gumtree.util.eclipse.ExtensionRegistryConstants.ATTRIBUTE_VALUE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gumtree.ui.internal.InternalImage;
import org.gumtree.ui.service.applaunch.IAppLaunchDescriptor;
import org.gumtree.widgets.swt.util.UIResourceUtils;

import com.google.common.base.Objects;

public class AppLaunchDescriptor implements IAppLaunchDescriptor {

	private IConfigurationElement element;

	private String commandId;

	private String label;

	private ImageDescriptor icon64;

	private Map<String, String> parameters;

	public AppLaunchDescriptor(IConfigurationElement element) {
		this.element = element;
	}

	public String getCommandId() {
		if (commandId == null) {
			commandId = getElement().getAttribute(ATTRIBUTE_COMMAND_ID);
		}
		return commandId;
	}

	public String getLabel() {
		if (label == null) {
			label = getElement().getAttribute(ATTRIBUTE_LABEL);
		}
		return label;
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

	public boolean hasParameters() {
		return element.getChildren(ELEMENT_PARAMETER).length > 0;
	}
	
	public Map<String, String> getParameters() {
		if (parameters == null) {
			parameters = new HashMap<String, String>(2);
			IConfigurationElement[] parameterElements = element.getChildren(ELEMENT_PARAMETER);
			for (IConfigurationElement parameterElement : parameterElements) {
				String id = parameterElement.getAttribute(ATTRIBUTE_ID);
				String value = parameterElement.getAttribute(ATTRIBUTE_VALUE);
				parameters.put(id, value);
			}
		}
		return Collections.unmodifiableMap(parameters);
	}

	private IConfigurationElement getElement() {
		return element;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("label", getLabel())
				.add("commandId", getCommandId()).toString();
	}

}
