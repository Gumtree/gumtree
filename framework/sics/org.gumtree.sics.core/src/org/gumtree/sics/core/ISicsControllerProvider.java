package org.gumtree.sics.core;

import org.gumtree.core.object.IDisposable;
import org.gumtree.sics.control.IServerController;
import org.gumtree.sics.io.ISicsProxy;

public interface ISicsControllerProvider extends IDisposable {

	public IServerController createServerController();
	
	public ISicsProxy getProxy();
	
	public void setProxy(ISicsProxy proxy);
	
	public ISicsModelProvider getModelProvider();
	
	public void setModelProvider(ISicsModelProvider modelProvider);
	
}
