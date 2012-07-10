package org.gumtree.gumnix.sics.internal.io;

import org.gumtree.gumnix.sics.io.ISicsCallback;
import org.gumtree.gumnix.sics.io.ISicsConnectionContext;
import org.gumtree.gumnix.sics.io.SicsExecutionException;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SicsZipDataChannel extends SicsChannel {

	private static Logger logger;

	protected SicsZipDataChannel(String channelId, SicsProxy sicsProxy) {
		super(channelId, sicsProxy);
	}

	public void login(ISicsConnectionContext context) throws SicsExecutionException, SicsIOException {
		if(getChannelState() != ChannelState.DISCONNECTED) {
			throw new SicsIOException("Channel communication has already been established.");
		}

		// STEP 1: Open socket (state: DISCONNECTED -> CONNECTING -> CONNECTED)
		openSocket(context, new SicsZipDataSocketListener(this));
		getLogger().info("Connected to SICS");

		// STEP 2: Login (state: CONNECTED -> LOGINED)
		setLogin(context);
		getLogger().info("Logined to SICS as " + context.getRole());

		// STEP 3: Skip set JSON protocol and make this channel available
		setChannelState(ChannelState.NORMAL);
		getLogger().info("Channel to SICS is ready.");

	}

	private Logger getLogger() {
		if(logger == null) {
			logger = LoggerFactory.getLogger(SicsZipDataChannel.class.getName() + ":" + getChannelId());
		}
		return logger;
	}

	protected synchronized void handleResponse(final Object data, int transId) {
		// do a clean up on completed listeners
		cleanupCompletedListeners();

		final ISicsCallback callback = getCallbackMap().get(transId);
		if(callback == null) {
			return;
		}
		Thread thread = new Thread(new Runnable() {
			public void run() {
				callback.receiveRawData(data);
			}
		});
		thread.run();
	}

}
