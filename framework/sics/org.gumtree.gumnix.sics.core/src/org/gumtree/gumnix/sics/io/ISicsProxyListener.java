package org.gumtree.gumnix.sics.io;

public interface ISicsProxyListener {

	public void proxyConnectionReqested();

	public void proxyConnected();

	public void proxyDisconnected();

	public void messageSent(String message, String channelId);

	public void messageReceived(String message, String channelId);

}
