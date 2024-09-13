/**
 * 
 */
package org.gumtree.control.imp.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gumtree.control.core.ISicsChannel;
import org.gumtree.control.events.ISicsCallback;
import org.gumtree.control.exception.SicsCommunicationException;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.exception.SicsExecutionException;
import org.gumtree.control.exception.SicsInterruptException;
import org.gumtree.control.imp.SicsReplyData;
import org.gumtree.control.model.PropertyConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

/**
 * @author nxi
 *
 */
public class ClientChannel implements ISicsChannel {
	
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
	
	private static final int COMMAND_WAIT_TIME = 1;
	private static final int COMMAND_TIMEOUT = 10000;
	private static Logger logger = LoggerFactory.getLogger(ClientChannel.class);
	
	private static ZContext context = new ZContext();
    private ZMQ.Socket clientSocket;
//    private ZMQ.Socket subscriberSocket;
    
    private boolean isBusy;
    private boolean isConnected;
    
    private String id;
    private int cid;
    private Map<Integer, SicsCommand> commandMap;
    
    private ClientMessageHandler messageHandler;
    private Thread clientThread;
//    private Thread subscribeThread;
    
    private String serverAddress;
//    private String publisherAddress;
    
    private InputStream inputStream;
    private OutputStream outputStream;
    
    private List<IClientListener> listeners;
    
	public ClientChannel() {
	    id = String.valueOf(System.currentTimeMillis()).substring(3);
//	    context = ZMQ.context(1);
	    clientSocket = context.createSocket(SocketType.DEALER);
	    clientSocket.setSendTimeOut(COMMAND_TIMEOUT);
	    clientSocket.setLinger(0);
	    clientSocket.setIdentity(id.getBytes(ZMQ.CHARSET));
	    messageHandler = new ClientMessageHandler();
	    commandMap = new HashMap<Integer, SicsCommand>();
	    listeners = new ArrayList<IClientListener>();
	}
	
//	private void subscribe(String publisherAddress) {
//
//		subscriberSocket = context.socket(ZMQ.SUB);
//	    subscriberSocket.connect(publisherAddress);
//	    subscriberSocket.subscribe("".getBytes());
//		
//		subscribeThread = new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				while(true) {
//					try {
//						String msg = subscriberSocket.recvStr();
////						System.err.println("SUB " + msg);
////						logger.info("SUB: " + msg);
//						JSONObject json;
//						json = new JSONObject(msg);
//						messageHandler.delayedProcess(json);
//					} catch (ZMQException ze) {
//						logger.error("subscriber socket closed");
//						break;
//					} catch (JSONException e) {
//						logger.error(e.getMessage());
//						e.printStackTrace();
//					} finally {
//					}
//				}
//			}
//		});
//		
//		subscribeThread.start();
//	}

	@Override
	public String syncSend(String command, ISicsCallback callback) throws SicsException {
//		JSONObject json = null;
//		try {
//			json = new JSONObject(received);	
//		} catch (Exception e) {
//		}
//		if (json != null && json.has(JSON_KEY_CID) && json.getInt(JSON_KEY_CID) == cid) {
		cid++;
		SicsCommand sicsCommand = new SicsCommand(cid, command, callback);
		commandMap.put(cid, sicsCommand);
		isBusy = true;
		logger.debug("syncRun: " + command);
		try {
			return sicsCommand.syncRun();
		} finally {
			isBusy = false;
		}
	}

	@Override
	public void asyncSend(String command, ISicsCallback callback) throws SicsException {
		cid++;
		logger.debug("asyncRun: " + command);
		SicsCommand sicsCommand = new SicsCommand(cid, command, callback);
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
//	    subscribe(publisherAddress);
	    this.serverAddress = serverAddress;
//	    this.publisherAddress = publisherAddress;
		isConnected = true;
		clientThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(isConnected) {
					try {
						String received = clientSocket.recvStr();
						String timeStamp = new SimpleDateFormat("dd.HH.mm.ss.SSS").format(new Date());
//						System.err.println(timeStamp + " Received: " + received);
						logger.debug("CMD: " + received);
						JSONObject json = null;
						try {
							json = new JSONObject(received);	
							if (json != null && json.has(JSON_KEY_CID)) {
								int commandId = json.getInt(JSON_KEY_CID);
								if (commandMap.containsKey(commandId)) {
									commandMap.get(commandId).progress(json);
									processMessage(json);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} catch (Exception e) {
						logger.error("proxy disconnected");
						break;
					}
				}
			}
		});
		clientThread.start();
	}

	@Override
	public void disconnect() {
		if (clientSocket != null) {
			if (serverAddress != null) {
				try {
					logger.warn("terminal socket disconnecting");
					clientSocket.disconnect(serverAddress);
					clientSocket.unbind(serverAddress);
				} catch (Exception e) {
					logger.error("failed to disconnect client socket, ", e);
				}
			}
			try {
				clientSocket.close();
			} catch (Exception e) {
				logger.error("failed to close client socket, ", e);
			}
			try {
				context.destroySocket(clientSocket);
//				context.destroy();
				logger.debug("destroyed terminal socket");
			} catch (Exception e) {
				logger.error("failed to terminate ZMQ context, ", e);
			}
		}
//		if (subscriberSocket != null) {
//			if (publisherAddress != null) {
//				try {
//					subscriberSocket.disconnect(publisherAddress);
//				} catch (Exception e) {
//					logger.error("failed to disconnect subscriber socket, ", e);
//				}
//			}
//			try {
//				subscriberSocket.close();
//			} catch (Exception e) {
//				logger.error("failed to close subscriber socket, ", e);
//			}
//		}
        clientThread.interrupt();
        isConnected = false;
	}
	
	@Override
	public boolean isBusy() {
		return isBusy;
	}
	
	@Override
	public void reset() {
		isBusy = false;
	}
	
	@Override
	public void syncPoch() throws SicsCommunicationException {
	}
	
	public void dropCommand(Integer cid) {
		commandMap.remove(cid);
	}
	
	public InputStream getInputStream() {
		return inputStream;
	}
	
	public OutputStream getOutputStream() {
		return outputStream;
	}
	
	public void addClientListener(IClientListener listener) {
		listeners.add(listener);
	}
	
	public void removeClientListener(IClientListener listener) {
		listeners.remove(listener);
	}
	
	private void processMessage(final JSONObject json) {
		for (IClientListener listener : listeners) {
			listener.processMessage(json);
		}
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
		
		SicsCommand(int cid, String command, ISicsCallback callback) {
			this.cid = cid;
			this.command = command;
			this.callback = callback;
			isFinished = false;
		}
		
		String syncRun() throws SicsException {
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
			logger.debug("syncSend: " + msg);
			clientSocket.send(msg);
			while (!isFinished) {
				try {
					Thread.sleep(COMMAND_WAIT_TIME);
				} catch (InterruptedException e) {
					throw new SicsExecutionException("interrupted");
				}
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
				if (!isStarted) {
					isStarted = true;
					messageHandler.process(json);
					if (json.has(PropertyConstants.PROP_COMMAND_REPLY)) {
						reply = json.get(PropertyConstants.PROP_COMMAND_REPLY).toString();
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
				takeError(e);;
			} catch (SicsException e) {
				takeError(e);
			}
		}
		
		void interrupt() {
			takeError(new SicsInterruptException("user interrupted"));
		}
		
		void finish() {
			isFinished = true;
			dropCommand(cid);
		}
		
		boolean hasError() {
			return hasError;
		}
		
		SicsException getError() {
			return error;
		}
	}
}
