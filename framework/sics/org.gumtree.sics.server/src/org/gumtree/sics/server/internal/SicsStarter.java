package org.gumtree.sics.server.internal;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.gumtree.core.object.IDisposable;
import org.gumtree.sics.core.ISicsManager;

public class SicsStarter implements IDisposable {

	ISicsManager sicsManager;
	
	public SicsStarter() {
		// TODO Auto-generated constructor stub
	}
	
	public ISicsManager getSicsManager() {
		return sicsManager;
	}
	
	@Inject
	public void setSicsManager(ISicsManager sicsManager) {
		this.sicsManager = sicsManager;
	}

	@Override
	@PreDestroy
	public void disposeObject() {
		sicsManager = null;
	}
	
}
