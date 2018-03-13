package org.gumtree.control.imp;

import java.beans.PropertyChangeListener;

import org.gumtree.control.core.ISicsConnectionContext;
import org.gumtree.control.core.SicsCoreProperties;
import org.gumtree.control.core.SicsRole;

public class SicsConnectionContext implements ISicsConnectionContext {

	private String host;
	
	private int port;
	
	private SicsRole role;
	
	private String password;
	
	public SicsConnectionContext() {
		this("", 0, SicsRole.UNDEF, "");
	}
	
	public SicsConnectionContext(String host, int port, SicsRole role, String password) {
		this.host = host;
		this.port = port;
		this.role = role;
		this.password = password;
	}
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public SicsRole getRole() {
		return role;
	}
	
	public void setRole(SicsRole role) {
		this.role = role;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	// Required by JFace data binding
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		// TODO: do we need to implement this?
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		// TODO: do we need to implement this?
	}
	
	public static ISicsConnectionContext createContextFromSystemProperties() {
		return new SicsConnectionContext(SicsCoreProperties.SERVER_HOST.getValue(),
				SicsCoreProperties.SERVER_PORT.getInt(),
				SicsRole.getRole(SicsCoreProperties.ROLE.getValue()),
				SicsCoreProperties.PASSWORD.getValue());
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SicsConnectionContext(");
		builder.append("host=" + getHost() + ",");
		builder.append("port=" + getPort() + ",");
		builder.append("role=" + getRole() + ",");
		builder.append("password=xxxx");
		builder.append(")");
		return builder.toString();
	}
	
}
