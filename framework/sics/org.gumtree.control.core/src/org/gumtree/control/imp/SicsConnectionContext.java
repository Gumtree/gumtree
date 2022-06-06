package org.gumtree.control.imp;

import java.beans.PropertyChangeListener;

import org.gumtree.control.core.ISicsConnectionContext;
import org.gumtree.control.core.SicsCoreProperties;
import org.gumtree.control.core.SicsRole;

public class SicsConnectionContext implements ISicsConnectionContext {

	private String serverAddress;
	
	private String publisherAddress;
	
	private SicsRole role;
	
	private String password;
	
	public SicsConnectionContext() {
		this("", "", SicsRole.UNDEF, "");
	}
	
	public SicsConnectionContext(String serverAddress, String publisherAddress, SicsRole role, String password) {
		this.serverAddress = serverAddress;
		this.publisherAddress = publisherAddress;
		this.role = role;
		this.password = password;
	}
	
	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String address) {
		this.serverAddress = address;
	}
	
	public String getPublisherAddress() {
		return publisherAddress;
	}

	public void setPublisherAddress(String address) {
		publisherAddress = address;
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
		return new SicsConnectionContext(
				SicsCoreProperties.SERVER_HOST.getValue() + ":" + SicsCoreProperties.SERVER_PORT.getValue(),
				SicsCoreProperties.SERVER_HOST.getValue() + ":" + SicsCoreProperties.PUBLISH_PORT.getValue(),
				SicsRole.getRole(SicsCoreProperties.ROLE.getValue()),
				SicsCoreProperties.PASSWORD.getValue());
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SicsConnectionContext(");
		builder.append("serverAddress=" + getServerAddress() + ",");
		builder.append("publisherAddress=" + getPublisherAddress() + ",");
		builder.append("role=" + getRole() + ",");
		builder.append("password=xxxx");
		builder.append(")");
		return builder.toString();
	}
}
