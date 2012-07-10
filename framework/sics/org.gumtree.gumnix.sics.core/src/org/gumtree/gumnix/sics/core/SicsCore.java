package org.gumtree.gumnix.sics.core;

import org.gumtree.core.service.ServiceUtils;
import org.gumtree.gumnix.sics.control.ISicsController;
import org.gumtree.gumnix.sics.io.ISicsProxy;

public final class SicsCore {

	public static ISicsManager getSicsManager() {
		return ServiceUtils.getService(ISicsManager.class);
	}

	public static ISicsProxy getDefaultProxy() {
		return getSicsManager().proxy();
	}

	public static ISicsController getSicsController() {
		return getSicsManager().control().getSicsController();
	}

	private SicsCore() {
		super();
	}

}
