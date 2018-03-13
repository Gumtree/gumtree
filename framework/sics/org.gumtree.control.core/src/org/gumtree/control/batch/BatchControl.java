package org.gumtree.control.batch;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.core.ISicsReplyData;
import org.gumtree.control.core.ServerStatus;
import org.gumtree.control.core.SicsCommunicationConstants.JSONTag;
import org.gumtree.control.core.SicsCoreProperties;
import org.gumtree.control.events.ISicsCallback;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.events.SicsCallbackAdapter;
import org.gumtree.control.exception.SicsCommunicationException;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.model.PropertyConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchControl implements IBatchControl {

	private static final int WAIT_INTERVAL = 10;

//	private static final String BATCH_START = "BATCHSTART";

//	private static final String BATCH_END = "BATCHEND";

	private static final String CMD_GUMPUT = "gumput";

	private static Logger logger;

	// used for inner class in setBatchInterest()
//	private boolean interestSetFlag = false;
	private ISicsProxy sicsProxy;

//	private IListenerManager<IBatchListener> listenerManager;
	
	private List<IBatchListener> batchListeners;

	private ISicsCallback outputListener;

	private BatchStatus status;

	private ProxyListener proxyListener;

	public BatchControl(ISicsProxy proxy) {
		super();
		sicsProxy = proxy;
		batchListeners = new ArrayList<IBatchListener>();
		if(proxy.isConnected()) {
			setBatchStatus(BatchStatus.IDLE);
			initialise();
		} else {
			setBatchStatus(BatchStatus.DISCONNECTED);
		}
		proxyListener = new ProxyListener();
		proxy.addProxyListener(proxyListener);
	}

	private void initialise() {
		try {
			status = BatchStatus.parseStatus(sicsProxy.syncRun("exe info", null));
		} catch (SicsException e) {
		}
		getOutputListener();
	}
	
	public void run(String[] commands, String scriptName) throws SicsException {
		if(getStatus() == BatchStatus.IDLE) {
//			setBatchInterest();
//			setStateListener();
//			setBatchStatus(BatchStatus.READY);
//		}
//		if(getStatus() == BatchStatus.READY) {
			synchronousSend("exe clear");
			synchronousSend("exe clearupload");
			synchronousSend("exe upload");
			int lineCount = 1;
			for(String command : commands) {
				synchronousSend("exe append " + CMD_GUMPUT + " " + lineCount++);
				// [GUMTREE-652] trim command before sending to SICS
				synchronousSend("exe append " + command.trim());
			}
			synchronousSend("exe forcesave " + scriptName);
			synchronousSend("exe enqueue " + scriptName);

			sicsProxy.syncRun("exe run", getOutputListener());
		} else {
			throw new SicsCommunicationException("Bath system is not ready to run.");
		}
	}

	public BatchStatus getStatus() {
		return status;
	}

	private synchronized void setBatchStatus(final BatchStatus status) {
		this.status = status;
		for (IBatchListener listener : batchListeners) {
			listener.statusChanged(status);
		}
	}

	public void addListener(IBatchListener listener) {
		batchListeners.add(listener);
	}

	public void removeListener(IBatchListener listener) {
		batchListeners.remove(listener);
	}

	private void synchronousSend(String command) throws SicsException {
		SicsCallbackAdapter callback = new SicsCallbackAdapter();
				
		sicsProxy.syncRun(command, callback);
		int timeCount = 0;
		while(callback.isCallbackCompleted()) {
			if(timeCount > SicsCoreProperties.PROXY_TIMEOUT.getLong()) {
				throw new SicsCommunicationException("Timeout in executing" + command);
			}
			if(callback.hasError()) {
				throw new SicsCommunicationException("Fail to execute " + command);
			}
			timeCount += WAIT_INTERVAL;
		}
	}

	private ISicsCallback getOutputListener() {
		if(outputListener == null) {
			outputListener = new SicsCallbackAdapter() {
				public void receiveError(ISicsReplyData data) {
					String message = data.getString();
					if(message != null) {
						if(message.equals("ERROR: batch processing interrupted")) {
							setBatchStatus(BatchStatus.IDLE);
						} else {
							try {
								String sicsObject = data.getFullReply().getString(JSONTag.OBJECT.getText());
								if(!sicsObject.equals(CMD_GUMPUT)) {
									return;
								}
								final int line = data.getInteger();
								for (IBatchListener listener : batchListeners) {
									listener.lineExecuted(line);
								}
							} catch (Exception e) {
							}
						}
					}
				}

			};
		}
		return outputListener;
	}


	public void interrupt() throws SicsException {
		synchronousSend("INT1712 3");
	}

	private class ProxyListener implements ISicsProxyListener {

		@Override
		public void connect() {
			if(getStatus().equals(BatchStatus.DISCONNECTED)) {
				setBatchStatus(BatchStatus.IDLE);
				if (outputListener == null) {
					initialise();
				}
			}
		}
		@Override
		public void disconnect() {
			setBatchStatus(BatchStatus.DISCONNECTED);
		}
		@Override
		public void interrupt(boolean isInterrupted) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void setStatus(ServerStatus newStatus) {
			// TODO Auto-generated method stub
			
		}
	}

	private static Logger getLogger() {
		if(logger == null) {
			logger = LoggerFactory.getLogger(BatchControl.class);
		}
		return logger;
	}

	@Override
	public void fireBatchEvent(String type, String value) {
		if (type.equals(PropertyConstants.PROP_BATCH_NAME)) {
			for (IBatchListener listener : batchListeners) {
				listener.scriptChanged(value);
			}
		} else if (type.equals(PropertyConstants.PROP_BATCH_RANGE)) {
			for (IBatchListener listener : batchListeners) {
				listener.lineExecuted(Integer.valueOf(value));;
			}
		} 
//		else if (type.equals(PropertyConstants.PROP_BATCH_TEXT)) {
//			for (IBatchListener listener : batchListeners) {
//				listener.(Integer.valueOf(value));;
//			}
//		}
	}

}
