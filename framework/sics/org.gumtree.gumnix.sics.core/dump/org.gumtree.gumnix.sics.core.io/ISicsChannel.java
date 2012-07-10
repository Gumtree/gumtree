package org.gumtree.gumnix.sics.core.io;


public interface ISicsChannel {
	public enum ChannelState {
		DISCONNECTED, LOGIN, CONNECTED, SYCAMORE
	}
	
	public enum ChannelType {
		GENERAL, INTEREST
	}
	
	public void send(String command, ISicsProxyListener proxyListener) throws SicsIOException;
	
	public void logout() throws SicsIOException;
	
	public void handleResponse(ISycamoreResponse response);

	public void addInterestListner(String sicsObjectId, ISicsInterestListener interestListener) throws SicsIOException;

	public void removeInterestListener(String sicsObjectId, ISicsInterestListener interestListener) throws SicsIOException;
	
	public ChannelState getChannelState();
	
	public void setChannelState(ChannelState channelState);
	
	public ChannelType getChannelType();
}
