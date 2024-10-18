/**
 * 
 */
package org.gumtree.control.imp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gumtree.control.batch.BatchControl;
import org.gumtree.control.batch.IBatchControl;
import org.gumtree.control.core.ISicsChannel;
import org.gumtree.control.core.ISicsConnectionContext;
import org.gumtree.control.core.ISicsModel;
import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.core.ServerStatus;
import org.gumtree.control.core.SicsCoreProperties;
import org.gumtree.control.events.ISicsCallback;
import org.gumtree.control.events.ISicsMessageListener;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.exception.SicsCommunicationException;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.exception.SicsModelException;
import org.gumtree.control.model.SicsModel;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.JobRunner;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nxi
 *
 */
public class SicsProxy implements ISicsProxy {

	private static Logger logger = LoggerFactory.getLogger(SicsProxy.class);
	private static final int EPOCH_PERIOD = 3000;
	private static final int EPOCH_RETRY = 1;

	private String serverAddress;
	private String publisherAddress;
	private ISicsChannel channel;
	private ServerStatus serverStatus;
	private IBatchControl batchControl;
	private boolean isConnected;
	private boolean isModelAvailable;
	private boolean isInterrupted;
    private boolean isBroken;
    private boolean keepConnection;
	private ISicsModel sicsModel;
	private List<ISicsProxyListener> proxyListeners;
	private List<ISicsMessageListener> messageListeners;
	private ISicsConnectionContext connectionContext;
	private Thread monitorThread;
	
