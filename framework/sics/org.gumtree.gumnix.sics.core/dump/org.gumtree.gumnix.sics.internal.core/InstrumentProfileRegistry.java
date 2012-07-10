package org.gumtree.gumnix.sics.internal.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.gumtree.core.util.eclipse.EclipseUtils;
import org.gumtree.gumnix.sics.core.IInstrumentProfile;

public class InstrumentProfileRegistry implements IExtensionChangeHandler {

	private List<IInstrumentProfile> descriptors;

	private InstrumentProfileRegistryReader reader;

	protected InstrumentProfileRegistry() {
		super();
		descriptors = new ArrayList<IInstrumentProfile>();
		descriptors.add(new DefaultInstrumentProfile());
		reader = new InstrumentProfileRegistryReader(this);
		reader.readProfiles();
		EclipseUtils.getExtensionTracker().registerHandler(
				this,
				ExtensionTracker
						.createExtensionPointFilter(getExtensionPointFilter()));
	}

	public IInstrumentProfile[] getRegisteredProfiles() {
		return descriptors.toArray(new IInstrumentProfile[descriptors.size()]);
	}

	public IInstrumentProfile getInstrumentDescriptor(String id) {
		for(IInstrumentProfile descriptor : descriptors) {
			if(descriptor.getId().equals(id)) {
				return descriptor;
			}
		}
		return null;
	}

	protected void addInstrumentDescriptor(IInstrumentProfile descriptor) {
		descriptors.add(descriptor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler#addExtension(org.eclipse.core.runtime.dynamichelpers.IExtensionTracker, org.eclipse.core.runtime.IExtension)
	 */
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		IConfigurationElement[] addedElements = extension.getConfigurationElements();
		for (IConfigurationElement element : addedElements) {
			reader.readElement(element);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler#removeExtension(org.eclipse.core.runtime.IExtension, java.lang.Object[])
	 */
	public void removeExtension(IExtension extension, Object[] objects) {
		for (Object object : objects) {
			if (object instanceof IInstrumentProfile) {
				descriptors.remove(object);
			}
		}
	}

	private IExtensionPoint getExtensionPointFilter() {
		return Platform
				.getExtensionRegistry()
				.getExtensionPoint(
						InstrumentProfileRegistryConstants.EXTENTION_POINT_INSTRUMENTS);
	}

}
