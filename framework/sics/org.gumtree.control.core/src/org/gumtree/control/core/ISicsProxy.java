package org.gumtree.control.core;

import org.gumtree.control.exception.SicsCommunicationException;

public interface ISicsProxy {

	void connect();
	void disconnect();
	boolean isConnected();
	void send(String command, ISicsCallback callback, String channelName) throws SicsCommunicationException;
	
}
