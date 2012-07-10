package org.gumtree.gumnix.sics.internal.ui;

import org.gumtree.ui.terminal.support.telnet.ConnectionContext;

public class SicsConnectionContext extends ConnectionContext {

	private String login;
	
	private String password;

	public SicsConnectionContext() {
		super();
	}
	
	public SicsConnectionContext(String host, int port) {
		super(host, port);
	}
	
	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
