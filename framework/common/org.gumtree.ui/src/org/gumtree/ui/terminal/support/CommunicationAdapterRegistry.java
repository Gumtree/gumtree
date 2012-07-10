package org.gumtree.ui.terminal.support;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.ui.PlatformUI;
import org.gumtree.ui.terminal.ICommunicationAdapterDescriptor;
import org.gumtree.ui.terminal.ICommunicationAdapterRegistry;

public class CommunicationAdapterRegistry implements
		ICommunicationAdapterRegistry, IExtensionChangeHandler {

	private List<ICommunicationAdapterDescriptor> descriptors;

	private CommunicationAdapterRegistryReader reader;

	private static final ICommunicationAdapterRegistry singleton = new CommunicationAdapterRegistry();

	public CommunicationAdapterRegistry() {
		super();
		reader = new CommunicationAdapterRegistryReader(this);
		reader.readAdapters();
//		PlatformUI.getWorkbench().getExtensionTracker().registerHandler(
//				this,
//				ExtensionTracker
//						.createExtensionPointFilter(getExtensionPointFilter()));
	}

	public static ICommunicationAdapterRegistry getDefault() {
		return singleton;
	}

	public void addAdapter(ICommunicationAdapterDescriptor descriptor) {
		getDescriptors().add(descriptor);
	}

	/* (non-Javadoc)
	 * @see org.gumtree.ui.terminal.ICommunicationAdapterRegistry#getAdapterDescriptor(java.lang.String)
	 */
	public ICommunicationAdapterDescriptor getAdapterDescriptor(String id) {
		for(ICommunicationAdapterDescriptor desc : getDescriptors()) {
			if(desc.getId().equals(id)) {
				return desc;
			}
		}
		return null;
	}

	public ICommunicationAdapterDescriptor[] getAdapterDescriptors() {
		return getDescriptors().toArray(new ICommunicationAdapterDescriptor[getDescriptors().size()]);
	}

	private List<ICommunicationAdapterDescriptor> getDescriptors() {
		if(descriptors == null) {
			descriptors = new ArrayList<ICommunicationAdapterDescriptor>();
		}
		return descriptors;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler#addExtension(org.eclipse.core.runtime.dynamichelpers.IExtensionTracker,
	 *      org.eclipse.core.runtime.IExtension)
	 */
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		IConfigurationElement[] addedElements = extension
				.getConfigurationElements();
		for (IConfigurationElement element : addedElements) {
			reader.readElement(element);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler#removeExtension(org.eclipse.core.runtime.IExtension,
	 *      java.lang.Object[])
	 */
	public void removeExtension(IExtension extension, Object[] objects) {
		for (Object object : objects) {
			if (object instanceof ICommunicationAdapterDescriptor) {
				getDescriptors().remove(object);
			}
		}
	}

	private IExtensionPoint getExtensionPointFilter() {
		return Platform
				.getExtensionRegistry()
				.getExtensionPoint(
						CommunicationAdapterRegistryConstants.EXTENTION_POINT_COMMUNICATION_ADAPTERS);
	}

}
