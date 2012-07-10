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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.gumtree.sics.io.SicsCommunicationConstants;
import org.gumtree.sics.io.ISicsChannel.ChannelState;
import org.gumtree.sics.io.SicsCommunicationConstants.JSONTag;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SicsSocketListener implements Runnable {

	private static Logger logger;

	private InputStream input;

	private BufferedReader reader;

	private SicsChannel channel;

	protected SicsSocketListener(SicsChannel channel) {
		this.channel = channel;
	}

	public void run() {
		try {
			String replyMessage;
			while ((replyMessage = getReader().readLine()) != null
					&& getChannel().getChannelState() != ChannelState.DISCONNECTED) {
				// Update statistics
				getChannel().lineRead++;

				// little hack to sics telnet bug
				while (replyMessage.startsWith("ящ")) {
					replyMessage = replyMessage.substring(2);
				}

				// for broadcasting to console within application
				getChannel().messageRecieved(replyMessage);
				if (getChannel().getChannelState() == ChannelState.NORMAL) {
					// We log the reply after parsing JSON object
					handleNormalState(replyMessage);
					continue;
				} else if (getChannel().getChannelState() == ChannelState.CONNECTING) {
					getLogger().info("Server replied: " + replyMessage);
					handleConnectingState(replyMessage);
					continue;
				} else if (getChannel().getChannelState() == ChannelState.CONNECTED) {
					getLogger().info("Server replied: " + replyMessage);
					handleConnectedState(replyMessage);
					continue;
				} else if (getChannel().getChannelState() == ChannelState.LOGINED) {
					getLogger().info("Server replied: " + replyMessage);
					handleLoginedState(replyMessage);
					continue;
				} else {
					getLogger().info("Server replied: " + replyMessage);
				}
			}
		} catch (IOException e) {
			getLogger().info("Error in reading SICS output.", e);
		}
	}

	protected void handleConnectingState(String replyMessage) {
		if (replyMessage.equalsIgnoreCase(SicsCommunicationConstants.REPLY_OK))
			getChannel().setChannelState(ChannelState.CONNECTED);
	}

	protected void handleConnectedState(String replyMessage) {
		if (replyMessage
				.equalsIgnoreCase(SicsCommunicationConstants.REPLY_LOGIN_OK))
			getChannel().setChannelState(ChannelState.LOGINED);
		else {
			getChannel().setChannelState(ChannelState.LOGIN_FAILED);
		}
	}

	protected void handleLoginedState(String replyMessage) {
		try {
			JSONObject response = new JSONObject(replyMessage);
			String data = response.getString(JSONTag.DATA.getText());
			if (data.equals(SicsCommunicationConstants.REPLY_OK)) {
				getChannel().setChannelState(ChannelState.NORMAL);
			}
		} catch (JSONException e) {
			// getLogger().error("Cannot interpret: " + replyMessage, e);
		}
	}

	protected void handleNormalState(String replyMessage) {
		// Ignore raw messages from broadcast and "config listen"
		// if (!replyMessage.startsWith("{")) {
		// return;
		// }
		try {
			JSONObject response = new JSONObject(replyMessage);
			// Log every output except the xml data
			if (!response.get(JSONTag.OBJECT.getText()).equals("getgumtreexml")) {
				getLogger().info("Server replied: " + replyMessage);
			} else {
				getLogger().info("Server replied getgumtreexml ... ");
			}
			getChannel().handleResponse(response);
		} catch (JSONException e) {
			// getLogger().debug("Cannot interpret: " + replyMessage, e);
		}
	}

	protected BufferedReader getReader() {
		return reader;
	}

	protected InputStream getInput() {
		return input;
	}

	protected void setInput(InputStream input) {
		if (this.input == null) {
			this.input = input;
			reader = new BufferedReader(new InputStreamReader(input));
		}
	}

	protected SicsChannel getChannel() {
		return channel;
	}

	private Logger getLogger() {
		if (logger == null) {
			logger = LoggerFactory.getLogger(SicsSocketListener.class.getName()
					+ ":" + getChannel().getChannelId());
		}
		return logger;
	}

}
