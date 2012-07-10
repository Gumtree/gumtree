package org.gumtree.gumnix.sics.internal.core;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.gumtree.core.util.eclipse.ExtensionRegistryReader;

public class InstrumentProfileRegistryReader extends ExtensionRegistryReader {

	private InstrumentProfileRegistry registry;
	
	protected InstrumentProfileRegistryReader(InstrumentProfileRegistry registry) {
		super(Activator.getDefault());
		this.registry = registry;
	}
	
	@Override
	protected boolean readElement(IConfigurationElement element) {
		if(element.getName().equals(InstrumentProfileRegistryConstants.ELEMENT_PROFILE)) {
			readInstrument(element);
			return true;
		}
		return false;
	}
	
	private void readInstrument(IConfigurationElement element) {
		registry.addInstrumentDescriptor(new InstrumentProfile(element));
	}
	
	protected void readProfiles() {
		IExtensionRegistry in = Platform.getExtensionRegistry();
		readRegistry(in, getPlugin().getBundle().getSymbolicName(),
				InstrumentProfileRegistryConstants.EXTENSION_INSTRUMENT_PROFILES);
	}

}
