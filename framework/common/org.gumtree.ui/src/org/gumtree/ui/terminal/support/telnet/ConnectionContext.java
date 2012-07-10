package org.gumtree.ui.terminal.support.telnet;

public class ConnectionContext implements IConnectionContext {

	private String host;
	
	private int port;
	
	public ConnectionContext() {
		super();
	}

	public ConnectionContext(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.ui.terminal.telnet.IConnectionContext#getHost()
	 */
	public String getHost() {
		return host;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.ui.terminal.telnet.IConnectionContext#setHost(java.lang.String)
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.ui.terminal.telnet.IConnectionContext#getPort()
	 */
	public int getPort() {
		return port;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.ui.terminal.telnet.IConnectionContext#setPort(int)
	 */
	public void setPort(int port) {
		this.port = port;
	}
	
}
