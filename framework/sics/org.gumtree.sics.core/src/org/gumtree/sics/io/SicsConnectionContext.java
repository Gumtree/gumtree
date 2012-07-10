/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.sics.io;

import java.beans.PropertyChangeListener;

import com.google.common.base.Objects;

public class SicsConnectionContext implements ISicsConnectionContext {

	private String host;

	private int port;

	private SicsRole role;

	private String password;

	public SicsConnectionContext() {
		this("", 0, SicsRole.UNDEF, "");
	}

	public SicsConnectionContext(String host, int port, SicsRole role,
			String password) {
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
	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		// TODO: do we need to implement this?
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		// TODO: do we need to implement this?
	}

	public String toString() {
		return Objects.toStringHelper(this).add("host", getHost())
				.add("port", getPort()).add("role", getRole()).toString();
	}

}
