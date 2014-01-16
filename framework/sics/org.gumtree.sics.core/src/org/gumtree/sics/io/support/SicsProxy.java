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

import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.collection.LambdaCollections.with;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.gumtree.sics.core.PropertyConstants;
import org.gumtree.sics.io.ISicsCallback;
import org.gumtree.sics.io.ISicsChannel;
import org.gumtree.sics.io.ISicsChannelMonitor;
import org.gumtree.sics.io.ISicsConnectionContext;
import org.gumtree.sics.io.ISicsData;
import org.gumtree.sics.io.ISicsProxy;
import org.gumtree.sics.io.ISicsProxyWatchdog;
import org.gumtree.sics.io.SicsCallbackAdapter;
import org.gumtree.sics.io.SicsCommunicationConstants.JSONTag;
import org.gumtree.sics.io.SicsEventBuilder;
import org.gumtree.sics.io.SicsExecutionException;
import org.gumtree.sics.io.SicsIOException;
import org.gumtree.sics.io.SicsRole;
import org.gumtree.sics.util.SicsCoreProperties;
import org.gumtree.sics.util.SicsIOUtils;
import org.gumtree.util.string.StringProvider;
import org.gumtree.util.string.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.unifr.nio.framework.Dispatcher;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

public class SicsProxy implements ISicsProxy {

	private static final Logger logger = LoggerFactory.getLogger(SicsProxy.class);
	
	// heart beat check for every 5 sec
	private static final int HEART_BEAT_PERIOD = 5000;

	private static final int WAIT_INTERVAL = 10;

	private String id;
	
	private Map<String, AbstractSicsChannel> channels;

	private ProxyState state;

	private SicsRole role;

	private boolean roleChangedNotify;

	private ISicsConnectionContext context;

	private Thread connectionMonitor;

	private ISicsProxyWatchdog watchdog;

	private Thread messageDispatcherThread;
	
	private Dispatcher dispatcher;

	private BlockingQueue<MessageContainer> incomingMessageQueue;
	
	public SicsProxy() {
		id = "default";
		state = ProxyState.DISCONNECTED;
		role = SicsRole.UNDEF;
		roleChangedNotify = false;

		// New
		try {
			dispatcher = new Dispatcher();
			dispatcher.start();
		} catch (IOException e) {
			logger.error("Failed to create dispatcher.", e);
		}
		incomingMessageQueue = new LinkedBlockingQueue<MessageContainer>();
		messageDispatcherThread = new Thread(new MessageDispatcher(), "SICS Message Dispatcher");
		messageDispatcherThread.start();
	}

	/*************************************************************************
	 * Attributes
	 *************************************************************************/
	
	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public void setId(String id) {
		this.id = id;
	}

	/*************************************************************************
	 * I/O
	 *************************************************************************/
	
