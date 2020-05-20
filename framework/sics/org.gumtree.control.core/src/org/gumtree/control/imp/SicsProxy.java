/**
 * 
 */
package org.gumtree.control.imp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gumtree.control.batch.BatchControl;
import org.gumtree.control.batch.IBatchControl;
import org.gumtree.control.core.ISicsChannel;
import org.gumtree.control.core.ISicsModel;
import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.core.ServerStatus;
import org.gumtree.control.events.ISicsCallback;
import org.gumtree.control.events.ISicsMessageListener;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.exception.SicsCommunicationException;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.model.SicsModel;

import com.itextpdf.text.log.Logger;

/**
 * @author nxi
 *
 */
public class SicsProxy implements ISicsProxy {

	private String serverAddress;
	private String publisherAddress;
	private ISicsChannel channel;
	private ServerStatus serverStatus;
	private IBatchControl batchControl;
	private boolean isInterrupted;
	private ISicsModel sicsModel;
	private List<ISicsProxyListener> proxyListeners;
	private List<ISicsMessageListener> messageListeners;
	
	public SicsProxy() {
		serverStatus = ServerStatus.UNKNOWN;
		proxyListeners = new ArrayList<ISicsProxyListener>();
		messageListeners = new ArrayList<ISicsMessageListener>();
		batchControl = new BatchControl(this);
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.control.core.ISicsProxy#connect()
	 */
	@Override
	public boolean connect(String serverAddress, String publisherAddress) {
		if (serverAddress != null && !serverAddress.equals(this.serverAddress)) {
			this.serverAddress = serverAddress;
			this.publisherAddress = publisherAddress;
			channel = new SicsChannel(this);
			try {
				channel.connect(serverAddress, publisherAddress);
			} catch (Exception e) {
				return false;
			}
			try {
				String s = channel.syncSend("status", null);
				if (s.contains("=")) {
					s = s.split("=")[1].trim();
				}
				serverStatus = ServerStatus.parseStatus(s);
			} catch (SicsException e) {
			}
//			try {
//				batchStatus = BatchStatus.parseStatus(channel.send("exe info", null));
//			} catch (SicsException e) {
//			}
			fireConnectionEvent(true);
		}
		return true;
	}

	public boolean reconnect() {
		if (channel != null && channel.isConnected()) {
			channel.disconnect();
		} 
		channel = new SicsChannel(this);
		try {
			channel.connect(serverAddress, publisherAddress);
		} catch (Exception e) {
			return false;
		}
		try {
			serverStatus = ServerStatus.parseStatus(channel.syncSend("status", null));
		} catch (SicsException e) {
		}
//		try {
//			batchStatus = BatchStatus.parseStatus(channel.send("exe info", null));
//		} catch (SicsException e) {
//		}
		fireConnectionEvent(true);
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.control.core.ISicsProxy#disconnect()
	 */
	@Override
	public void disconnect() {
		if (channel != null) {
			channel.disconnect();
			fireConnectionEvent(false);
		}
	}

	/* (non-Javadoc)
	 * @see org.gumtree.control.core.ISicsProxy#isConnected()
	 */
	@Override
	public boolean isConnected() {
		return channel != null && channel.isConnected();
	}

	@Override
	public String syncRun(String command) throws SicsException {
		return syncRun(command, null);
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.control.core.ISicsProxy#send(java.lang.String, org.gumtree.control.core.ISicsCallback, java.lang.String)
	 */
	@Override
	public String syncRun(String command, ISicsCallback callback) throws SicsException {
		if (channel != null && channel.isConnected()) {
			return channel.syncSend(command, callback);
		} else {
			throw new SicsCommunicationException("not connected");
		}
	}

	@Override
	public void asyncRun(String command, ISicsCallback callback) throws SicsException {
		if (channel != null && channel.isConnected()) {
			channel.asyncSend(command, callback);
		} else {
			throw new SicsCommunicationException("not connected");
		}
	}
	
	@Override
	public ISicsChannel getSicsChannel() {
		return channel;
	}

	@Override
	public ServerStatus getServerStatus() {
		return serverStatus;
	}

	@Override
	public void setServerStatus(ServerStatus status) {
		serverStatus = status;
		fireStatusEvent(status);
	}
	
	@Override
	public boolean multiDrive(Map<String, Number> devices) throws SicsException {
		if (devices.size() > 0) {
			String command = "drive";
			for (String key : devices.keySet()) {
				Number value = devices.get(key);
				command += " " + key + " " + value;
			}
			syncRun(command, null);
		}
		return false;
	}
	
	@Override
	public void interrupt() {
		try {
			syncRun("INT1712 3", null);
		} catch (SicsException e) {
		}
	}
	
	@Override
	public boolean isInterrupted() {
		return isInterrupted;
	}
	
	@Override
	public void labelInterruptFlag() {
		isInterrupted = true;
		fireInterruptEvent(isInterrupted);
	}
	
	@Override
	public void addProxyListener(ISicsProxyListener listener) {
		proxyListeners.add(listener);
	}
	
	@Override
	public void removeProxyListener(ISicsProxyListener listener) {
		proxyListeners.remove(listener);
	}
	
	private void fireInterruptEvent(boolean isInterrupted) {
		for (ISicsProxyListener listener : proxyListeners) {
			listener.interrupt(isInterrupted);
		}
	}
	
	private void fireConnectionEvent(boolean isConnected) {
		if (isConnected) {
			for (ISicsProxyListener listener : proxyListeners) {
				listener.connect();
			}
		} else {
			for (ISicsProxyListener listener : proxyListeners) {
				listener.disconnect();
			}
		}
	}

	private void fireStatusEvent(ServerStatus status) {
		for (ISicsProxyListener listener : proxyListeners) {
			listener.setStatus(status);
		}
	}
	
	@Override
	public void clearInterruptFlag() {
		isInterrupted = false;
		fireInterruptEvent(isInterrupted);
	}

	@Override
	public IBatchControl getBatchControl() {
		return batchControl;
	}

	public void addMessageListener(ISicsMessageListener listener) {
		messageListeners.add(listener);
	}
	
	public void removeMessageListener(ISicsMessageListener listener) {
		messageListeners.remove(listener);
	}
	
	public void fireMessageEvent(String message) {
		for (ISicsMessageListener listener : messageListeners) {
			listener.messageReceived(message);
		}
	}
	
	@Override
	public synchronized ISicsModel getSicsModel() {
		if (sicsModel == null) {
			try {
				String msg = channel.syncSend("getgumtreexml /", null);
				if (msg != null) {
					int idx = msg.indexOf("<");
					msg = msg.substring(idx);
					try {
			            Files.write(Paths.get("C:\\Gumtree\\docs\\GumtreeXML\\new.xml"), msg.getBytes("UTF-8"));
			        } catch (IOException e) {
			            e.printStackTrace();
			        }
					sicsModel = new SicsModel(this);
					sicsModel.loadFromString(msg);
				}
			} catch (SicsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sicsModel;
	}
}
