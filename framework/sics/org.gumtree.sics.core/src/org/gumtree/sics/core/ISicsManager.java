package org.gumtree.sics.core;

import org.gumtree.core.object.IDisposable;
import org.gumtree.core.service.IService;
import org.gumtree.sics.batch.IBatchBufferManager;
import org.gumtree.sics.control.IServerController;
import org.gumtree.sics.io.ISicsProxy;

public interface ISicsManager extends IService, IDisposable {
	
	public ISicsProxy getProxy();
	
	public void setProxy(ISicsProxy proxy);
	
	public ISicsMonitor getMonitor();
	
	public void setMonitor(ISicsMonitor monitor);
	
	public ISicsModelProvider getModelProvider();
	
	public void setModelProvider(ISicsModelProvider modelProvider);
	
	public ISicsControllerProvider getSicsControllerProvider();
	
	public void setSicsControllerProvider(ISicsControllerProvider controllerProvider);
	
	public IServerController getServerController();
	
	public IBatchBufferManager getBufferManager();

	void reloadServerController();
	
}
