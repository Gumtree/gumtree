package org.gumtree.gumnix.sics.internal.io;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gumtree.gumnix.sics.core.SicsCoreProperties;
import org.gumtree.gumnix.sics.internal.io.SicsCommunicationConstants.Flag;
import org.gumtree.gumnix.sics.internal.io.SicsCommunicationConstants.JSONTag;
import org.gumtree.gumnix.sics.io.ISicsCallback;
import org.gumtree.gumnix.sics.io.ISicsConnectionContext;
import org.gumtree.gumnix.sics.io.ISicsProxyListener;
import org.gumtree.gumnix.sics.io.SicsExecutionException;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.gumnix.sics.io.SicsReplyData;
import org.gumtree.util.messaging.SafeListenerRunnable;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SicsChannel extends AbstractSicsChannel {

	private static final int WAIT_INTERVAL = 10;

	private Logger logger;

	private Socket socket;

	private PrintStream proxyOutput;

	private Thread sicsSocketListener;

	private Map<Integer, ISicsCallback> callbackMap;

	protected SicsChannel(String channelId, SicsProxy sicsProxy) {
		super(channelId, sicsProxy);
	}

	public void disconnect() throws SicsIOException {
		if(getChannelState() == ChannelState.DISCONNECTED) {
			throw new SicsIOException("Channel communication has already been disconnected.");
		}
		setChannelState(ChannelState.DISCONNECTED);
		transIdCount = 1;
	}

	public void login(ISicsConnectionContext context) throws SicsExecutionException, SicsIOException {
		if(getChannelState() != ChannelState.DISCONNECTED) {
			throw new SicsIOException("Channel communication has already been established.");
		}

		// STEP 1: Open socket (state: DISCONNECTED -> CONNECTING -> CONNECTED)
		openSocket(context, new SicsSocketListener(this));
		getLogger().info("Connected to SICS");

		// STEP 2: Login (state: CONNECTED -> LOGINED)
		setLogin(context);
		getLogger().info("Logined to SICS as " + context.getRole());

		// STEP 3: Get protocol to JSON (state: LOGINED -> NORMAL)
		setJSONProtocol();
		getLogger().info("Channel to SICS is ready.");

	}

	protected void openSocket(ISicsConnectionContext context, SicsSocketListener listener) throws SicsIOException {
		try {
			setChannelState(ChannelState.CONNECTING);
			socket = new Socket(context.getHost(), context.getPort());
			proxyOutput = new PrintStream(socket.getOutputStream());
			listener.setInput(socket.getInputStream());
			sicsSocketListener = new Thread(listener);
			sicsSocketListener.start();
			sicsSocketListener.setPriority(Thread.MAX_PRIORITY);
			int timeCount = 0;
			while(true) {
				if(getChannelState() == ChannelState.CONNECTING) {
					if(timeCount > SicsCoreProperties.PROXY_TIMEOUT.getLong()) {
						handleConnectionError();
						throw new SicsIOException("Time out on connecting to SICS.");
					}
					try {
						Thread.sleep(WAIT_INTERVAL);
						timeCount += WAIT_INTERVAL;
					} catch (InterruptedException e) {
						getLogger().error("Error using connecting to SICS.", e);
					}
				} else if (getChannelState() == ChannelState.CONNECTED){
					break;
				} else {
					handleConnectionError();
					throw new SicsIOException("Error on connecting to SICS.");
				}
			}
		} catch (UnknownHostException e) {
			throw new SicsIOException("Cannot connect to SICS.", e);
		} catch (IOException e) {
			throw new SicsIOException("Cannot connect to SICS.", e);
		}
	}

	protected void setLogin(ISicsConnectionContext context) throws SicsExecutionException, SicsIOException{
		internalSend(context.getRole().getLoginId() + " " + context.getPassword());
		int timeCount = 0;
		while(true) {
			if(getChannelState() == ChannelState.CONNECTED) {
				if(timeCount > SicsCoreProperties.PROXY_TIMEOUT.getLong()) {
					handleConnectionError();
					throw new SicsIOException("Time out on connecting to SICS.");
				}
				try {
					Thread.sleep(WAIT_INTERVAL);
					timeCount += WAIT_INTERVAL;
				} catch (InterruptedException e) {
					getLogger().error("Error using connecting to SICS.", e);
				}
			} else if(getChannelState() == ChannelState.LOGINED) {
				break;
			} else if(getChannelState() == ChannelState.LOGIN_FAILED) {
				disconnect();
				throw new SicsExecutionException("Incorrect login.");
			} else {
				handleConnectionError();
				throw new SicsIOException("Error on connecting to SICS.");
			}
		}
	}

	protected void setJSONProtocol() throws SicsIOException {
		internalSend(SicsCommunicationConstants.CMD_SET_JSON_PROTOCOL);
		int timeCount = 0;
		while(true) {
			if(getChannelState() == ChannelState.LOGINED) {
				if(timeCount > SicsCoreProperties.PROXY_TIMEOUT.getLong()) {
					handleConnectionError();
					throw new SicsIOException("Time out on connecting to SICS.");
				}
				try {
					Thread.sleep(WAIT_INTERVAL);
					timeCount += WAIT_INTERVAL;
				} catch (InterruptedException e) {
					getLogger().error("Error using connecting to SICS.", e);
				}
			} else if(getChannelState() == ChannelState.NORMAL) {
				break;
			} else {
				handleConnectionError();
				throw new SicsIOException("Error on connecting to SICS.");
			}
		}
	}

	private void handleConnectionError() {
		setChannelState(ChannelState.DISCONNECTED);
	}

	private void internalSend(final String command) throws SicsIOException {
		getLogger().info("Client sent: " + command);
		if(proxyOutput == null)
			throw new SicsIOException("Connection error");
		proxyOutput.println(command);
		// make sure buffer is emptied
		proxyOutput.flush();
		getSicsProxy().getListenerManager().asyncInvokeListeners(
				new SafeListenerRunnable<ISicsProxyListener>() {
					public void run(ISicsProxyListener listener)
							throws Exception {
						listener.messageSent(command, getChannelId());
					}
				});
	}

	public void send(String command, ISicsCallback proxyListener) throws SicsIOException {
		if(getChannelState() != ChannelState.NORMAL) {
			throw new SicsIOException("Channel is not ready for accepting commands.");
		}
		int transactionId = transIdCount++;
		getCallbackMap().put(transactionId, proxyListener);
		internalSend("contextdo " + transactionId + " " + command);
	}

	protected synchronized void cleanupCompletedListeners() {
		synchronized (getCallbackMap()) {
			List<Integer> redundantIds = new ArrayList<Integer>();
			for (Entry<Integer, ISicsCallback> entry : getCallbackMap()
					.entrySet()) {
				if (entry.getValue() != null && entry.getValue().isCallbackCompleted())
					redundantIds.add(entry.getKey());
			}
			for (Integer id : redundantIds) {
				getCallbackMap().remove(id);
			}
		}
	}

	private Logger getLogger() {
		if(logger == null) {
			logger = LoggerFactory.getLogger(SicsChannel.class.getName() + ":" + getChannelId());
		}
		return logger;
	}

	protected synchronized Map<Integer, ISicsCallback> getCallbackMap() {
		if(callbackMap == null)
			callbackMap = new HashMap<Integer, ISicsCallback>();
		return callbackMap;
	}

	/**
	 * This is a package level method for channel to notify proxy about recieved messages.
	 * This will broadcast to all proxy listeners.
	 *
	 * @param message
	 */
	protected void messageRecieved(final String message) {
		getSicsProxy().getListenerManager().asyncInvokeListeners(
				new SafeListenerRunnable<ISicsProxyListener>() {
					public void run(ISicsProxyListener listener)
							throws Exception {
						listener.messageReceived(message, getChannelId());
					}
				});
	}

	public synchronized void handleResponse(final JSONObject response) {
		try {
			final String flag = response.getString(JSONTag.FLAG.getText());
			int transId = response.getInt(JSONTag.TRANSACTION.getText());
//			Object data = response.get(JSONTag.DATA.getText());

			// do a clean up on completed listeners
			cleanupCompletedListeners();

			final ISicsCallback callback = getCallbackMap().get(transId);
			if(callback == null) {
				return;
			}
			Thread thread = new Thread(new Runnable() {
				public void run() {
					if(flag.equalsIgnoreCase(Flag.ERROR.toString())) {
						callback.receiveError(new SicsReplyData(response));
					} else if(flag.equalsIgnoreCase(Flag.WARNING.toString())) {
						callback.receiveWarning(new SicsReplyData(response));
					} else if(flag.equalsIgnoreCase("LOG")) {
						callback.receiveWarning(new SicsReplyData(response));
					} else if(flag.equalsIgnoreCase(Flag.FINISH.toString())){
						callback.receiveFinish(new SicsReplyData(response));
					} else {
						callback.receiveReply(new SicsReplyData(response));
					}
				}
			});
			messageProcessed++;
			thread.run();
			invalidMessage++;
		} catch (JSONException e) {
			logger.error("Cannot interrupt response: " + response.toString(), e);
		}
	}
	
}
