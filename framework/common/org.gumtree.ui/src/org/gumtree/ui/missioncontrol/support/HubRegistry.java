package org.gumtree.ui.missioncontrol.support;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.gumtree.ui.missioncontrol.IHub;
import org.gumtree.ui.missioncontrol.IHubRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HubRegistry implements IHubRegistry {

	private static final Logger logger = LoggerFactory
			.getLogger(HubRegistry.class);

	private SortedSet<IHub> hubs;
	
	private IExtensionRegistry extensionRegistry;
	
	public HubRegistry() {
		hubs = new TreeSet<IHub>(new Comparator<IHub>() {
			@Override
			public int compare(IHub hub1, IHub hub2) {
				return hub1.getLabel().compareTo(hub2.getLabel());
			}
		});
	}
	
	/*************************************************************************
	 * Life cycle
	 *************************************************************************/
	
	public void activate() {
		// Load from extension point
		if (getExtensionRegistry() != null) {
			HubExtensionReader reader = new HubExtensionReader();
			reader.setExtensionRegistry(getExtensionRegistry());
			List<IHub> registeredHubs = reader.getRegisteredHubs();
			hubs.addAll(registeredHubs);
		}
		// Load from local disk
//		List<ITasklet> persistedTasklets = persistor.loadTasklet();
//		tasklets.addAll(persistedTasklets);
	}
	
	public void deactivate() {
		extensionRegistry = null;
	}
	
	/*************************************************************************
	 * API
	 *************************************************************************/
	
	@Override
	public List<IHub> getHubs() {
		return new ArrayList<IHub>(hubs);
	}

	@Override
	public void addHub(IHub hub) {
		hubs.add(hub);
	}

	@Override
	public void removeHub(IHub hub) {
		hubs.remove(hub);
	}

	/*************************************************************************
	 * Components
	 *************************************************************************/
	
	public IExtensionRegistry getExtensionRegistry() {
		return extensionRegistry;
	}

	@Inject
	public void setExtensionRegistry(IExtensionRegistry extensionRegistry) {
		this.extensionRegistry = extensionRegistry;
	}
	
}
