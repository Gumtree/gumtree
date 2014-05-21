/*******************************************************************************
 * Copyright (c) 2011 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.sics.io;

import org.gumtree.core.object.IDisposable;
import org.gumtree.sics.core.ISicsManager;

public interface ISicsProxy extends IDisposable {
	
	public static final String EVENT_TOPIC_PROXY_ALL = "org/gumtree/sics/proxy/*";
	
	public static final String EVENT_TOPIC_PROXY_STATE_ALL = "org/gumtree/sics/proxy/state/*";
	
	public static final String EVENT_TOPIC_PROXY_STATE_CONNECTED = "org/gumtree/sics/proxy/state/connected";
	
	public static final String EVENT_TOPIC_PROXY_STATE_DISCONNECTED = "org/gumtree/sics/proxy/state/disconnected";
	
	public static final String EVENT_TOPIC_PROXY_STATE_ACTIVATION_REQUESTED = "org/gumtree/sics/proxy/state/activationRequested";
	
	public static final String EVENT_TOPIC_PROXY_MESSAGE_RECEIVED = "org/gumtree/sics/proxy/message/received";
	
	public static final String EVENT_TOPIC_PROXY_MESSAGE_SENT = "org/gumtree/sics/proxy/message/sent";
	
	public static final String EVENT_PROP_PROXY = "proxy";
	
	public static final String EVENT_PROP_CHANNEL = "channel";
	
	public static final String EVENT_PROP_MESSAGE = "message";
	
	public static final String CHANNEL_GENERAL = "general";

	public static final String CHANNEL_STATUS = "status";

	public static final String CHANNEL_BATCH = "batch";

	public static final String CHANNEL_ZIP_DATA = "zipdata";

	public static final String CHANNEL_SCAN = "scan";

	public static final String CHANNEL_RAW_BATCH = "rawBatch";

	public enum ProxyState {
		CONNECTED, DISCONNECTED, DISPOSED
	}

	/*************************************************************************
	 * Attributes
	 *************************************************************************/
	
	public String getId();
	
	public void setId(String id);
	
	/*************************************************************************
	 * I/O
	 *************************************************************************/
	public void login(ISicsConnectionContext context)
			throws SicsExecutionException, SicsIOException;
	
	// throws exception if ready disconnected
	public void disconnect() throws SicsIOException;

	public void send(String command, ISicsCallback callback)
			throws SicsIOException;
	

	public void send(String command, ISicsCallback callback, String channelId)
			throws SicsIOException;

	
	public String[] getConnectedChannelIds();

	public ISicsChannelMonitor[] getChannelMonitors();

	/**
	 * 
	 * @return the current connection details when SICS is connected, null
	 *         otherwise
	 */
	public ISicsConnectionContext getConnectionContext();

	public SicsRole getCurrentRole();
	
	public void changeRole(SicsRole role, String password)
			throws SicsExecutionException, SicsIOException;
	
	/*************************************************************************
	 * Status
	 *************************************************************************/

	public ProxyState getProxyState();

	public ISicsProxyWatchdog getWatchdog();

	public void setWatchdog(ISicsProxyWatchdog watchdog);

	/**
	 * A convenient method to show whether the proxy is connected or not.
	 * 
	 * @return true if this proxy is connected; false otherwise
	 */
	public boolean isConnected();

	public ISicsManager getSicsManager();

	public void setSicsManager(ISicsManager sicsManager);

}
