package org.gumtree.gumnix.sics.internal.io;

import org.gumtree.gumnix.sics.io.ISicsCallback;
import org.gumtree.gumnix.sics.io.ISicsChannelMonitor;
import org.gumtree.gumnix.sics.io.ISicsConnectionContext;
import org.gumtree.gumnix.sics.io.SicsExecutionException;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.json.JSONObject;

public interface ISicsChannel {

	public enum ChannelState {
		DISCONNECTED, CONNECTING, CONNECTED, LOGINED, LOGIN_FAILED, NORMAL
	}
	
	public String getChannelId();
	
	public void login(ISicsConnectionContext context) throws SicsExecutionException, SicsIOException;
	
	public void disconnect() throws SicsIOException;
	
	public void send(String command, ISicsCallback proxyListener) throws SicsIOException;
	
	public ChannelState getChannelState();
	
	// New
	public void handleResponse(JSONObject response);
	
	public ISicsChannelMonitor getMonitor();
	
}
