package org.gumtree.gumnix.sics.simulator.services;

public interface ISicsConnection {

	public enum Flag {
		status, hdbevent, error, warning
	}

	public enum ConnectionState {
		LOGIN, NORMAL
	}

	public int getConnectionId();

	public ConnectionState getConnectionState();

	public ISicsPrototocol getProtocol();

	public void setProtocol(ISicsPrototocol protocol);

	public void write(ISicsOutput sicsOutput);

}
