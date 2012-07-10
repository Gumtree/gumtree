package org.gumtree.ui.service.sidebar.support;

import static org.gumtree.ui.service.sidebar.support.GadgetRegistryConstants.ELEMENT_GADGET;
import static org.gumtree.ui.service.sidebar.support.GadgetRegistryConstants.EXTENSION_GADGETS;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.gumtree.ui.internal.Activator;
import org.gumtree.util.eclipse.ExtensionRegistryReader;

public class GadgetRegistryReader extends ExtensionRegistryReader {

	private GadgetRegistry registry;
	
	protected GadgetRegistryReader(GadgetRegistry registry) {
		super(Activator.getDefault());
		this.registry = registry;
	}

	@Override
	protected boolean readElement(IConfigurationElement element) {
		if (element.getName().equals(ELEMENT_GADGET)) {
			// Registers gadget
			registry.addGadget(new ExtensionBasedGadget(element));
			return true;
		}
		return false;
	}

	protected void readGadgets() {
		IExtensionRegistry in = Platform.getExtensionRegistry();
		readRegistry(in, "org.gumtree.ui", EXTENSION_GADGETS);
	}

}
