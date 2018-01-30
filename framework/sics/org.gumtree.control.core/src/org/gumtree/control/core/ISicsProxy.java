package org.gumtree.control.core;

import org.gumtree.control.exception.SicsException;

public interface ISicsProxy {

	boolean connect(String serverAddress, String publisherAddress);
	void disconnect();
	boolean isConnected();
	void send(String command, ISicsCallback callback, String channelName) throws SicsException;
	String syncRun(String command) throws SicsException;
	ISicsChannel getSicsChannel();
}
