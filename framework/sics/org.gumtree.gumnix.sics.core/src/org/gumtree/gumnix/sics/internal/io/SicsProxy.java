package org.gumtree.gumnix.sics.internal.io;

import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.collection.LambdaCollections.with;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.gumtree.core.management.IManageableBean;
import org.gumtree.gumnix.sics.core.SicsCoreProperties;
import org.gumtree.gumnix.sics.core.SicsEvents;
import org.gumtree.gumnix.sics.internal.io.SicsCommunicationConstants.JSONTag;
import org.gumtree.gumnix.sics.io.ISicsCallback;
import org.gumtree.gumnix.sics.io.ISicsChannelMonitor;
import org.gumtree.gumnix.sics.io.ISicsConnectionContext;
import org.gumtree.gumnix.sics.io.ISicsProxy;
import org.gumtree.gumnix.sics.io.ISicsProxyListener;
import org.gumtree.gumnix.sics.io.ISicsProxyWatchdog;
import org.gumtree.gumnix.sics.io.ISicsReplyData;
import org.gumtree.gumnix.sics.io.SicsCallbackAdapter;
import org.gumtree.gumnix.sics.io.SicsExecutionException;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.gumnix.sics.io.SicsRole;
import org.gumtree.util.messaging.EventBuilder;
import org.gumtree.util.messaging.IListenerManager;
import org.gumtree.util.messaging.ListenerManager;
import org.gumtree.util.messaging.SafeListenerRunnable;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.unifr.nio.framework.Dispatcher;

public class SicsProxy implements ISicsProxy {
	
	// heart beat check for every 5 sec
	private static final int HEART_BEAT_PERIOD = 5000;

	private static final int WAIT_INTERVAL = 10;
	
	private static final int SOCKET_TIME_OUT = 1000;
	
	private static final int SOCKET_TRY_INTERVAL = 200;
	
	private static long idCounter = 0;

	private static Logger logger;

	private Map<String, AbstractSicsChannel> channels;

	private ProxyState state;

	private SicsRole role;

	private boolean roleChangedNotify;

	private IListenerManager<ISicsProxyListener> listenerManager;

	private ISicsConnectionContext context;

	private Thread connectionMonitor;
	
	private ISicsProxyWatchdog watchdog;
	
	private Dispatcher dispatcher;
	
	private BlockingQueue<MessageContainer> incomingMessageQueue;
	
	private Thread messageDispatcherThread;

	private String id;
	
	public SicsProxy() {
		id = idCounter++ + "";
		state = ProxyState.DISCONNECTED;
		role = SicsRole.UNDEF;
		roleChangedNotify = false;
		
		// New
		try {
			dispatcher = new Dispatcher();
			dispatcher.start();
		} catch (IOException e) {
			getLogger().error("Failed to create dispatcher.", e);
		}
		incomingMessageQueue = new LinkedBlockingQueue<MessageContainer>();
		messageDispatcherThread = new Thread(new MessageDispatcher(), "SICS Message Dispatcher");
		messageDispatcherThread.start();
	}

	public String getId() {
		return id;
	}
	
	public void disconnect() throws SicsIOException {
		if(state == ProxyState.DISCONNECTED) {
			throw new SicsIOException("Proxy has already been disconnected");
		}
		for(ISicsChannel channel : getChannels().values()) {
			channel.disconnect();
		}
		role = SicsRole.UNDEF;
		context = null;
		channels = null;
		state = ProxyState.DISCONNECTED;
		getListenerManager().asyncInvokeListeners(
				new SafeListenerRunnable<ISicsProxyListener>() {
					public void run(ISicsProxyListener listener)
							throws Exception {
						listener.proxyDisconnected();
					}
				});
		new EventBuilder(SicsEvents.Proxy.TOPIC_DISCONNECTED)
				.append(SicsEvents.Proxy.PROXY, this)
				.append(SicsEvents.Proxy.PROXY_ID, getId()).post();
		connectionMonitor = null;
		
		// New
		incomingMessageQueue.clear();
	}

	public SicsRole getCurrentRole() {
		return role;
	}

	private void setCurrentRole(SicsRole role) {
		this.role = role;
	}

