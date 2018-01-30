package org.gumtree.control.core;

import org.gumtree.control.exception.SicsCommunicationException;
import org.gumtree.control.exception.SicsException;

public interface ISicsChannel {

	String send(String command) throws SicsException;
	boolean isConnected();
	void connect(String serverAddress, String publisherAddress) throws SicsCommunicationException;
	void disconnect();
	boolean isBusy();
}
