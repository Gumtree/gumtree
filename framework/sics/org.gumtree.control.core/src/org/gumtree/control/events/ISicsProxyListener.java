package org.gumtree.control.events;

import org.gumtree.control.core.ServerStatus;

public interface ISicsProxyListener {

	void connect();
	void disconnect();
	void modelUpdated();
	void interrupt(boolean isInterrupted);
	void setStatus(ServerStatus newStatus);
	void proxyConnectionReqested();
	void activate();
	void deactivate();
	boolean isActive();
}
