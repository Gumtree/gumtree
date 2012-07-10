package org.gumtree.sics.core.support;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.gumtree.sics.core.ISicsMonitor;
import org.gumtree.sics.core.SicsMonitorState;
import org.gumtree.sics.io.ISicsCallback;
import org.gumtree.sics.io.ISicsData;
import org.gumtree.sics.io.ISicsProxy;
import org.gumtree.sics.io.SicsCallbackAdapter;
import org.gumtree.sics.io.SicsEventBuilder;
import org.gumtree.sics.io.SicsEventHandler;
import org.gumtree.sics.io.SicsIOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SicsMonitor implements ISicsMonitor {

	private static final Logger logger = LoggerFactory
			.getLogger(SicsMonitor.class);

	private static final String CMD_GLOBAL_NOTIFY = "hnotify / 1";

	private static final String CMD_STATEMON_INTEREST = "statemon interest";

	private static final String CMD_STATEMON_HDB_INTEREST = "statemon hdbinterest";

	private static final String CMD_STATUS_INTEREST = "status interest";

	private static final String CMD_CONFIG_LISTEN = "config listen 1";

	private SicsEventHandler proxyEventHandler;

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

	public SicsMonitor() {
		// Setup proxy listener
		proxyEventHandler = new SicsEventHandler(ISicsProxy.EVENT_TOPIC_PROXY_ALL) {
			@Override
			public void handleSicsEvent(Event event) {
				if (getTopic(event).equals(
						ISicsProxy.EVENT_TOPIC_PROXY_STATE_CONNECTED)) {
					bindProxy();
				} else if (getTopic(event).equals(
						ISicsProxy.EVENT_TOPIC_PROXY_STATE_DISCONNECTED)) {
					unbindProxy();
				} else if (getTopic(event).equals(
						ISicsProxy.EVENT_TOPIC_PROXY_MESSAGE_RECEIVED)) {
					// Handle interrupt event
					String message = getString(event,
							ISicsProxy.EVENT_PROP_MESSAGE);
					if (message.startsWith("INTERRUPT")) {
						try {
							// Get interrupt level
							final int level = Integer.parseInt(message
									.split(" ")[1]);
							// Call listeners
							new SicsEventBuilder(EVENT_TOPIC_INTERRUPT,
									getProxy().getId()).append(
									EVENT_PROP_INTERRUPT_LEVEL, level).post();
						} catch (Exception e) {
						}
					}
				}
			}
		};
	}

	private void bindProxy() {
		if(getProxy() == null && getProxy().isConnected()) {
			return;
		}
		callback = new SicsCallbackAdapter() {
			private boolean initialised = false;

			public void receiveReply(ISicsData data) {
				try {
					if (!initialised) {
						initialised = true;
						return;
					}
					JSONObject object = data.getJSONObject();
					if (object == null) {
						logger.warn("Replied data should be a JSON object! (reply:\""
								+ data.getOriginal().toString() + "\")");
						return;
					}
					String path = (String) object.keys().next();
					// updates entry
					// int endIndex = path.lastIndexOf('/');

					final String value = object.getString(path);
					new SicsEventBuilder(EVENT_TOPIC_HNOTIFY + path, getProxy()
							.getId()).append(EVENT_PROP_VALUE, value).post();
				} catch (JSONException e) {
					logger.error(
							"Cannot interprete reply from SICS in the hnotifty normal callback",
							e);
				}
			}
		};
		stateMonCallback = new SicsCallbackAdapter() {
			private boolean initialised = false;

			public void receiveReply(ISicsData data) {
				if (!initialised) {
					initialised = true;
					return;
				}
			}

			public void receiveWarning(ISicsData data) {
				logger.debug("Monitor received message");
				String reply = data.getString();
				if (reply != null) {
					final String[] results = reply.split("=");
					if (results.length == 2) {
						final SicsMonitorState state = SicsMonitorState
								.valueOf(results[0].trim());
						if (state == null) {
							logger.debug("Cannot find state for "
									+ results[0].trim());
							return;
						}
						String message = results[1].trim();
						String[] tokens = message.split("\\s+");
						if (tokens.length >= 1) {
							new SicsEventBuilder(EVENT_TOPIC_STATEMON + "/"
									+ tokens[0], getProxy().getId())
									.append(EVENT_PROP_STATE, state)
									.append(EVENT_PROP_MESSAGE, message).post();								
						}
					}
				} else {
					logger.warn("Empty reply in statemon warning callback");
				}
			}
		};
		statusInterestCallback = new SicsCallbackAdapter() {
			private boolean initialised = false;

			public void receiveReply(ISicsData data) {
				if (!initialised) {
					initialised = true;
					return;
				}
			}

			public void receiveWarning(ISicsData data) {
				try {
					final String status = data.getString().split("=")[1].trim();
					new SicsEventBuilder(EVENT_TOPIC_SERVER, getProxy().getId())
							.append(EVENT_PROP_STATUS, status).post();
				} catch (Exception e) {
					logger.error("Failed to extract status string.", e);
				}
			}
		};

		try {
			getProxy().send(CMD_GLOBAL_NOTIFY, callback, ISicsProxy.CHANNEL_STATUS);
			getProxy().send(CMD_STATEMON_INTEREST, stateMonCallback,
					ISicsProxy.CHANNEL_STATUS);
			getProxy().send(CMD_STATEMON_HDB_INTEREST, stateMonCallback,
					ISicsProxy.CHANNEL_STATUS);
			getProxy().send(CMD_STATUS_INTEREST, statusInterestCallback,
					ISicsProxy.CHANNEL_STATUS);
			getProxy().send(CMD_CONFIG_LISTEN, null, ISicsProxy.CHANNEL_STATUS);
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

	@Override
	public ISicsProxy getProxy() {
		return proxy;
	}

	@Override
	@Inject
	public void setProxy(ISicsProxy proxy) {
		// Unbind previous proxy
		if (this.proxy != null) {
			unbindProxy();
			proxyEventHandler.deactivate();
		}
		this.proxy = proxy;
		if (this.proxy != null) {
			// Bind to proxy if connected
			if (proxy.isConnected()) {
				bindProxy();
			}
			proxyEventHandler.setProxyId(proxy.getId()).activate();
		}
	}

	@Override
	@PreDestroy
	public void disposeObject() {
		if (getProxy() != null) {
			setProxy(null);
		}
		if (proxyEventHandler != null) {
			proxyEventHandler.deactivate();
			proxyEventHandler = null;
		}
		callback = null;
		stateMonCallback = null;
		statusInterestCallback = null;
	}

}
