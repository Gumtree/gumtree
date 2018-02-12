package org.gumtree.control.events;

public interface ISicsProxyListener {

	void connect();
	void disconnect();
	void interrupt(boolean isInterrupted);
	
}
