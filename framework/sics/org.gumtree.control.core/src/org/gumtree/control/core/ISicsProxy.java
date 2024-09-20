package org.gumtree.control.core;

import java.util.Map;

import org.gumtree.control.batch.IBatchControl;
import org.gumtree.control.events.ISicsCallback;
import org.gumtree.control.events.ISicsMessageListener;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.exception.SicsException;
import org.json.JSONObject;

public interface ISicsProxy {

	ISicsModel getSicsModel();
	boolean connect(String serverAddress, String publisherAddress) throws SicsException;
	boolean reconnect() throws SicsException; 
	void disconnect();
	boolean isConnected();
	String syncRun(String command) throws SicsException;
	String syncRun(String command, ISicsCallback callback) throws SicsException;
	void asyncRun(String command, ISicsCallback callback) throws SicsException;
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
	IBatchControl getBatchControl();
	void addMessageListener(ISicsMessageListener listener);
	void removeMessageListener(ISicsMessageListener listener);
	void fireMessageEvent(JSONObject message);
	void updateGumtreeXML() throws SicsException;
	ISicsConnectionContext getConnectionContext();
}
