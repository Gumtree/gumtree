package org.gumtree.ui.terminal;

import org.gumtree.core.service.IService;

public interface ICommunicationAdapterRegistry extends IService {

	public ICommunicationAdapterDescriptor[] getAdapterDescriptors();

	/**
	 * @param id
	 * @return a descriptor of the registered adapter that matches the id; null otherwise
	 */
	public ICommunicationAdapterDescriptor getAdapterDescriptor(String id);

}
