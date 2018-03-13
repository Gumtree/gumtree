/**
 * 
 */
package org.gumtree.control.imp;

import java.util.HashMap;
import java.util.Map;

import org.gumtree.control.core.ISicsChannel;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsCallback;
import org.gumtree.control.exception.SicsCommunicationException;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.exception.SicsExecutionException;
import org.gumtree.control.exception.SicsInterruptException;
import org.gumtree.control.model.PropertyConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.zeromq.ZMQ;

/**
 * @author nxi
 *
 */
public class SicsChannel implements ISicsChannel {
	
	public static final String JSON_KEY_STATUS = "status";
	public static final String JSON_KEY_ERROR = "error";
	public static final String JSON_KEY_INTERRUPT = "interrupt";
	public static final String JSON_KEY_FINISHED = "finished";
	public static final String JSON_KEY_CID = "cid";
	public static final String JSON_KEY_COMMAND = "command";
	
	private static final int COMMAND_WAIT_TIME = 1;
	
	private ZMQ.Context context;
    private ZMQ.Socket clientSocket;
    private ZMQ.Socket subscriberSocket;
    
    private boolean isBusy;
    private boolean isConnected;
    
    private String id;
    private int cid;
    private Map<Integer, SicsCommand> commandMap;
    
    private MessageHandler messageHandler;
    private Thread clientThread;
    private Thread subscribeThread;
    
	public SicsChannel() {
	    id = String.valueOf(System.currentTimeMillis()).substring(3);
	    context = ZMQ.context(2);
	    clientSocket = context.socket(ZMQ.DEALER);
	    clientSocket.setIdentity(id.getBytes(ZMQ.CHARSET));
	    messageHandler = new MessageHandler();
	    commandMap = new HashMap<Integer, SicsCommand>();
	}
	
	private void subscribe(String publisherAddress) {

		subscriberSocket = context.socket(ZMQ.SUB);
	    subscriberSocket.connect(publisherAddress);
	    subscriberSocket.subscribe("".getBytes());
		
		subscribeThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					String msg = subscriberSocket.recvStr();
//					System.out.println("subscribe " + msg);
					JSONObject json;
					try {
						json = new JSONObject(msg);
						messageHandler.delayedProcess(json);
					} catch (JSONException e) {
						e.printStackTrace();
					} finally {
					}
				}
			}
		});
		
		subscribeThread.start();
	}

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
		try {
			return sicsCommand.syncRun();
		} finally {
			isBusy = false;
		}
	}

	@Override
	public void asyncSend(String command, ISicsCallback callback) throws SicsException {
		cid++;
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
	    subscribe(publisherAddress);
		isConnected = true;
		clientThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(isConnected) {
					String received = clientSocket.recvStr();
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
						e.printStackTrace();
					}
				}
			}
		});
		clientThread.start();
	}

	@Override
	public void disconnect() {
		clientSocket.close();
		subscriberSocket.close();
        context.term();
        isConnected = false;
	}
	
	@Override
	public boolean isBusy() {
		return isBusy;
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
				jcom.put(JSON_KEY_CID, cid);
				jcom.put(JSON_KEY_COMMAND, command);
			} catch (JSONException e1) {
				throw new SicsExecutionException("illegal command");
			}
			clientSocket.send(jcom.toString());
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
				jcom.put(JSON_KEY_CID, cid);
				jcom.put(JSON_KEY_COMMAND, command);
			} catch (JSONException e1) {
				throw new SicsExecutionException("illegal command");
			}
			clientSocket.send(jcom.toString());
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
					if (json.has(PropertyConstants.PROP_UPDATE_DATA)) {
						reply = json.get(PropertyConstants.PROP_UPDATE_DATA).toString();
					}
					if (json.has(JSON_KEY_INTERRUPT)) {
						if (callback != null) {
							callback.setError(true);
						}
						throw new SicsInterruptException("interrupted");
					}
					if (json.has(JSON_KEY_ERROR)) {
						if (callback != null) {
							callback.setError(true);
						}
						throw new SicsExecutionException(json.getString(JSON_KEY_ERROR));
					}
					if (json.has(JSON_KEY_FINISHED)) {
						finish();
						if (callback != null) {
							callback.receiveFinish(new SicsReplyData(json));
						}
					} else {
						if (callback != null) {
							callback.receiveReply(new SicsReplyData(json));
						}
					}
				} else {
					messageHandler.process(json);
					if (json.has(PropertyConstants.PROP_UPDATE_DATA)) {
						reply = json.get(PropertyConstants.PROP_UPDATE_DATA).toString();
					}
					if (json.has(JSON_KEY_INTERRUPT)) {
						if (callback != null) {
							callback.setError(true);
						}
						throw new SicsInterruptException("interrupted");
					}
					if (json.has(JSON_KEY_ERROR)) {
						if (callback != null) {
							callback.setError(true);
						}
						throw new SicsExecutionException(json.getString(JSON_KEY_ERROR));
					}
					if (json.has(JSON_KEY_FINISHED)) {
						finish();
						if (callback != null) {
							callback.receiveFinish(new SicsReplyData(json));
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
				SicsManager.getSicsProxy().labelInterruptFlag();
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
