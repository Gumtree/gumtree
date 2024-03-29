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

package org.gumtree.sics.io.support;

import org.gumtree.sics.io.ISicsCallback;
import org.gumtree.sics.io.ISicsConnectionContext;
import org.gumtree.sics.io.SicsExecutionException;
import org.gumtree.sics.io.SicsIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SicsRawChannel extends SicsChannel {

	private static Logger logger;

	protected SicsRawChannel(String channelId, SicsProxy sicsProxy) {
		super(channelId, sicsProxy);
	}

	public void login(ISicsConnectionContext context)
			throws SicsExecutionException, SicsIOException {
		if (getChannelState() != ChannelState.DISCONNECTED) {
			throw new SicsIOException(
					"Channel communication has already been established.");
		}

		// STEP 1: Open socket (state: DISCONNECTED -> CONNECTING -> CONNECTED)
		openSocket(context, new SicsSocketListener(this));
		getLogger().info("Connected to SICS");

		// STEP 2: Login (state: CONNECTED -> LOGINED)
		setLogin(context);
		getLogger().info("Logined to SICS as " + context.getRole());

		// STEP 3: Skip set JSON protocol and make this channel available
		setChannelState(ChannelState.NORMAL);
		getLogger().info("Channel to SICS is ready.");

	}

	private Logger getLogger() {
		if (logger == null) {
			logger = LoggerFactory.getLogger(SicsRawChannel.class.getName()
					+ ":" + getChannelId());
		}
		return logger;
	}

	protected synchronized void handleResponse(final Object data, int transId) {
		// do a clean up on completed listeners
		cleanupCompletedListeners();

		final ISicsCallback callback = getCallbackMap().get(transId);
		if (callback == null) {
			return;
		}
		Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					callback.receiveRawData(data);
				} catch (Exception e) {
					logger.error("Failed to callback", e);
				}
			}
		});
		thread.run();
	}

}
