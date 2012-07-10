package org.gumtree.sics.core;

import org.gumtree.core.object.IDisposable;
import org.gumtree.sics.io.ISicsProxy;
import org.gumtree.sics.io.SicsIOException;

import ch.psi.sics.hipadaba.SICS;

public interface ISicsModelProvider extends IDisposable {

	public SICS getModel() throws SicsIOException;
	
	public SICS getModel(boolean refresh) throws SicsIOException;

	public ISicsProxy getProxy();
	
	public void setProxy(ISicsProxy proxy);
	
	public long getTimeout();
	
	public void setTimeout(long timeout);
	
}
