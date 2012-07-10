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

import org.json.JSONObject;

public interface ISicsChannel {
	
	public enum ChannelState {
		DISCONNECTED, CONNECTING, CONNECTED, LOGINED, LOGIN_FAILED, NORMAL
	}

	public String getChannelId();

	public void login(ISicsConnectionContext context)
			throws SicsExecutionException, SicsIOException;

	public void disconnect() throws SicsIOException;

	public void send(String command, ISicsCallback proxyListener)
			throws SicsIOException;

	public ChannelState getChannelState();

	// New
	public void handleResponse(JSONObject response);

	public ISicsChannelMonitor getMonitor();

}