	public synchronized void changeRole(final SicsRole role, String password) throws SicsExecutionException, SicsIOException {
		if(getProxyState().equals(ProxyState.DISCONNECTED)) {
			throw new SicsIOException("Has not yet been contacted to the server.");
		}
		if(getCurrentRole().equals(role)) {
			return;
		}
		roleChangedNotify = false;
		int timeCount = 0;
		ISicsCallback callback = new SicsCallbackAdapter() {
			public void receiveWarning(ISicsReplyData data) {
				setCallbackCompleted(true);
				setCurrentRole(role);
				roleChangedNotify = true;
			}
			public void receiveError(ISicsReplyData data) {
				roleChangedNotify = true;
				setCallbackCompleted(true);
			}
		};
		send("config rights " + role.getLoginId() + " " + password, callback);
		while(!roleChangedNotify) {
			if(timeCount > SicsCoreProperties.PROXY_TIMEOUT.getLong()) {
				throw new SicsIOException("Cannot change role due to time out.");
			}
			try {
				Thread.sleep(WAIT_INTERVAL);
				timeCount += WAIT_INTERVAL;
			} catch (InterruptedException e) {
				getLogger().error("Error using connecting to SICS.", e);
			}
		}
		if(getCurrentRole().equals(role)) {
			throw new SicsExecutionException("Cannot change role.");
		}
	}

	public void send(String command, ISicsCallback callback) throws SicsIOException {
		send(command, callback, CHANNEL_GENERAL);
	}

	public void send(String command, ISicsCallback callback, String channelId) throws SicsIOException {
		if(getProxyState() !=  ProxyState.CONNECTED) {
			getLogger().info("Proxy activatation requested.");
			for(final ISicsProxyListener listener : getListenerManager().getListeners()) {
				// do not use thread
				// this will give us more expected result
				SafeRunner.run(new ISafeRunnable() {
					public void handleException(Throwable exception) {
						getLogger().error("Error occurred in proxy activation notification.", exception);
					}
					public void run() throws Exception {
						listener.proxyConnectionReqested();
					}
				});
			}
			throw new SicsIOException("Not connected");
		}
//		getChannels().get(channelId).send(command, callback);
		AbstractSicsChannel channel = getChannels().get(channelId);
		if (channel == null) {
			channel = getChannels().get(CHANNEL_GENERAL);
		}
		channel.send(command, callback);
	}

