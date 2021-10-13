package org.gumtree.gumnix.sics.internal.io;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

import ch.unifr.nio.framework.AbstractClientSocketChannelHandler;
import ch.unifr.nio.framework.transform.AbstractForwarder;
import ch.unifr.nio.framework.transform.ByteBufferToStringTransformer;
import ch.unifr.nio.framework.transform.StringToByteBufferTransformer;

public class SicsNonBlockingChannel extends AbstractSicsChannel {
	
	private Logger logger;
	
	private volatile Map<Integer, ISicsCallback> callbackMap;
	
	private SicsChannelHandler channelHandler;
	
	private Executor executor;

	private Lock responseHandlingLock;
	
	private boolean isJSonProtocol;
	// Internal use for login
	private BlockingQueue<MessageContainer> incomingMessageQueue;
	
	protected SicsNonBlockingChannel(String channelId, SicsProxy sicsProxy, boolean isJSonProtocol) {
		super(channelId, sicsProxy);
		channelHandler = new SicsChannelHandler();
		incomingMessageQueue = new LinkedBlockingQueue<MessageContainer>();
		executor = Executors.newFixedThreadPool(1);
		responseHandlingLock = new ReentrantLock();
		this.isJSonProtocol = isJSonProtocol;
	}

	public void login(ISicsConnectionContext context)
			throws SicsExecutionException, SicsIOException {
		if(getChannelState() != ChannelState.DISCONNECTED) {
			throw new SicsIOException("Channel communication has already been established.");
		}
		
		try {
			// Connect (timeout in 5 sec)
			setChannelState(ChannelState.CONNECTING);
			incomingMessageQueue.clear();
			channelHandler.getSicsChannelReader().setIncomingMessageQueue(incomingMessageQueue);
			getSicsProxy().getDispatcher().registerClientSocketChannelHandler(
					context.getHost(), context.getPort(), channelHandler);
			String reply = incomingMessageQueue.poll(5, TimeUnit.SECONDS).getMessage();
			if (reply == null || !reply.startsWith("OK")) {
				setChannelState(ChannelState.DISCONNECTED);
				throw new SicsIOException("Failed to connect.");
			}
			incomingMessageQueue.clear();
			setChannelState(ChannelState.CONNECTED);
			channelHandler.setKeepAliveOption();
			getLogger().info("Connected.");
			
			// Login
			channelHandler.write(context.getRole().getLoginId() + " " + context.getPassword());
			reply = incomingMessageQueue.poll(5, TimeUnit.SECONDS).getMessage();
			if (reply == null || !reply.startsWith("Login OK")) {
				getLogger().info("Login failed.");
				setChannelState(ChannelState.DISCONNECTED);
				throw new SicsIOException("Incorrect login.");
			}
			incomingMessageQueue.clear();
			setChannelState(ChannelState.LOGINED);
			getLogger().info("Logined");
			
			// Set JSON protocol
			if (isJSonProtocol) {
				channelHandler.write(SicsCommunicationConstants.CMD_SET_JSON_PROTOCOL);
				reply = incomingMessageQueue.poll(5, TimeUnit.SECONDS).getMessage();
				if (reply == null) {
					setChannelState(ChannelState.DISCONNECTED);
					throw new SicsIOException("Failed to connect.");
				}
			}
			incomingMessageQueue.clear();
			channelHandler.getSicsChannelReader().setIncomingMessageQueue(getSicsProxy().getIncomingMessageQueue());
			setChannelState(ChannelState.NORMAL);
			getLogger().info("Channel ready.");
			
		} catch (InterruptedException e) {
			setChannelState(ChannelState.DISCONNECTED);
			throw new SicsIOException("Failed to connect.", e);
		} catch (IOException e) {
			setChannelState(ChannelState.DISCONNECTED);
			throw new SicsIOException("Failed to connect.", e);
		}
	}

	public void disconnect() throws SicsIOException {
		logger.error("Disconnected channel " + getChannelId());
		channelHandler.closeChannel();
	}

	public void send(final String command, ISicsCallback proxyListener)
			throws SicsIOException {
		if(getChannelState() != ChannelState.NORMAL) {
			throw new SicsIOException("Channel is not ready for accepting commands.");
		}
		int transactionId = transIdCount++;
		getCallbackMap().put(transactionId, proxyListener);
		try {
			channelHandler.write("contextdo " + transactionId + " " + command);
			getSicsProxy().getListenerManager().asyncInvokeListeners(
					new SafeListenerRunnable<ISicsProxyListener>() {
						public void run(ISicsProxyListener listener)
								throws Exception {
							listener.messageSent(command, getChannelId());
						}
					});
		} catch (IOException e) {
			throw new SicsIOException("Failed to send command: " + command, e);
		}
	}
	
