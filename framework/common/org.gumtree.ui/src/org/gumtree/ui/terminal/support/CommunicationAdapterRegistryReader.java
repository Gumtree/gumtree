package org.gumtree.ui.terminal.support;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.gumtree.ui.internal.Activator;
import org.gumtree.util.eclipse.ExtensionRegistryReader;

public class CommunicationAdapterRegistryReader extends ExtensionRegistryReader {

	private CommunicationAdapterRegistry registry;
	
	protected CommunicationAdapterRegistryReader(CommunicationAdapterRegistry registry) {
		super(Activator.getDefault());
		this.registry = registry;
	}

	@Override
	protected boolean readElement(IConfigurationElement element) {
		if(element.getName().equals(CommunicationAdapterRegistryConstants.ELEMENT_COMMUNICATION_ADAPTER)) {
			readAdapter(element);
			return true;
		}
		return false;
	}
	
	private void readAdapter(IConfigurationElement element) {
		registry.addAdapter(new CommunicationAdapterDescriptor(element));
	}
	
	protected void readAdapters() {
		IExtensionRegistry in = Platform.getExtensionRegistry();
		readRegistry(in, getPlugin().getBundle().getSymbolicName(),
				CommunicationAdapterRegistryConstants.EXTENSION_COMMUNICATION_ADAPTERS);
	}
	
}