	public void login(ISicsConnectionContext context) throws SicsExecutionException, SicsIOException {
		if(getProxyState() != ProxyState.DISCONNECTED) {
			throw new SicsIOException("Server has already been connected.");
		}
		// New switch between blocking and non-blocking I/O
		AbstractSicsChannel generalChannel = null;
		AbstractSicsChannel statusChannel = null;
//		AbstractSicsChannel batchChannel = null;
//		AbstractSicsChannel scanChannel = null;
		if (SicsCoreProperties.USE_NON_NIO_CHANNEL.getBoolean()) {
			generalChannel = new SicsChannel(CHANNEL_GENERAL, this);
			statusChannel = new SicsChannel(CHANNEL_STATUS, this);
//			batchChannel = new SicsChannel(CHANNEL_BATCH, this);
//			scanChannel = new SicsChannel(CHANNEL_SCAN, this);			
		} else {
			generalChannel = new SicsNonBlockingChannel(CHANNEL_GENERAL, this, true);
			statusChannel = new SicsNonBlockingChannel(CHANNEL_STATUS, this, true);
//			batchChannel = new SicsNonBlockingChannel(CHANNEL_BATCH, this);
//			scanChannel = new SicsNonBlockingChannel(CHANNEL_SCAN, this);
		}
//		ISicsChannel zipDataChannel = new SicsZipDataChannel(CHANNEL_ZIP_DATA, this);
		// Channel for new batch buffer
//		AbstractSicsChannel rawBatchChannel = new SicsRawChannel(CHANNEL_RAW_BATCH, this);
		SicsNonBlockingChannel rawBatchChannel = new SicsNonBlockingChannel(CHANNEL_RAW_BATCH, this, false);
		generalChannel.login(context);
		statusChannel.login(context);
//		batchChannel.login(context);
//		zipDataChannel.login(context);
//		scanChannel.login(context);
		rawBatchChannel.login(context);

		getChannels().put(CHANNEL_GENERAL, generalChannel);
		getChannels().put(CHANNEL_STATUS, statusChannel);
//		getChannels().put(CHANNEL_BATCH, batchChannel);
//		getChannels().put(CHANNEL_ZIP_DATA, zipDataChannel);
//		getChannels().put(CHANNEL_SCAN, scanChannel);
		getChannels().put(CHANNEL_RAW_BATCH, rawBatchChannel);

		role = context.getRole();
		state = ProxyState.CONNECTED;

		this.context = context;

		getLogger().info("Sics connected");
		getListenerManager().asyncInvokeListeners(
				new SafeListenerRunnable<ISicsProxyListener>() {
					public void run(ISicsProxyListener listener)
							throws Exception {
						listener.proxyConnected();
					}
				});
		new EventBuilder(SicsEvents.Proxy.TOPIC_CONNECTED)
				.append(SicsEvents.Proxy.PROXY, this)
				.append(SicsEvents.Proxy.PROXY_ID, getId()).post();
		getLogger().info("Sics listeners for notifying connection openned are all ready to run.");

		// [GUMTREE-70] since Java socket does not notify and check for disconnection
		// a new heart beat monitor is used to check network availability.
		// This does not care if the network takes very long time to setup
		connectionMonitor = new Thread(new Runnable() {
			public void run() {
				while(getProxyState().equals(ProxyState.CONNECTED)) {
					Socket socket = null;
					int counter = 0;
					while(socket == null && counter <= SOCKET_TIME_OUT) {
						try {
							socket = new Socket(getConnectionContext().getHost(), getConnectionContext().getPort());
						} catch (IOException ioe) {
							counter += SOCKET_TRY_INTERVAL;
							try {
								Thread.sleep(SOCKET_TRY_INTERVAL);
							} catch (InterruptedException e) {
							}
						} 
					}
					if (socket == null) {
						getLogger().info("SICS proxy needs to disconnect due to network error");
						try {
							// disconnect the proxy if network is unavailable
							disconnect();
						} catch (SicsIOException e) {
							getLogger().error("Error in proxy disconnection.", e);
						}
					} else {
						try {
							socket.close();
							socket = null;
						} catch (IOException e) {
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

	public ISicsConnectionContext getConnectionContext() {
		return context;
	}

	public ProxyState getProxyState() {
		return state;
	}

	public boolean isConnected() {
		return getProxyState().equals(ProxyState.CONNECTED);
	}
	
	public void addProxyListener(ISicsProxyListener listener) {
		getListenerManager().addListenerObject(listener);
	}

	public void removeProxyListener(ISicsProxyListener listener) {
		getListenerManager().removeListenerObject(listener);
	}

	protected IListenerManager<ISicsProxyListener> getListenerManager() {
		if(listenerManager == null) {
			listenerManager = new ListenerManager<ISicsProxyListener>();
		}
		return listenerManager;
	}

	private Map<String, AbstractSicsChannel> getChannels() {
		if(channels == null) {
			channels = new LinkedHashMap<String, AbstractSicsChannel>();
		}
		return channels;
	}
	
	public ISicsChannelMonitor[] getChannelMonitors() {
		return with(getChannels().values()).extract(
				on(ISicsChannel.class).getMonitor()).toArray(
				ISicsChannelMonitor.class);
	}

	private static Logger getLogger() {
		if(logger == null) {
			logger = LoggerFactory.getLogger(SicsProxy.class);
		}
		return logger;
	}

	public String[] getConnectedChannelIds() {
		return getChannels().keySet().toArray(new String[getChannels().keySet().size()]);
	}
	
	public ISicsProxyWatchdog getWatchdog() {
		return watchdog;
	}

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
					// Wait for message
					MessageContainer messageContainer = null;
					if (getProxyState().equals(ProxyState.CONNECTED) && watchdog != null) {
						long timeout = watchdog.getTimeout();
						// with timeout in connected mode
						messageContainer = incomingMessageQueue.poll(timeout, TimeUnit.MILLISECONDS);
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
					AbstractSicsChannel channel = getChannels().get(messageContainer.getChannelId());
					// Unknown message or channel is not ready
					if (channel == null) {
						continue;
					}
					
					// Broadcast message
					getListenerManager().asyncInvokeListeners(
							new SafeListenerRunnable<ISicsProxyListener>() {
								public void run(ISicsProxyListener listener)
										throws Exception {
									listener.messageReceived(replyMessage, channelId);
								}
							});
					
					// Wrap as JSON object
					try {
						JSONObject response = new JSONObject(replyMessage);
						// Log every output except the xml data
						if (!response.get(JSONTag.OBJECT.getText()).equals("getgumtreexml")) {
							getLogger().info("Server replied: " + replyMessage);
						} else {
							getLogger().info("Server replied getgumtreexml ... ");
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

	public IManageableBean getManageableBean() {
		return null;
	}

	public String getRegistrationKey() {
		return "org.gumtree.gumnix.sics:type=SicsProxy";
	}
	
}