	public void handleResponse(final JSONObject response) {
		try {
			responseHandlingLock.lock();
			final String flag = response.getString(JSONTag.FLAG.getText());
			int transId = response.getInt(JSONTag.TRANSACTION.getText());
//			Object data = response.get(JSONTag.DATA.getText());

			// do a clean up on completed listeners
			cleanupCompletedListeners();

			final ISicsCallback callback = getCallbackMap().get(transId);
			if(callback == null) {
				return;
			}
			executor.execute(new Runnable() {
				public void run() {
					if(flag.equalsIgnoreCase(Flag.ERROR.toString())) {
						callback.receiveError(new SicsReplyData(response));
					} else if(flag.equalsIgnoreCase(Flag.WARNING.toString())) {
						callback.receiveWarning(new SicsReplyData(response));
					} else if(flag.equalsIgnoreCase("log")) {
						callback.receiveWarning(new SicsReplyData(response));
					} else if(flag.equalsIgnoreCase(Flag.EVENT.toString())) {
						callback.receiveWarning(new SicsReplyData(response));
					} else if(flag.equalsIgnoreCase(Flag.FINISH.toString())){
						callback.receiveFinish(new SicsReplyData(response));
					} else {
						callback.receiveReply(new SicsReplyData(response));
					}
				}
			});
			messageProcessed++;
		} catch (JSONException e) {
			invalidMessage++;
			logger.error("Cannot interrupt response: " + response.toString(), e);
		} finally {
			responseHandlingLock.unlock();
		}
	}
	
	protected void cleanupCompletedListeners() {
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
	
	protected Map<Integer, ISicsCallback> getCallbackMap() {
		if (callbackMap == null) {
			synchronized (this) {
				if (callbackMap == null) {
					callbackMap = new HashMap<Integer, ISicsCallback>();
				}
			}
		}
		return callbackMap;
	}

	private Logger getLogger() {
		if(logger == null) {
			logger = LoggerFactory.getLogger(SicsNonBlockingChannel.class.getName() + ":" + getChannelId());
		}
		return logger;
	}
	
	class SicsChannelHandler extends AbstractClientSocketChannelHandler {

		private StringToByteBufferTransformer stringToByteBufferTransformer;
		
		private SicsChannelReader sicsChannelReader;
		
		protected SicsChannelHandler() {
			// Use direct read, max buffer size and indirect write
			super(true, 4096, Integer.MAX_VALUE, false);
			
			// setup input chain
			ByteBufferToStringTransformer byteBufferToStringTransformer = new ByteBufferToStringTransformer();
			reader.setNextForwarder(byteBufferToStringTransformer);
			sicsChannelReader = new SicsChannelReader();
			byteBufferToStringTransformer.setNextForwarder(sicsChannelReader);

			// setup output chain
			stringToByteBufferTransformer = new StringToByteBufferTransformer();
			stringToByteBufferTransformer.setNextForwarder(writer);
			
		}
		
		protected void write(String data) throws IOException {
			getLogger().info("Client sent: " + data);
			stringToByteBufferTransformer.forward(data + "\n");
		}
		
		protected SicsChannelReader getSicsChannelReader() {
			return sicsChannelReader;
		}
		
		// TODO: report user with connection error
		public void resolveFailed() {
//			incomingMessageQueue.offer(FLAG_FAILED);
		}

		public void connectSucceeded() {
//			incomingMessageQueue.offer(FLAG_CONNECTED);
		}

		// TODO: report user with connection error
		public void connectFailed(IOException exception) {
//			incomingMessageQueue.offer(FLAG_FAILED);
		}

		// TODO: report user with connection error
		public void inputClosed() {
			
		}
		
		// TODO: report user with connection error
		public void channelException(Exception exception) {
		}
		
		public void closeChannel(){
			try {
				handlerAdapter.closeChannel();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void setKeepAliveOption() {
			try {
				((SocketChannel) handlerAdapter.getChannel()).setOption(StandardSocketOptions.SO_KEEPALIVE, true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	class SicsChannelReader extends AbstractForwarder<String, Void> {
		
		private StringBuilder stringBuilder; 
		
		private BlockingQueue<MessageContainer> incomingMessageQueue;
		
		private Lock lock;
		
		public SicsChannelReader() {
			stringBuilder = new StringBuilder();
			lock = new ReentrantLock();
		}

		@Override
		public void forward(String input) throws IOException {
			lock.lock();
			try {
				int index = -1;
				int cursor = 0;
				while ((index = input.indexOf('\n', cursor)) != -1) {
					// End of line detected
					String message = input.substring(cursor, index).trim();
					cursor = index + 1;
					queueMessage(message);
				}
				if (cursor < input.length()) {
					// End of line not detected
					appendStringBuffer(input.substring(cursor, input.length()));
				}
			} finally {
				lock.unlock();
			}
		}

		protected void setIncomingMessageQueue(BlockingQueue<MessageContainer> incomingMessageQueue) {
			this.incomingMessageQueue = incomingMessageQueue;
		}
		
		private void queueMessage(String message) {
			appendStringBuffer(message);
			String actualMessage = stringBuilder.toString();
			if (actualMessage.length() > 0) {
				lineRead++;
				MessageContainer container = new MessageContainer(getChannelId(), actualMessage);
				// [GUMTREE-839] Message drop mechanism
				// Drop previously queued message and append to the last
//				if (messageDropEnable) {
//					if (incomingMessageQueue.remove(container)) {
//						messageDropped++;
//					}
//				}
				incomingMessageQueue.offer(container);
			}
			clearStringBuffer();
		}
		
		private void appendStringBuffer(String text) {
			stringBuilder.append(text);
		}
		
		private void clearStringBuffer() {
			stringBuilder.delete(0, stringBuilder.length());
		}
		
	}
	
}
