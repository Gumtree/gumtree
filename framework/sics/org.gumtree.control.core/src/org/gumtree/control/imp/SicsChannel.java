/**
 * 
 */
package org.gumtree.control.imp;

import java.nio.channels.ClosedSelectorException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.gumtree.control.core.ISicsChannel;
import org.gumtree.control.events.ISicsCallback;
import org.gumtree.control.exception.SicsCommunicationException;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.exception.SicsExecutionException;
import org.gumtree.control.exception.SicsInterruptException;
import org.gumtree.control.model.PropertyConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

/**
 * @author nxi
 *
 */
public class SicsChannel implements ISicsChannel {
	
	public enum CommandType {
		sics,
		tcl,
		POCH,
		INT1712
	}
	
	public static final String JSON_KEY_STATUS = "status";
	public static final String JSON_KEY_ERROR = "error";
	public static final String JSON_KEY_FLAG = "flag";
	public static final String JSON_KEY_INTERRUPT = "interrupt";
	public static final String JSON_KEY_FINISHED = "final";
	public static final String JSON_KEY_CID = "trans";
	public static final String JSON_KEY_COMMAND = "text";
	public static final String JSON_KEY_TYPE = "cmd";
	
	public static final String JSON_VALUE_ERROR = "ERROR";
	public static final String JSON_VALUE_OK = "OK";
	private static final String POCH_COMMAND = "POCH";
	
	static final int POCH_TIMEOUT = 90*1000;
	static final int COMMAND_WAIT_TIME = 3;
	static final int SEND_TIMEOUT = 70*000;
	static final int RECEIVE_TIMEOUT = 5000;
	static final int COMMAND_TIMEOUT = 100000;
	
	private static Logger logger = LoggerFactory.getLogger(SicsChannel.class);
	
//	private static ZMQ.Context context = ZMQ.context(2);
//	static ZContext context = new ZContext();
	private ZContext context;
    private ZMQ.Socket clientSocket;
    private ZMQ.Socket subscriberSocket;
    
    private boolean isBusy;
    private boolean isConnected;
    private String currentCommand;
    
    private String id;
    private int cid;
    private int pochId;
    private Map<Integer, SicsCommand> commandMap;
    
    private MessageHandler messageHandler;
    private Thread clientThread;
    private Thread subscribeThread;
    
    private String serverAddress;
    private String publisherAddress;
    
    private SicsProxy sicsProxy;
    
	public SicsChannel(SicsProxy sicsProxy) {
	    id = String.valueOf(System.currentTimeMillis()).substring(3);
	    this.sicsProxy = sicsProxy;
//	    context = ZMQ.context(2);
//	    clientSocket = context.socket(ZMQ.DEALER);
	    context = new ZContext();
	    clientSocket = context.createSocket(SocketType.DEALER);
//	    clientSocket.setSendTimeOut(COMMAND_TIMEOUT);
	    clientSocket.setSendTimeOut(SEND_TIMEOUT);
	    clientSocket.setLinger(0);
//	    clientSocket.setBacklog(1);
//	    clientSocket.setConflate(true);
	    clientSocket.setReceiveTimeOut(RECEIVE_TIMEOUT);
	    clientSocket.setIdentity(id.getBytes(ZMQ.CHARSET));
	    messageHandler = new MessageHandler(sicsProxy);
	    commandMap = new ConcurrentHashMap<Integer, SicsCommand>();
	}
	
	private void subscribe(String publisherAddress) {

//		subscriberSocket = context.socket(ZMQ.SUB);
		subscriberSocket = context.createSocket(SocketType.SUB);
	    subscriberSocket.connect(publisherAddress);
	    subscriberSocket.subscribe("".getBytes());
		
	}
	
