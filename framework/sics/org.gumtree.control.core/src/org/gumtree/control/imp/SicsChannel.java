/**
 * 
 */
package org.gumtree.control.imp;

import org.gumtree.control.core.ISicsCallback;
import org.gumtree.control.core.ISicsChannel;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.core.SicsStatic;
import org.gumtree.control.exception.SicsCommunicationException;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.exception.SicsExecutionException;
import org.gumtree.control.exception.SicsInterruptException;
import org.json.JSONException;
import org.json.JSONObject;
import org.zeromq.ZMQ;

/**
 * @author nxi
 *
 */
public class SicsChannel implements ISicsChannel {
	
	public static final String JSON_KEY_STATUS = "status";
	public static final String JSON_KEY_VALUE = "value";
	public static final String JSON_KEY_ERROR = "error";
	public static final String JSON_KEY_INTERRUPT = "interrupt";
	public static final String JSON_KEY_FINISHED = "finished";
	public static final String JSON_KEY_CID = "cid";
	public static final String JSON_KEY_COMMAND = "command";
	
	private ZMQ.Context context;
    private ZMQ.Socket clientSocket;
    private ZMQ.Socket subscriberSocket;
    
    private boolean isBusy;
    private boolean isConnected;
    
    private String id;
    private int cid;
    
    private MessageHandler messageHandler;
    
	public SicsChannel() {
	    id = String.valueOf(System.currentTimeMillis()).substring(3);
	    context = ZMQ.context(2);
	    clientSocket = context.socket(ZMQ.DEALER);
	    clientSocket.setIdentity(id.getBytes(ZMQ.CHARSET));
	    messageHandler = new MessageHandler();
	}
	
	private void subscribe(String publisherAddress) {

		subscriberSocket = context.socket(ZMQ.SUB);
	    subscriberSocket.connect(publisherAddress);
	    subscriberSocket.subscribe("".getBytes());
		
		Thread subscribeThread = new Thread(new Runnable() {
			
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
						// TODO: handle finally clause
					}
				}
			}
		});
		
		subscribeThread.start();
	}

	@Override
	public String send(String command, ISicsCallback callback) throws SicsException {
		if (!isConnected()) {
			throw new SicsCommunicationException("disconnected");
		}
		isBusy = true;
		String value = "";
		try {
			JSONObject jcom = new JSONObject();
			cid++;
			jcom.put(JSON_KEY_CID, cid);
			jcom.put(JSON_KEY_COMMAND, command);
			clientSocket.send(jcom.toString());
			boolean isStarted = false;
			boolean isFinished = false;
			int count = 0;
			while (!isStarted && count < SicsStatic.TIMEOUT_COMMAND_START) {
				String received = clientSocket.recvStr();
				JSONObject json = null;
				try {
					json = new JSONObject(received);	
				} catch (Exception e) {
				}
				if (json != null && json.has(JSON_KEY_CID) && json.getInt(JSON_KEY_CID) == cid) {
					isStarted = true;
					messageHandler.process(json);
					if (json.has(JSON_KEY_VALUE)) {
						value = json.get(JSON_KEY_VALUE).toString();
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
						isFinished = true;
						if (callback != null) {
							callback.receiveFinish(json);
						}
					} else {
						if (callback != null) {
							callback.receiveReply(json);
						}
					}
				} else if (json != null) {
					if (json.has(JSON_KEY_INTERRUPT)) {
						if (callback != null) {
							callback.setError(true);
						}
						throw new SicsInterruptException("interrupted");
					}
				}
//					try {
//						Thread.sleep(50);
//					} catch (InterruptedException e) {
//						if (callback != null) {
//							callback.setError(true);
//						}
//						throw new SicsInterruptException("interrupted");
//					}
//					count += 50;
			}
//			if (!isStarted) {
//				throw new SicsCommunicationException("failed to send the command");
//			}
			while (!isFinished) {
				String received = clientSocket.recvStr();
					JSONObject json = null;
					try {
						json = new JSONObject(received);
					} catch (Exception e) {
					}
					if (json != null && json.has(JSON_KEY_CID) && json.getInt(JSON_KEY_CID) == cid) {
//						System.out.println("inside command " + json);
						messageHandler.process(json);
						if (json.has(JSON_KEY_VALUE)) {
							value = json.get(JSON_KEY_VALUE).toString();
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
							isFinished = true;
							if (callback != null) {
								callback.receiveFinish(json);
							}
						} else {
							if (callback != null) {
								callback.receiveReply(json);
							}
						}
					} else if (json != null) {
						if (json.has(JSON_KEY_INTERRUPT)) {
							if (callback != null) {
								callback.setError(true);
							}
							throw new SicsInterruptException("interrupted");
						}
					}
				}
		} catch (JSONException e) {
			throw new SicsCommunicationException(e.getMessage());
		} catch (SicsInterruptException e) {
			SicsManager.getSicsProxy().labelInterruptFlag();
			throw e;
		} finally {
			isBusy = false;
		}
		return value;
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
	}

	@Override
	public void disconnect() {
		clientSocket.close();
		subscriberSocket.close();
        context.term();
	}
	
	@Override
	public boolean isBusy() {
		return isBusy;
	}
}
