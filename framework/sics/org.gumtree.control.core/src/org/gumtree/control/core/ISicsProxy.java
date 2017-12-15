package org.gumtree.control.core;

import org.gumtree.control.exception.SicsCommunicationException;
import org.gumtree.control.exception.SicsException;

public interface ISicsProxy {

	boolean connect(String server);
	void disconnect();
	boolean isConnected();
	void send(String command, ISicsCallback callback, String channelName) throws SicsCommunicationException;
	String syncRun(String command) throws SicsException;
	
}
