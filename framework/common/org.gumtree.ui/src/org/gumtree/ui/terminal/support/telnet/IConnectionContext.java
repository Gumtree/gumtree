package org.gumtree.ui.terminal.support.telnet;

public interface IConnectionContext {

	public String getHost();

	public void setHost(String host);

	public int getPort();

	public void setPort(int port);

}