	@Override
	public void login(ISicsConnectionContext context)
			throws SicsExecutionException, SicsIOException {
		if (getProxyState() != ProxyState.DISCONNECTED) {
			throw new SicsIOException("Server has already been connected.");
		}

		this.context = context;

		// New switch between blocking and non-blocking I/O
		AbstractSicsChannel generalChannel = new SicsNonBlockingChannel(
				CHANNEL_GENERAL, this);
		AbstractSicsChannel statusChannel = new SicsNonBlockingChannel(
				CHANNEL_STATUS, this);
//		AbstractSicsChannel batchChannel = new SicsNonBlockingChannel(
//				CHANNEL_BATCH, this);
//		AbstractSicsChannel scanChannel = new SicsNonBlockingChannel(
//				CHANNEL_SCAN, this);
		// [Tony][2012-02-09] Raw channel hangs on Mac!?
//		AbstractSicsChannel rawBatchChannel = new SicsRawChannel(
//				CHANNEL_RAW_BATCH, this);
		
		generalChannel.login(context);
		statusChannel.login(context);
//		batchChannel.login(context);
//		scanChannel.login(context);
//		rawBatchChannel.login(context);

		getChannels().put(CHANNEL_GENERAL, generalChannel);
		getChannels().put(CHANNEL_STATUS, statusChannel);
//		getChannels().put(CHANNEL_BATCH, batchChannel);
//		getChannels().put(CHANNEL_SCAN, scanChannel);
//		getChannels().put(CHANNEL_RAW_BATCH, rawBatchChannel);

		role = context.getRole();

		// [GUMTREE-70] since Java socket does not notify and check for
		// disconnection
		// a new heart beat monitor is used to check network availability.
		// This does not care if the network takes very long time to setup
		connectionMonitor = new Thread(new Runnable() {
			public void run() {
				while (getProxyState().equals(ProxyState.CONNECTED)) {
					Socket socket = null;
					try {
						// System.out.println("Checking heat beat...");
						socket = new Socket(getConnectionContext().getHost(),
								getConnectionContext().getPort());
					} catch (IOException ioe) {
						try {
							// disconnect the proxy if network is unavailable
							disconnect();
						} catch (SicsIOException e) {
							logger.error("Error in proxy disconnection.",
									e);
						}
					} finally {
						// clean up
						if (socket != null) {
							try {
								socket.close();
								socket = null;
							} catch (IOException e) {
							}
						}
					}
					try {
						Thread.sleep(HEART_BEAT_PERIOD);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				try {
					if (Boolean.valueOf(System.getProperty(PropertyConstants.SICS_KEEP_CONNECTION))){
						reconnect();
					}
				} catch (Exception e) {
				}
			}

		});
		connectionMonitor.start();
		
		setProxyState(ProxyState.CONNECTED);
		logger.info("Sics connected");
	}

	private void reconnect() {
		connectionMonitor = new Thread(new Runnable() {
			public void run() {
				while (getProxyState().equals(ProxyState.DISCONNECTED)) {
					Socket socket = null;
					try {
						System.err.println("check connection");
						// System.out.println("Checking heat beat...");
						socket = new Socket(getConnectionContext().getHost(),
								getConnectionContext().getPort());
						System.err.println("try reconnect");
						send("status", null, CHANNEL_GENERAL);
					} catch (IOException ioe) {
					} finally {
						// clean up
						if (socket != null) {
							try {
								socket.close();
								socket = null;
							} catch (IOException e) {
							}
						}
					}
					try {
						Thread.sleep(HEART_BEAT_PERIOD);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}

		});
		connectionMonitor.start();
	}

	@Override
	public void disconnect() throws SicsIOException {
		if (state == ProxyState.DISCONNECTED) {
			throw new SicsIOException("Proxy has already been disconnected");
		}
		for (ISicsChannel channel : getChannels().values()) {
			channel.disconnect();
		}
		role = SicsRole.UNDEF;
//		context = null;
		channels = null;
		connectionMonitor = null;
		incomingMessageQueue.clear();
		setProxyState(ProxyState.DISCONNECTED);
	}
	
	@Override
	public void send(String command, ISicsCallback callback)
			throws SicsIOException {
		send(command, callback, CHANNEL_GENERAL);
	}

	@Override
	public void send(String command, ISicsCallback callback, String channelId)
			throws SicsIOException {
		// Auto login if possible
		if (getProxyState() != ProxyState.CONNECTED) {
			if (SicsCoreProperties.LOGIN_MODE.getValue().equals("auto")) {
				try {
					login(SicsIOUtils.createContextFromSystemProperties());
				} catch (SicsExecutionException e) {
					logger.error("Failed to auto login", e);
				}
			}
		}
		if (getProxyState() != ProxyState.CONNECTED) {
			logger.info("Proxy activatation requested.");
			new SicsEventBuilder(EVENT_TOPIC_PROXY_STATE_ACTIVATION_REQUESTED,
					getId()).post();
			throw new SicsIOException("Not connected");
		}
//		getChannels().get(channelId).send(command, callback);
		AbstractSicsChannel channel = getChannels().get(channelId);
		if (channel == null) {
			channel = getChannels().get(CHANNEL_GENERAL);
		}
		channel.send(command, callback);
	}
	
	public ISicsConnectionContext getConnectionContext() {
		return context;
	}
	
	@Override
	public SicsRole getCurrentRole() {
		return role;
	}

	private void setCurrentRole(SicsRole role) {
		this.role = role;
	}

	@Override
	public synchronized void changeRole(final SicsRole role, String password)
			throws SicsExecutionException, SicsIOException {
		if (getProxyState().equals(ProxyState.DISCONNECTED)) {
			throw new SicsIOException(
					"Has not yet been contacted to the server.");
		}
		if (getCurrentRole().equals(role)) {
			return;
		}
		roleChangedNotify = false;
		int timeCount = 0;
		ISicsCallback callback = new SicsCallbackAdapter() {
			public void receiveWarning(ISicsData data) {
				setCallbackCompleted(true);
				setCurrentRole(role);
				roleChangedNotify = true;
			}

			public void receiveError(ISicsData data) {
				roleChangedNotify = true;
				setCallbackCompleted(true);
			}
		};
		send("config rights " + role.getLoginId() + " " + password, callback);
		while (!roleChangedNotify) {
			if (timeCount > SicsCoreProperties.PROXY_TIMEOUT.getLong()) {
				throw new SicsIOException("Cannot change role due to time out.");
			}
			try {
				Thread.sleep(WAIT_INTERVAL);
				timeCount += WAIT_INTERVAL;
			} catch (InterruptedException e) {
				logger.error("Error using connecting to SICS.", e);
			}
		}
		if (getCurrentRole().equals(role)) {
			throw new SicsExecutionException("Cannot change role.");
		}
	}

	private Map<String, AbstractSicsChannel> getChannels() {
		if (channels == null) {
			channels = new LinkedHashMap<String, AbstractSicsChannel>();
		}
		return channels;
	}
	
	@Override
	public ISicsChannelMonitor[] getChannelMonitors() {
		return with(getChannels().values()).extract(
				on(ISicsChannel.class).getMonitor()).toArray(
				ISicsChannelMonitor.class);
	}

	@Override
	public String[] getConnectedChannelIds() {
		return getChannels().keySet().toArray(
				new String[getChannels().keySet().size()]);
	}
	
	/*************************************************************************
	 * Status
	 *************************************************************************/
	
	@Override
	public ProxyState getProxyState() {
		return state;
	}
	
	protected void setProxyState(ProxyState state) {
		this.state = state;
		if (state.equals(ProxyState.CONNECTED)) {
			new SicsEventBuilder(EVENT_TOPIC_PROXY_STATE_CONNECTED, getId()).post();
		} else if (state.equals(ProxyState.DISCONNECTED)) {
			new SicsEventBuilder(EVENT_TOPIC_PROXY_STATE_DISCONNECTED, getId()).post();
		}
	}

	@Override
	public boolean isConnected() {
		return getProxyState().equals(ProxyState.CONNECTED);
	}

	@Override
	public ISicsProxyWatchdog getWatchdog() {
		return watchdog;
	}

	@Override
	public void setWatchdog(ISicsProxyWatchdog watchdog) {
		this.watchdog = watchdog;
	}

	/*************************************************************************
	 * New methods to support new I/O channel
	 *************************************************************************/
	
	protected Dispatcher getDispatcher() {
		return dispatcher;
	}

	protected BlockingQueue<MessageContainer> getIncomingMessageQueue() {
		return incomingMessageQueue;
	}

	// Central message dispatcher
	class MessageDispatcher implements Runnable {
		public void run() {
			boolean stalledNotified = false;
			while (true) {
				try {
					if (getProxyState().equals(ProxyState.DISPOSED)) {
						return;
					}
					// Wait for message
					MessageContainer messageContainer = null;
					if (getProxyState().equals(ProxyState.CONNECTED)
							&& watchdog != null) {
						long timeout = watchdog.getTimeoutInSecond();
						// with timeout in connected mode
						messageContainer = incomingMessageQueue.poll(timeout,
								TimeUnit.SECONDS);
					} else {
						// without timeout in disconnected mode
						messageContainer = incomingMessageQueue.take();
					}

					// Check if SICS is stalled
					if (messageContainer == null) {
						// Send warning out once (ignore multiple trigger)
						if (watchdog != null && !stalledNotified) {
							watchdog.notifySicsStalled();
							stalledNotified = true;
						}
						continue;
					} else {
						// No need to report stall again
						stalledNotified = false;
					}

					final String replyMessage = messageContainer.getMessage();
					final String channelId = messageContainer.getChannelId();
					AbstractSicsChannel channel = getChannels().get(
							messageContainer.getChannelId());
					// Unknown message or channel is not ready
					if (channel == null) {
						continue;
					}

					// Broadcast message
					new SicsEventBuilder(EVENT_TOPIC_PROXY_MESSAGE_RECEIVED, getId())
							.append(EVENT_PROP_CHANNEL, channelId)
							.append(EVENT_PROP_MESSAGE, replyMessage).post();

					// Wrap as JSON object
					try {
						JSONObject response = new JSONObject(replyMessage);
						// Log every output except the xml data
						if (!response.get(JSONTag.OBJECT.getText()).equals(
								"getgumtreexml")) {
							logger.info("Server replied: " + replyMessage);
						} else {
							logger.info(
									"Server replied getgumtreexml ... ");
						}
						// Let individual channel to handle
						channel.handleResponse(response);
					} catch (JSONException e) {
						channel.nonJSONMessage++;
					} catch (Exception e) {
					}
				} catch (InterruptedException e) {
				}
			}
		}
	}

	/*************************************************************************
	 * Object life cycle
	 *************************************************************************/
	
	@Override
	@PreDestroy
	public void disposeObject() {
		state = ProxyState.DISPOSED;
		if (isConnected()) {
			for (ISicsChannel channel : getChannels().values()) {
				channel.disconnect();
			}
		}
		if (dispatcher != null) {
			// Is there a better way to clean the dispatcher?
//			dispatcher.stop();
			dispatcher = null;
		}
		if (incomingMessageQueue != null) {
			incomingMessageQueue.clear();
			incomingMessageQueue = null;
		}
		if (messageDispatcherThread != null) {
//			messageDispatcherThread.stop();
			messageDispatcherThread = null;
		}
		if (channels != null) {
			channels.clear();
			channels = null;
		}
		role = null;
		context = null;
		connectionMonitor = null;
		watchdog = null;
		logger.info("SicsProxy {} has been disposed.", getId());
	}
	
	/*************************************************************************
	 * Utilities
	 *************************************************************************/
	
	@Override
	public String toString() {
		ToStringHelper toStringHelper = Objects.toStringHelper(this)
				.add("id", getId()).add("state", getProxyState());
		String channels = StringUtils.formatIterable(getChannels().keySet(),
				new StringProvider());
		toStringHelper.add("channels", channels);
		return toStringHelper.toString();
	}

}
