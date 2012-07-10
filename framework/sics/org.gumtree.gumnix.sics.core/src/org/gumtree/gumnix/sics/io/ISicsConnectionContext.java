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

package org.gumtree.gumnix.sics.io;

/**
 * The SICS connection context contains information for
 * establishing connection to SICS via network socket.
 *
 * @since 1.0
 */
public interface ISicsConnectionContext {

	/**
	 * Returns the host name of SICS.
	 *
	 * @return SICS host name for connection
	 */
	public String getHost();

	/**
	 * Sets the host name for SICS
	 *
	 * @param host SICS host name for connection
	 */
	public void setHost(String host);

	/**
	 * Returns the SICS server port for connection.
	 *
	 * @return port number of the SICS server
	 */
	public int getPort();

	/**
	 * Sets the SICS server port for connection.
	 *
	 * @param port number of the SICS server
	 */
	public void setPort(int port);

	/**
	 * Returns the role set in this context.
	 *
	 * @return SicsRole object for initial login
	 */
	public SicsRole getRole();

	/**
	 * Sets the role of this context.
	 *
	 * @param role sics role for this sics connection
	 */
	public void setRole(SicsRole role);

	/**
	 * Returns the password string in this context.
	 *
	 * @return string password for sics login
	 */
	public String getPassword();

	/**
	 * Sets the passoword to this context.
	 *
	 * @param password clear text password for sics login
	 */
	public void setPassword(String password);

}