	public SicsProxy() {
		serverStatus = ServerStatus.UNKNOWN;
		proxyListeners = new ArrayList<ISicsProxyListener>();
		messageListeners = new ArrayList<ISicsMessageListener>();
		batchControl = new BatchControl(this);
		connectionContext = new SicsConnectionContext();
		keepConnection = SicsCoreProperties.KEEP_CONNECTION.getBoolean();
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.control.core.ISicsProxy#connect()
	 */
	@Override
	public boolean connect(String serverAddress, String publisherAddress) throws SicsException {
		if (serverAddress != null && !serverAddress.equals(this.serverAddress)) {
			this.serverAddress = serverAddress;
			this.publisherAddress = publisherAddress;
			connectionContext.setServerAddress(serverAddress);
			connectionContext.setPublisherAddress(publisherAddress);
			if (channel != null) {
				if (channel.isConnected()) {
					channel.disconnect();
				}
			}
			channel = new SicsChannel(this);
			try {
				logger.warn("connecting to server: " + serverAddress);
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
				logger.error("failed to get status after connecting to the server");
				throw e;
			}
//			try {
//				batchStatus = BatchStatus.parseStatus(channel.send("exe info", null));
//			} catch (SicsException e) {
//			}
			getGumtreeXML();
			
			fireConnectionEvent(true);
			
			startMonitorThread();
		} else {
			return reconnect();
		}
		return true;
	}

	private void startMonitorThread() {
		isBroken = false;
		monitorThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				int fct = 0;
				while(true) {
					try {
						Thread.sleep(EPOCH_PERIOD);
					} catch (InterruptedException e1) {
						break;
					}
					if (isConnected && !isBroken && channel != null && channel.isConnected()) {
						try {
							channel.syncPoch();
							fct = 0;
						} catch (SicsCommunicationException e) {
							fct++;
							if (fct >= EPOCH_RETRY) {
								isBroken = true;
								logger.error(String.format("POCH failed for %d times, disconnect", fct));
								disconnect();
								fireConnectionBrokenEvent();
							} else {
								logger.error(String.format("POCH failed for %d time(s), retry in %d milliseconds", fct, EPOCH_PERIOD));
							}
						} catch (Exception e) {
						}
					} else if (keepConnection && !isConnected && channel != null) {
						try {
							reconnect();
						} catch (SicsException e) {
							logger.error("Reconnection failed");
						} catch (Exception e) {
							logger.error("falied to run reconnection function.", e);
						}
					}
				}
			}
		});
		monitorThread.start();
	}
	
	public boolean reconnect() throws SicsException {
//		if (channel != null && channel.isConnected()) {
//			channel.disconnect();
//		} 
//		channel = new SicsChannel(this);
//		try {
//			channel.connect(serverAddress, publisherAddress);
//		} catch (Exception e) {
//			return false;
//		}
		if (channel == null || !channel.isConnected()) {
			channel = new SicsChannel(this);
			try {
				logger.warn("reconnect to " + serverAddress);
				channel.connect(serverAddress, publisherAddress);
			} catch (Exception e) {
				logger.error("Failed to get status from server after reconnecting. Disconnect now.");
				disconnect();
				return false;
			}
		} 
		try {
			logger.warn("connection reestablished");
			serverStatus = ServerStatus.parseStatus(channel.syncSend("status", null));
		} catch (Exception e) {
			if (keepConnection) {
				logger.error("Failed to get status from server after reconnecting. Set status to UNKNOWN.");
				setServerStatus(ServerStatus.UNKNOWN);
			} else {
				logger.error("Failed to get status from server after reconnecting. Disconnect now.");
				disconnect();
			}
			return false;
//			throw e;
		}

//		if (sicsModel == null) {
			getGumtreeXML();
//		}

		fireConnectionEvent(true);
		isBroken = false;
		if (monitorThread == null) {
			startMonitorThread();
		}
//		} catch (SicsException e) {
//			
//		}
//		try {
//			batchStatus = BatchStatus.parseStatus(channel.send("exe info", null));
//		} catch (SicsException e) {
//		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.control.core.ISicsProxy#disconnect()
	 */
	@Override
	public void disconnect() {
		if (channel != null) {
			logger.warn("disconnected from SICS");
			channel.disconnect();
			setServerStatus(ServerStatus.UNKNOWN);
			fireConnectionEvent(false);
		}
	}

	/* (non-Javadoc)
	 * @see org.gumtree.control.core.ISicsProxy#isConnected()
	 */
	@Override
	public boolean isConnected() {
//		return channel != null && channel.isConnected();
		return isConnected;
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
			asyncRun("INT1712 3", null);
			labelInterruptFlag();
			if (channel != null) {
				channel.reset();
			}
		} catch (SicsException e) {
		}
	}
	
	@Override
	public boolean isInterrupted() {
		if (isInterrupted) {
			clearInterruptFlag();
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void labelInterruptFlag() {
		isInterrupted = true;
		fireInterruptEvent(isInterrupted);
	}
	
	@Override
	public void addProxyListener(ISicsProxyListener listener) {
		synchronized (proxyListeners) {
			proxyListeners.add(listener);
		}
	}
	
	@Override
	public void removeProxyListener(ISicsProxyListener listener) {
		synchronized (proxyListeners) {
			proxyListeners.remove(listener);
		}
	}
	
	private void fireInterruptEvent(boolean isInterrupted) {
		synchronized (proxyListeners) {
			for (Iterator<ISicsProxyListener> iter = proxyListeners.iterator(); iter.hasNext();) {
				ISicsProxyListener listener = iter.next();
				listener.interrupt(isInterrupted);
			}
		}
	}
	
	private void fireConnectionEvent(boolean isConnected) {
		this.isConnected = isConnected;
		synchronized (proxyListeners) {
			if (isConnected) {
				for (Iterator<ISicsProxyListener> iter = proxyListeners.iterator(); iter.hasNext();) {
					ISicsProxyListener listener = iter.next();
					try {
						listener.connect();
					} catch (Exception e) {
						logger.error("failed fire connecting event", e);
					}
				}
			} else {
				for (Iterator<ISicsProxyListener> iter = proxyListeners.iterator(); iter.hasNext();) {
					ISicsProxyListener listener = iter.next();
					try {
						listener.disconnect();
					} catch (Exception e) {
						logger.error("failed fire disconnecting event", e);
					}
				}
			}
		}
	}
	
	private void fireModelUpdatedEvent() {
		synchronized (proxyListeners) {
			for (Iterator<ISicsProxyListener> iter = proxyListeners.iterator(); iter.hasNext();) {
				ISicsProxyListener listener = iter.next();
				try {
					listener.modelUpdated();
				} catch (Exception e) {
					logger.error("failed fire model updating event", e);
				}
			}
//			System.err.println("fire model updated");
		}
	}

	private void fireConnectionBrokenEvent() {
		synchronized (proxyListeners) {
			for (Iterator<ISicsProxyListener> iter = proxyListeners.iterator(); iter.hasNext();) {
				ISicsProxyListener listener = iter.next();
				try {
					listener.proxyConnectionReqested();
				} catch (Exception e) {
					logger.error("failed fire connection broken event", e);
				}
			}
//		System.err.println("fire connection broken");
		}
	}
	
	private void fireStatusEvent(ServerStatus status) {
		synchronized (proxyListeners) {
			for (Iterator<ISicsProxyListener> iter = proxyListeners.iterator(); iter.hasNext();) {
				ISicsProxyListener listener = iter.next();
				try {
					listener.setStatus(status);
				} catch (Exception e) {
					logger.error("failed fire status changing event", e);
				}
			}
		}
	}
	
	@Override
	public void clearInterruptFlag() {
		isInterrupted = false;
//		fireInterruptEvent(isInterrupted);
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
	
	public void fireMessageEvent(JSONObject message) {
		for (ISicsMessageListener listener : messageListeners) {
			try {
				listener.messageReceived(message);
			} catch (Exception e) {
				logger.error("failed fire message event", e);
			}
		}
	}
	
	public synchronized void getGumtreeXML() throws SicsException {
//		if (sicsModel == null) {
			if (sicsModel != null) {
				sicsModel.dispose();
				sicsModel = null;
			}
			try {
				String msg = channel.syncSend("getgumtreexml /", null);
				if (msg != null) {
					int idx = msg.indexOf("<");
					msg = msg.substring(idx);
					sicsModel = new SicsModel(this);
					sicsModel.loadFromString(msg);
					isModelAvailable = true;
					fireModelUpdatedEvent();
				}
			} catch (IOException e) {
				throw new SicsModelException("failed to interpret SICS model text");
			}
//		}
	}
	
	public synchronized void updateGumtreeXML() throws SicsException {
		try {
			String msg = channel.syncSend("getgumtreexml /", null);
			if (msg != null) {
				int idx = msg.indexOf("<");
				msg = msg.substring(idx);
				if (sicsModel != null) {
					sicsModel.dispose();
				}
				sicsModel = new SicsModel(this);
				sicsModel.loadFromString(msg);
				isModelAvailable = true;
				fireModelUpdatedEvent();
			}
		} catch (IOException e) {
			throw new SicsModelException("failed to interpret SICS model text");
		} finally {
		}
	}
	@Override
	public synchronized ISicsModel getSicsModel() {
		return sicsModel;
	}

	@Override
	public ISicsConnectionContext getConnectionContext() {
		return connectionContext;
	}

	/**
	 * @return the isModelAvailable
	 */
	public boolean isModelAvailable() {
		return isModelAvailable;
	}

}
