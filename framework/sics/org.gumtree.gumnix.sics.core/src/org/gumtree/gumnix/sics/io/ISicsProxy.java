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


public interface ISicsProxy {

	public static final String CHANNEL_GENERAL = "general";

	public static final String CHANNEL_STATUS = "status";

	public static final String CHANNEL_BATCH = "batch";

	public static final String CHANNEL_ZIP_DATA = "zipdata";

	public static final String CHANNEL_SCAN = "scan";
	
	public static final String CHANNEL_RAW_BATCH = "rawBatch";

	public enum ProxyState {
		CONNECTED, DISCONNECTED
	}

	public String getId();
	
	public void login(ISicsConnectionContext context) throws SicsExecutionException, SicsIOException;

	public void changeRole(SicsRole role, String password) throws SicsExecutionException, SicsIOException;

	// throws exception if ready disconnected
	public void disconnect() throws SicsIOException;

	/**
	 * @return
	 */
	public SicsRole getCurrentRole();

	/**
	 * @param command
	 * @param callback
	 * @throws SicsIOException
	 */
	public void send(String command, ISicsCallback callback) throws SicsIOException;

	/**
	 * @param command
	 * @param callback
	 * @param channelId
	 * @throws SicsIOException
	 */
	public void send(String command, ISicsCallback callback, String channelId) throws SicsIOException;

	public String[] getConnectedChannelIds();

	public ISicsChannelMonitor[] getChannelMonitors();
	
	/**
	 *
	 * @return the current connection details when SICS is connected, null otherwise
	 */
	public ISicsConnectionContext getConnectionContext();

	/**
	 * Returns the current state of this proxy.
	 *
	 * @return proxy state
	 */
	public ProxyState getProxyState();

	/**
	 * A convenient method to show whether the proxy is connected or not.
	 * 
	 * @return true if this proxy is connected; false otherwise
	 */
	public boolean isConnected();
	
	/**
	 * Adds a listener to this proxy.
	 *
	 * @param listener the proxy listener for monitoring the proxy
	 */
	public void addProxyListener(ISicsProxyListener listener);

	/**
	 * Removes a listener from this proxy.
	 *
	 * @param listener the registered proxy listener that was monitoring the proxy
	 */
	public void removeProxyListener(ISicsProxyListener listener);
	
	public ISicsProxyWatchdog getWatchdog();
	
	public void setWatchdog(ISicsProxyWatchdog watchdog);
	
}
