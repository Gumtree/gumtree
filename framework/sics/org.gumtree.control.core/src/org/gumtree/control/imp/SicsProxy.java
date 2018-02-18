/**
 * 
 */
package org.gumtree.control.imp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gumtree.control.core.ISicsCallback;
import org.gumtree.control.core.ISicsChannel;
import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.core.ServerStatus;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.exception.SicsCommunicationException;
import org.gumtree.control.exception.SicsException;

/**
 * @author nxi
 *
 */
public class SicsProxy implements ISicsProxy {

	private String serverAddress;
	private String publisherAddress;
	private ISicsChannel channel;
	private ServerStatus serverStatus;
	private boolean isInterrupted;
	private List<ISicsProxyListener> proxyListeners;
	
	public SicsProxy() {
		serverStatus = ServerStatus.UNKNOWN;
		proxyListeners = new ArrayList<ISicsProxyListener>();
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.control.core.ISicsProxy#connect()
	 */
	@Override
	public boolean connect(String serverAddress, String publisherAddress) {
		this.serverAddress = serverAddress;
		this.publisherAddress = publisherAddress;
		channel = new SicsChannel();
		try {
			channel.connect(serverAddress, publisherAddress);
		} catch (Exception e) {
			return false;
		}
		try {
			serverStatus = ServerStatus.parseStatus(channel.send("status", null));
		} catch (SicsException e) {
		}
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
		// TODO Auto-generated method stub
		return channel.isConnected();
	}

	@Override
	public String send(String command) throws SicsException {
		return send(command, null);
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.control.core.ISicsProxy#send(java.lang.String, org.gumtree.control.core.ISicsCallback, java.lang.String)
	 */
	@Override
	public String send(String command, ISicsCallback callback) throws SicsException {
		if (channel != null && channel.isConnected()) {
			return channel.send(command, callback);
		} else {
			throw new SicsCommunicationException("not connected");
		}
	}

	/* (non-Javadoc)
	 * @see org.gumtree.control.core.ISicsProxy#syncRun(java.lang.String)
	 */
	@Override
	public String syncRun(String command) throws SicsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISicsChannel getSicsChannel() {
		// TODO Auto-generated method stub
		return channel;
	}

	@Override
	public ServerStatus getServerStatus() {
		// TODO Auto-generated method stub
		return serverStatus;
	}

	@Override
	public void setServerStatus(ServerStatus status) {
		serverStatus = status;
	}
	
	@Override
	public boolean multiDrive(Map<String, Number> devices) throws SicsException {
		if (devices.size() > 0) {
			String command = "drive";
			for (String key : devices.keySet()) {
				Number value = devices.get(key);
				command += " " + key + " " + value;
			}
			send(command, null);
		}
		return false;
	}
	
	@Override
	public void interrupt() {
		try {
			send("INT1712 3", null);
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

	@Override
	public void clearInterruptFlag() {
		isInterrupted = false;
		fireInterruptEvent(isInterrupted);
	}
	
}
