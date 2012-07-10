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

import java.io.IOException;
import java.util.StringTokenizer;

import org.gumtree.sics.io.SicsCommunicationConstants;
import org.gumtree.sics.io.ISicsChannel.ChannelState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SicsZipDataSocketListener extends SicsSocketListener {

	private static final int BUFFER_SIZE = 32768;

	private Logger logger;

	private int tempTransId = 1;

	private int expectedBinaryByte;

	private byte[] buffer;

	private byte[] binaryBuffer;

	private int binaryBufferCount;

	protected SicsZipDataSocketListener(SicsChannel channel) {
		super(channel);
		buffer = new byte[BUFFER_SIZE];
	}

	public void run() {
		getLogger().info("Listener has been started");
		while (true) {
			try {
				int byteRead = getInput().read(buffer);
				if (!getChannel().getChannelState().equals(ChannelState.NORMAL)) {
					//
					String replyMessage = new String(buffer, 0, 20);
					if (replyMessage
							.startsWith(SicsCommunicationConstants.REPLY_OK)) {
						getLogger().debug(
								"Server replied: "
										+ SicsCommunicationConstants.REPLY_OK);
						getChannel().messageRecieved(
								SicsCommunicationConstants.REPLY_OK);
						getChannel().setChannelState(ChannelState.CONNECTED);
					} else if (replyMessage
							.startsWith(SicsCommunicationConstants.REPLY_LOGIN_OK)) {
						getLogger()
								.debug("Server replied: "
										+ SicsCommunicationConstants.REPLY_LOGIN_OK);
						getChannel().messageRecieved(
								SicsCommunicationConstants.REPLY_LOGIN_OK);
						getChannel().setChannelState(ChannelState.LOGINED);
					} else if (replyMessage
							.startsWith(SicsCommunicationConstants.REPLY_BAD_LOGIN)) {
						getLogger()
								.debug("Server replied: "
										+ SicsCommunicationConstants.REPLY_BAD_LOGIN);
						getChannel().messageRecieved(
								SicsCommunicationConstants.REPLY_BAD_LOGIN);
						getChannel().setChannelState(ChannelState.LOGIN_FAILED);
					}
					continue;
				} else {
					// Normal state
					String replyMessage = new String(buffer, 0, 100);
					if (replyMessage.startsWith("SICSBIN")) {
						getLogger().debug(
								"Server replied: " + replyMessage.trim());
						getChannel().messageRecieved(replyMessage.trim());
						StringTokenizer st = new StringTokenizer(replyMessage);
						st.nextToken();
						// Type
						st.nextToken();
						// Name
						st.nextToken();
						expectedBinaryByte = Integer.parseInt(st.nextToken());
						binaryBuffer = new byte[expectedBinaryByte];
						binaryBufferCount = 0;
					} else if (binaryBuffer != null) {
						if (binaryBufferCount < expectedBinaryByte) {
							System.arraycopy(buffer, 0, binaryBuffer,
									binaryBufferCount, byteRead);
							binaryBufferCount += byteRead;
							if (binaryBufferCount == expectedBinaryByte) {
								byte[] zipdata = new byte[expectedBinaryByte];
								System.arraycopy(binaryBuffer, 0, zipdata, 0,
										expectedBinaryByte);
								getZipDataChannel().handleResponse(zipdata,
										tempTransId++);
								binaryBuffer = null;
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private SicsZipDataChannel getZipDataChannel() {
		return (SicsZipDataChannel) getChannel();
	}

	private Logger getLogger() {
		if (logger == null) {
			logger = LoggerFactory.getLogger(SicsZipDataSocketListener.class
					.getName() + ":" + getChannel().getChannelId());
		}
		return logger;
	}

}
