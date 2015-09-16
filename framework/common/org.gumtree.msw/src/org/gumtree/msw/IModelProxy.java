package org.gumtree.msw;

import java.io.OutputStream;

import org.gumtree.msw.model.DataSource;

public interface IModelProxy {
	// properties (all return immediately)
	public IRefIdProvider getIdProvider();
	public DataSource getXsd();
	public boolean isConnected();
	public boolean hasControl();
	// use IModelListener to listen for updates
	public Object getProperty(Iterable<String> elementPath, String property);
	// can only be called if notifications are suspended to ensure that no changes are missed
	public Iterable<String> getListElements(Iterable<String> listPath);
	
	// communication
	public void command(ICommand command);

	// serialization
	public boolean serializeTo(Iterable<String> elementPath, OutputStream stream);
	
	// listeners (are called with suspended notifications to make getProperty/getListElements safe)
	public void addListener(IModelListener listener);
	public boolean removeListener(IModelListener listener);
	// multithread support
	public INotificationLock suspendNotifications();
}
