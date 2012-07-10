package org.gumtree.ui.terminal.support;

import org.eclipse.core.runtime.IConfigurationElement;
import org.gumtree.ui.terminal.CommunicationAdapterException;
import org.gumtree.ui.terminal.ICommunicationAdapter;
import org.gumtree.ui.terminal.ICommunicationAdapterDescriptor;
import org.gumtree.util.eclipse.ExtensionRegistryConstants;

public class CommunicationAdapterDescriptor implements
		ICommunicationAdapterDescriptor {

	private IConfigurationElement element;

	private String id;

	private String label;

	protected CommunicationAdapterDescriptor(IConfigurationElement element) {
		this.element = element;
	}

	public ICommunicationAdapter createNewAdapter()
			throws CommunicationAdapterException {
		try {
			Object adapter = element
					.createExecutableExtension(ExtensionRegistryConstants.ATTRIBUTE_CLASS);
			if (adapter instanceof ICommunicationAdapter) {
				return (ICommunicationAdapter) adapter;
			}
		} catch (Exception e) {
			throw new CommunicationAdapterException(
					"Cannot not instantiate communication adapter (" + getId()
							+ ").", e);
		}
		throw new CommunicationAdapterException(
				"Cannot not instantiate communication adapter (" + getId()
						+ ").");
	}

	public String getId() {
		if (id == null) {
			id = element.getAttribute(ExtensionRegistryConstants.ATTRIBUTE_ID);
		}
		return id;
	}

	public String getLabel() {
		if (label == null) {
			label = element
					.getAttribute(ExtensionRegistryConstants.ATTRIBUTE_LABEL);
		}
		return label;
	}

	public String toString() {
		if (getLabel() != null) {
			return getLabel();
		}
		return getId();
	}

}
