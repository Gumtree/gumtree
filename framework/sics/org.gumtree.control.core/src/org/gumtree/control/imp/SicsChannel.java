/**
 * 
 */
package org.gumtree.control.imp;

import org.gumtree.control.core.ISicsChannel;
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
	
	public static final String JSON_KEY_VALUE = "value";
	public static final String JSON_KEY_ERROR = "error";
	public static final String JSON_KEY_FINISHED = "finished";
	
	private ZMQ.Context context;

    private ZMQ.Socket socket;
    
    private boolean isBusy;
    private boolean isConnected;
    
	public SicsChannel() {
	    context = ZMQ.context(1);
	    socket = context.socket(ZMQ.REQ);
	}
	
	@Override
	public String send(String command) throws SicsException {
		if (!isConnected()) {
			throw new SicsCommunicationException("disconnected");
		}
		isBusy = true;
		String value = "";
		try {
			socket.send(command.getBytes(ZMQ.CHARSET), 0);
			boolean isStarted = false;
			boolean isFinished = false;
			int count = 0;
			while (!isStarted && count < SicsStatic.TIMEOUT_COMMAND_START) {
				byte[] reply = socket.recv(0);
				if (reply != null) {
					String received = new String(reply, ZMQ.CHARSET);
					JSONObject json = null;
					try {
						json = new JSONObject(received);	
					} catch (JSONException e) {
						throw new SicsCommunicationException(received);
					}
					if (json != null) {
						isStarted = true;
						try {
							if (json.has(JSON_KEY_VALUE)) {
								value = json.get(JSON_KEY_VALUE).toString();
							}
							if (json.has(JSON_KEY_ERROR)) {
								throw new SicsExecutionException(json.getString(JSON_KEY_ERROR));
							}
							if (json.has(JSON_KEY_FINISHED)) {
								isFinished = true;
							}
						} catch (Exception e) {
							throw new SicsCommunicationException(e.getMessage());
						}
					}
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					throw new SicsInterruptException("interrupted");
				}
				count += 50;
			}
			if (!isStarted) {
				throw new SicsCommunicationException("failed to send the command");
			}
			while (!isFinished) {
				byte[] reply = socket.recv(0);
				if (reply != null) {
					String received = new String(reply, ZMQ.CHARSET);
					JSONObject json = null;
					try {
						json = new JSONObject(received);	
					} catch (JSONException e) {
						throw new SicsCommunicationException(received);
					}
					if (json != null) {
						isStarted = true;
						try {
							if (json.has(JSON_KEY_VALUE)) {
								value = json.get(JSON_KEY_VALUE).toString();
							}
							if (json.has(JSON_KEY_ERROR)) {
								throw new SicsExecutionException(json.getString(JSON_KEY_ERROR));
							}
							if (json.has(JSON_KEY_FINISHED)) {
								isFinished = true;
							}
						} catch (Exception e) {
							throw new SicsCommunicationException(e.getMessage());
						}
					}
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					throw new SicsInterruptException("interrupted");
				}
			}
		} finally {
			isBusy = false;
		}
		return value;
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return isConnected;
	}

	@Override
	public void connect(String server) throws SicsCommunicationException {
	    socket.connect(server);
		isConnected = true;
	}

	@Override
	public void disconnect() {
		socket.close();
        context.term();
	}
	
	@Override
	public boolean isBusy() {
		// TODO Auto-generated method stub
		return isBusy;
	}
}
