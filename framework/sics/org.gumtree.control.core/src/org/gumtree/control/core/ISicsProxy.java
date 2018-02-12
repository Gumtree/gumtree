package org.gumtree.control.core;

import java.util.Map;

import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.exception.SicsException;

public interface ISicsProxy {

	boolean connect(String serverAddress, String publisherAddress);
	void disconnect();
	boolean isConnected();
	String send(String command, ISicsCallback callback) throws SicsException;
	String syncRun(String command) throws SicsException;
	ISicsChannel getSicsChannel();
	ServerStatus getServerStatus();
	void setServerStatus(ServerStatus status);
	boolean multiDrive(Map<String, Number> devices) throws SicsException;
	void interrupt();
	boolean isInterrupted();
	void labelInterruptFlag();
	void clearInterruptFlag();
	void addProxyListener(ISicsProxyListener listener);
	void removeProxyListener(ISicsProxyListener listener);
	
}