	private void startSubscribeThread() {
		subscribeThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(isConnected) {
					try {
						String msg = subscriberSocket.recvStr();
						logger.debug("SUB: " + msg);
						JSONObject json;
						json = new JSONObject(msg);
						messageHandler.delayedProcess(json);
					} catch (ZMQException ze) {
						logger.error("subscriber socket not available");
						break;
					} catch (JSONException e) {
						logger.error(e.getMessage());
						e.printStackTrace();
					} catch (ClosedSelectorException ce) { 
						logger.error("subscriber socket not available");
						break;
					} catch (Exception ne) {
						logger.error("broken subscriber socket");
//						break;
					} finally {
					}
				}
			}
		});
		
		subscribeThread.start();
	}

	@Override
	public String syncSend(String command, ISicsCallback callback) throws SicsException {
		return syncSend(command, callback, -1);
	}
	
	@Override
	public String syncSend(String command, ISicsCallback callback, int timeout) throws SicsException {
//		JSONObject json = null;
//		try {
//			json = new JSONObject(received);	
//		} catch (Exception e) {
//		}
//		if (json != null && json.has(JSON_KEY_CID) && json.getInt(JSON_KEY_CID) == cid) {
		if (isBusy) {
			throw new SicsExecutionException("channel is busy with the current command: " + currentCommand);
		}
		cid++;
		currentCommand = command;
		SicsCommand sicsCommand = new SicsCommand(cid, command, callback, timeout);
		commandMap.put(cid, sicsCommand);
		isBusy = true;
		logger.info("syncRun: " + command);
		try {
			return sicsCommand.syncRun();
		} catch(Exception e) {
			isBusy = false;
			logger.error(e.getMessage());
			if (sicsProxy.isInterrupted()) {
				throw new SicsInterruptException("user interrupted");
			} else if (e instanceof SicsCommunicationException) {
				throw e;
			} else {
				throw e;
			}
		} finally {
			commandMap.remove(cid);
			isBusy = false;
		}
	}

	@Override
	public String syncPoch() throws SicsCommunicationException {
		pochId --;
//		SicsCommand sicsCommand = new SicsCommand(pochId, POCH_COMMAND, null, POCH_TIMEOUT);
		PochCommand sicsCommand = new PochCommand(pochId);
		commandMap.put(pochId, sicsCommand);
		try {
			return sicsCommand.syncRun(POCH_COMMAND);
		} catch(SicsCommunicationException e) {
			logger.error("failed to PING server, ", e);
			throw e;
		} catch (Exception e) {
			return "";
		} finally {
			commandMap.remove(pochId);
		}
	}
	
	@Override
	public void asyncSend(String command, ISicsCallback callback) throws SicsException {
		cid++;
		logger.info("asyncRun: " + command);
		SicsCommand sicsCommand = new SicsCommand(cid, command, callback, -1);
		commandMap.put(cid, sicsCommand);
		sicsCommand.asyncRun();
	}

	@Override
	public boolean isConnected() {
		return isConnected;
	}

	@Override
	public void connect(String serverAddress, String publisherAddress) throws SicsCommunicationException {
	    clientSocket.connect(serverAddress);
	    subscribe(publisherAddress);
		isConnected = true;
	    startSubscribeThread();
	    this.serverAddress = serverAddress;
	    this.publisherAddress = publisherAddress;
		isBusy = false;
		clientThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// Use a ZMQ Poller to avoid blocking indefinitely on recv
				ZMQ.Poller poller = context.createPoller(1);
				poller.register(clientSocket, ZMQ.Poller.POLLIN);
				while(isConnected) {
					try {
						logger.debug("waiting for next message");
						int rc = poller.poll(RECEIVE_TIMEOUT);
						if (rc <= 0) {
							// timeout or interrupted, continue waiting
							continue;
						}
						if (!poller.pollin(0)) {
							// no input ready despite poll returning > 0
							continue;
						}
						String received = clientSocket.recvStr(ZMQ.DONTWAIT);
//						String timeStamp = new SimpleDateFormat("dd.HH.mm.ss.SSS").format(new Date());
//						System.err.println(timeStamp + " Received: [" + received);
						if (received == null) {
							logger.debug("received null");
							continue;
						}
						logger.debug("CMD: " + received);
						JSONObject json = null;
						try {
							json = new JSONObject(received);	
							if (json != null && json.has(JSON_KEY_CID)) {
								int commandId = json.getInt(JSON_KEY_CID);
								if (commandMap.containsKey(commandId)) {
									commandMap.get(commandId).progress(json);
								}
							}
						} catch (Exception e) {
							logger.error("JSon Error: " + (json != null ? json.toString() : "failed to create JSon object;"), e);
						}
					} catch (Exception e) {
						logger.error("proxy disconnected", e);
						break;
					}
				}
				logger.warn("quitting client thread");
			}
		});
		clientThread.start();
	}

	@Override
	public void disconnect() {
		logger.warn("disconnection called");
		if (clientSocket != null) {
			if (serverAddress != null) {
				try {
					logger.debug("client socket disconnecting");
					clientSocket.disconnect(serverAddress);
					clientSocket.unbind(serverAddress);
				} catch (Exception e) {
					logger.error("failed to disconnect client socket, ", e);
				}
			}
			try {
				clientSocket.close();
				logger.debug("client socket closed");
			} catch (Exception e) {
				logger.error("failed to close client socket, ", e);
			}
		}
		if (subscriberSocket != null) {
			if (publisherAddress != null) {
				try {
					logger.debug("subscriber socket disconnecting");
					subscriberSocket.unsubscribe("");
					subscriberSocket.disconnect(publisherAddress);
					subscriberSocket.unbind(publisherAddress);
				} catch (Exception e) {
					logger.error("failed to disconnect subscriber socket, ", e);
				}
			}
			try {
				subscriberSocket.close();
				logger.debug("subscriber socket closed");
			} catch (Exception e) {
				logger.error("failed to close subscriber socket, ", e);
			}
		}
		if (clientThread != null) {
			logger.debug("interrupting client thread");
	        clientThread.interrupt();
		}
		try {
//			context.destroySocket(clientSocket);
//			logger.debug("context destroy client");
//			context.destroySocket(subscriberSocket);
//			logger.debug("context destroy subscriber");
			logger.debug("destroying context");
			logger.error("context closed = " + context.isClosed());
//			logger.error("context empty = " + context.isEmpty());
//			context.close();
			for (ZMQ.Socket socket : context.getSockets()) {
				socket.close();
			}
			logger.error("socket number = " + context.getSockets().size());
//			context.destroy();
			Thread dt = destroyContext(context);
			int ct = 0;
			while(dt.isAlive() && ct < SEND_TIMEOUT) {
				Thread.sleep(COMMAND_WAIT_TIME);
				ct += COMMAND_WAIT_TIME;
			}
			if (ct >= SEND_TIMEOUT) {
				logger.debug("interrupting destroy thread");
				dt.interrupt();
			}
			logger.debug("context destroyed");
		} catch (Exception e) {
			logger.error("failed to terminate ZMQ context, ", e);
		}

        isConnected = false;
        logger.warn("finished disconnecting routine");
	}
	
	private Thread destroyContext(final ZContext context) {
		Thread dThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					context.destroy();
				} catch (Exception e) {
					logger.error("failed to finish destroying context");
				}
				logger.debug("end of destroying thread");
			}
		});
		
		dThread.start();
		return dThread;
	}
	
	@Override
	public boolean isBusy() {
		return isBusy;
	}
	
	public void reset() {
		Set<Integer> keys = new TreeSet<Integer>(commandMap.keySet());
		for (Integer key : keys) {
			if (commandMap.containsKey(key)) {
				SicsCommand command = commandMap.get(key);
				if (!command.isFinished) {
					command.drop();
				}
			}
		}
		isBusy = false;
	}
	
	public void dropCommand(Integer cid) {
		commandMap.remove(cid);
	}
	
	class SicsCommand {
		
		private int cid;
		private String command;
		private ISicsCallback callback;
		private boolean isStarted;
		private boolean isFinished;
		private boolean hasError;
		private SicsException error;
		private String reply;
		private int timeout = -1;
		
		SicsCommand(int cid, String command, ISicsCallback callback, int timeout) {
			this.cid = cid;
			this.command = command;
			this.callback = callback;
			this.timeout = timeout;
			isFinished = false;
		}
		
		String syncRun() throws SicsException {
			return syncRun("sics");
		}
		
		String syncRun(String type) throws SicsException {
			isStarted = false;
			JSONObject jcom = new JSONObject();
			try {
				jcom.put(JSON_KEY_TYPE, type);
				jcom.put(JSON_KEY_CID, cid);
				jcom.put(JSON_KEY_COMMAND, command);
			} catch (JSONException e1) {
				throw new SicsExecutionException("illegal command");
			}
//			String timeStamp = new SimpleDateFormat("dd.HH.mm.ss.SSS").format(new Date());
//			System.err.println(timeStamp + " async send: " + jcom.toString());
			String msg = jcom.toString();
			if (!POCH_COMMAND.equals(command)) {
				logger.debug("syncSend: " + command);
			}
			if (!clientSocket.send(msg, ZMQ.DONTWAIT)) {
				logger.debug("client socket broken");
				throw new SicsCommunicationException("client socket broken");
			}
			int tc = 0;
			while (!isStarted && !isFinished && tc < COMMAND_TIMEOUT) {
				try {
					Thread.sleep(COMMAND_WAIT_TIME);
				} catch (InterruptedException e) {
					finish();
					throw new SicsExecutionException("interrupted");
				}
				tc += COMMAND_WAIT_TIME;
			}
			if (tc >= COMMAND_TIMEOUT) {
//				disconnect();
//				System.err.println("timeout starting command: " + command);
				finish();
				throw new SicsCommunicationException("timeout starting command: " + command);
			}
			tc = 0;
			while (!isFinished && (timeout <= 0 || tc <= timeout)) {
				try {
					Thread.sleep(COMMAND_WAIT_TIME);
				} catch (InterruptedException e) {
					throw new SicsExecutionException("interrupted");
				}
				tc += COMMAND_WAIT_TIME;
			}
			if (timeout > 0 && tc > timeout) {
				throw new SicsExecutionException("timout finishing command " + command);
			}
			if (hasError && error != null) {
				throw error;
			}
			return reply;
		}

		void asyncRun() throws SicsExecutionException {
			isStarted = false;
			JSONObject jcom = new JSONObject();
			try {
				jcom.put(JSON_KEY_TYPE, "sics");
				jcom.put(JSON_KEY_CID, cid);
				jcom.put(JSON_KEY_COMMAND, command);
			} catch (JSONException e1) {
				throw new SicsExecutionException("illegal command");
			}
//			String timeStamp = new SimpleDateFormat("dd.HH.mm.ss.SSS").format(new Date());
//			System.err.println(timeStamp + " async send: " + jcom.toString());
			String msg = jcom.toString();
			logger.debug("asyncSend: " + msg);
			clientSocket.send(msg);
		}

		void takeError(SicsException error) {
			logger.error("take error " + error.getMessage());
			hasError = true;
			this.error = error;
			finish();
		}
		
		void progress(JSONObject json) {
			if (!isConnected()) {
				takeError(new SicsCommunicationException("disconnected"));
				return;
			}
			try {
//				logger.debug(json.toString());
				if (!isStarted) {
					isStarted = true;
					messageHandler.process(json);
//					System.err.println(json);
					if (json.has(PropertyConstants.PROP_COMMAND_REPLY)) {
						reply = json.get(PropertyConstants.PROP_COMMAND_REPLY).toString();
						if (!POCH_COMMAND.equals(command)) {
							logger.debug(String.format("reply of %s: %s", command, reply));
						}
					}
					if (json.has(JSON_KEY_INTERRUPT)) {
						if (callback != null) {
							callback.setError(true);
						}
						throw new SicsInterruptException("interrupted");
					}
					if (json.has(JSON_KEY_FLAG)) {
						String flag = json.getString(JSON_KEY_FLAG);
						if (JSON_VALUE_ERROR.equals(flag)) {
							if (callback != null) {
								callback.setError(true);
							}
							throw new SicsExecutionException(reply);
						}
					}
					if (json.has(JSON_KEY_FINISHED)) {
						if (json.getString(JSON_KEY_FINISHED).equals("true")) {
							finish();
							if (callback != null) {
								callback.receiveFinish(new SicsReplyData(json));
							}
						}
					} else {
						if (callback != null) {
							callback.receiveReply(new SicsReplyData(json));
						}
					}
				} else {
					messageHandler.process(json);
					if (json.has(PropertyConstants.PROP_COMMAND_REPLY)) {
						reply = json.get(PropertyConstants.PROP_COMMAND_REPLY).toString().trim();
					}
					if (json.has(JSON_KEY_INTERRUPT)) {
						if (callback != null) {
							callback.setError(true);
						}
						throw new SicsInterruptException("interrupted");
					}
					if (json.has(JSON_KEY_FLAG)) {
						String flag = json.getString(JSON_KEY_FLAG);
						if (JSON_VALUE_ERROR.equals(flag)) {
							if (callback != null) {
								callback.setError(true);
							}
							logger.error(String.format("Error in command '%s': %s", command, reply));
							throw new SicsExecutionException(reply);
						}
					}
					if (json.has(JSON_KEY_FINISHED)) {
						if (json.getString(JSON_KEY_FINISHED).equals("true")) {
							if (callback != null) {
								callback.receiveFinish(new SicsReplyData(json));
							}
							finish();
						}
					} else {
						if (callback != null) {
							callback.receiveReply(new SicsReplyData(json));
						}
					}
				} 
			} catch (JSONException e) {
				takeError(new SicsCommunicationException(e.getMessage()));
			} catch (SicsInterruptException e) {
				sicsProxy.labelInterruptFlag();
				if (callback != null) {
					callback.receiveFinish(new SicsReplyData(json));
				}
				takeError(e);;
			} catch (SicsException e) {
				takeError(e);
			}
		}
		
		void interrupt() {
			takeError(new SicsInterruptException("user interrupted"));
		}
		
		void drop() {
			isStarted = true;
			finish();
		}
		
		void finish() {
			isFinished = true;
			dropCommand(cid);
		}
		
		void finish(String reply) {
			this.reply = reply;
			finish();
		}
		
		boolean hasError() {
			return hasError;
		}
		
		SicsException getError() {
			return error;
		}
		
		void setReply(String reply) {
			this.reply = reply;
		}
	}
	
	class PochCommand extends SicsCommand {
		
		public PochCommand(int pid) {
			super(pid, POCH_COMMAND, null, POCH_TIMEOUT);
		}
		
		@Override
		void progress(JSONObject json) {
			if (!isConnected()) {
				takeError(new SicsCommunicationException("disconnected"));
				return;
			}
			try {
				if (json.has(PropertyConstants.PROP_COMMAND_SINCE)) {
					setReply(json.get(PropertyConstants.PROP_COMMAND_SINCE).toString());
				} else {
					setReply(null);
				}
				finish();
			} catch (JSONException e) {
				takeError(new SicsCommunicationException(e.getMessage()));
			} 
		}
	}
}
