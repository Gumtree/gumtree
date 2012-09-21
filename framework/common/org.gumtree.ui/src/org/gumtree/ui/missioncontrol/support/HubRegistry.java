package org.gumtree.ui.missioncontrol.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.gumtree.ui.missioncontrol.IHub;
import org.gumtree.ui.missioncontrol.IHubRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HubRegistry implements IHubRegistry {

	private static final Logger logger = LoggerFactory
			.getLogger(HubRegistry.class);

	private Map<String, IHub> hubMap;
	
	public HubRegistry() {
		hubMap = new TreeMap<String, IHub>();
	}
	
	/*************************************************************************
	 * Life cycle
	 *************************************************************************/
	
	public void activate() {
		
	}
	
	public void deactivate() {
		
	}
	
	/*************************************************************************
	 * API
	 *************************************************************************/
	
	@Override
	public List<IHub> getHubs() {
		return new ArrayList<IHub>(hubMap.values());
	}

	@Override
	public void addHub(IHub hub) {
		hubMap.put(hub.getLabel(), hub);
	}

	@Override
	public void removeHub(IHub hub) {
		hubMap.remove(hub.getLabel());
	}

}
