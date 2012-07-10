package org.gumtree.gumnix.sics.internal.control;

import java.util.HashMap;
import java.util.Map;

import org.gumtree.gumnix.sics.control.IHipadabaListener;
import org.gumtree.gumnix.sics.control.ISicsListener;
import org.gumtree.gumnix.sics.control.ISicsMonitor;
import org.gumtree.gumnix.sics.control.IStateMonitorListener;
import org.gumtree.gumnix.sics.control.IStateMonitorListener.SicsMonitorState;
import org.gumtree.gumnix.sics.io.ISicsCallback;
import org.gumtree.gumnix.sics.io.ISicsProxy;
import org.gumtree.gumnix.sics.io.ISicsProxy.ProxyState;
import org.gumtree.gumnix.sics.io.ISicsProxyListener;
import org.gumtree.gumnix.sics.io.ISicsReplyData;
import org.gumtree.gumnix.sics.io.SicsCallbackAdapter;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;
import org.gumtree.util.messaging.EventBuilder;
import org.gumtree.util.messaging.IListenerManager;
import org.gumtree.util.messaging.ListenerManager;
import org.gumtree.util.messaging.SafeListenerRunnable;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SicsMonitor implements ISicsMonitor {

	private static final Logger logger = LoggerFactory.getLogger(SicsMonitor.class);

	private static final String CMD_GLOBAL_NOTIFY = "hnotify / 1";

	private static final String CMD_STATEMON_INTEREST = "statemon interest";

	private static final String CMD_STATEMON_HDB_INTEREST = "statemon hdbinterest";
	
	private static final String CMD_STATUS_INTEREST = "status interest";
	
	private static final String CMD_CONFIG_LISTEN = "config listen 1";

	/*************************************************************************
	 * Listener managers (internal listener storage)
	 *************************************************************************/
	private Map<String, IListenerManager<IHipadabaListener>> hdbListeners;

	private Map<String, IListenerManager<IStateMonitorListener>> stateMonitorListeners;
	
	private IListenerManager<ISicsListener> sicsListeners;

	/*************************************************************************
	 * Callback listeners
	 *************************************************************************/
	private ISicsCallback callback;
	
	private ISicsCallback stateMonCallback;
	
	private ISicsCallback statusInterestCallback;
	
	/*************************************************************************
	 * Sics proxy related
	 *************************************************************************/
	private ISicsProxy proxy;
	
	private ISicsProxyListener proxyListener;
	
	public SicsMonitor(ISicsProxy proxy) {
		this.proxy = proxy;
		hdbListeners = new HashMap<String, IListenerManager<IHipadabaListener>>();
		stateMonitorListeners = new HashMap<String, IListenerManager<IStateMonitorListener>>();
		sicsListeners = new ListenerManager<ISicsListener>();
		// Bind to proxy if connected
		if (proxy.getProxyState().equals(ProxyState.CONNECTED)) {
			bindProxy();
		}
		// Setup proxy listener
		proxyListener = new SicsProxyListenerAdapter() {
			public void proxyConnected() {
				bindProxy();
			}
			public void proxyDisconnected() {
				unbindProxy();
			}
			// Special handler
			public void messageReceived(String message, String channelId) {
				if (channelId.equals(ISicsProxy.CHANNEL_STATUS)) {
					// Handle interrupt event
					if (message.startsWith("INTERRUPT")) {
						try {
							// Get interrupt level
							final int level = Integer.parseInt(message.split(" ")[1]);
							// Call listeners
							sicsListeners.asyncInvokeListeners(
								new SafeListenerRunnable<ISicsListener>() {
									public void run(ISicsListener listener)
											throws Exception {
										listener.interrupted(level);
									}
							});
						} catch (Exception e) {
						}
					}
				}
			}
		};
		proxy.addProxyListener(proxyListener);
	}

	private void bindProxy() {
		callback = new SicsCallbackAdapter() {
			private boolean initialised = false;
			public void receiveReply(ISicsReplyData data) {
				try {
					if(!initialised) {
						initialised = true;
						return;
					}
					JSONObject object = data.getJSONObject();
					if (object == null) {
						logger.warn("Replied data should be a JSON object! (reply:\"" + data.getFullReply().toString() + "\")");
						return;
					}
					String path = (String)object.keys().next();
					// updates entry
//					int endIndex = path.lastIndexOf('/');
					
					final String value = object.getString(path);
					IListenerManager<IHipadabaListener> listenerManager = hdbListeners.get(path);
					if(listenerManager != null) {
						listenerManager.asyncInvokeListeners(new SafeListenerRunnable<IHipadabaListener>() {
							public void run(IHipadabaListener listener) throws Exception {
								listener.valueUpdated(value);
							}
						});
					}
					// [GUMTREE-809] New event bus support
					new EventBuilder(EVENT_TOPIC_HNOTIFY + path).append(EVENT_PROP_VALUE, value).post();
				} catch (JSONException e) {
					logger.error("Cannot interprete reply from SICS in the hnotifty normal callback", e);
				}
			}
		};
		stateMonCallback = new SicsCallbackAdapter() {
			private boolean initialised = false;
			public void receiveReply(ISicsReplyData data) {
				if(!initialised) {
					initialised = true;
					return;
				}
			}
			public void receiveWarning(ISicsReplyData data) {
				logger.debug("Monitor received message");
				String reply = data.getString();
				if(reply != null) {
					final String[] results = reply.split("=");
					if(results.length == 2) {
						final SicsMonitorState state = SicsMonitorState.valueOf(results[0].trim());
						if(state == null) {
							logger.debug("Cannot find state for " + results[0].trim());
							return;
						}
						String message = results[1].trim();
						String[] tokens = message.split("\\s+");
						if(tokens.length >= 1) {
//							getLogger().debug("Monitor token: " + tokens[0]);
							IListenerManager<IStateMonitorListener> listenerManager = stateMonitorListeners.get(tokens[0]);
							if(listenerManager != null) {
								listenerManager.asyncInvokeListeners(new SafeListenerRunnable<IStateMonitorListener>() {
									public void run(final IStateMonitorListener listener) throws Exception {
										logger.debug("Calling listener for state change: " + "[" + results[0].trim() + "]" + results[1].trim());
										listener.stateChanged(state, results[1].trim());												
									}
								});
							}
						}
					}
				} else {
					logger.warn("Empty reply in statemon warning callback");
				}
			}
		};
		statusInterestCallback = new SicsCallbackAdapter() {
			private boolean initialised = false;
			public void receiveReply(ISicsReplyData data) {
				if(!initialised) {
					initialised = true;
					return;
				}
			}
			public void receiveWarning(ISicsReplyData data) {
				try {
					final String status = data.getString().split("=")[1].trim();
					// Use '/' for top level status
					IListenerManager<IHipadabaListener> listenerManager = hdbListeners.get("/");
					if(listenerManager != null) {
						listenerManager.asyncInvokeListeners(new SafeListenerRunnable<IHipadabaListener>() {
							public void run(IHipadabaListener listener) throws Exception {
								listener.valueUpdated(status);
							}
						});
					}
				} catch (Exception e) {
					logger.error("Failed to extract status string.", e);
				}
			}
		};

		try {
			proxy.send(CMD_GLOBAL_NOTIFY, callback, ISicsProxy.CHANNEL_STATUS);
			proxy.send(CMD_STATEMON_INTEREST, stateMonCallback, ISicsProxy.CHANNEL_STATUS);
			proxy.send(CMD_STATEMON_HDB_INTEREST, stateMonCallback, ISicsProxy.CHANNEL_STATUS);
			proxy.send(CMD_STATUS_INTEREST, statusInterestCallback, ISicsProxy.CHANNEL_STATUS);
			proxy.send(CMD_CONFIG_LISTEN, null, ISicsProxy.CHANNEL_STATUS);
		} catch (SicsIOException e) {
			e.printStackTrace();
		}
	}

	private void unbindProxy() {
		// Stop all callback if necessary
		if (callback != null) {
			callback.setCallbackCompleted(true);
			callback = null;
		}
		if (stateMonCallback != null) {
			stateMonCallback.setCallbackCompleted(true);
			stateMonCallback = null;
		}
		if (statusInterestCallback != null) {
			statusInterestCallback.setCallbackCompleted(true);
			statusInterestCallback = null;
		}		
	}
	
	public void addListener(String path, IHipadabaListener listener) {
		IListenerManager<IHipadabaListener> listenerManager = hdbListeners.get(path);
		if(listenerManager == null) {
			listenerManager = new ListenerManager<IHipadabaListener>();
			hdbListeners.put(path, listenerManager);
		}
		listenerManager.addListenerObject(listener);
	}

	public void removeListener(String path, IHipadabaListener listener) {
		IListenerManager<IHipadabaListener> listenerManager = hdbListeners.get(path);
		if(listenerManager != null) {
			listenerManager.removeListenerObject(listener);
		}
	}

	public void addStateMonitor(String sicsObject, IStateMonitorListener listener) {
		IListenerManager<IStateMonitorListener> listenerManager = stateMonitorListeners.get(sicsObject);
		if(listenerManager == null) {
			listenerManager = new ListenerManager<IStateMonitorListener>();
			stateMonitorListeners.put(sicsObject, listenerManager);
		}
		listenerManager.addListenerObject(listener);
	}

	public void removeStateMonitor(String sicsObject, IStateMonitorListener listener) {
		IListenerManager<IStateMonitorListener> listenerManager = stateMonitorListeners.get(sicsObject);
		if(listenerManager != null) {
			listenerManager.removeListenerObject(listener);
		}
	}

	public void addSicsListener(ISicsListener sicsListener) {
		sicsListeners.addListenerObject(sicsListener);
	}
	
	public void removeSicsListener(ISicsListener sicsListener) {
		sicsListeners.removeListenerObject(sicsListener);
	}

}
