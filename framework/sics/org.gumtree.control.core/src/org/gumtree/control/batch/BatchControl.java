package org.gumtree.control.batch;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.core.ISicsReplyData;
import org.gumtree.control.core.SicsCommunicationConstants.JSONTag;
import org.gumtree.control.core.SicsCoreProperties;
import org.gumtree.control.events.ISicsCallback;
import org.gumtree.control.events.SicsCallbackAdapter;
import org.gumtree.control.events.SicsProxyListenerAdapter;
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

	private static final Logger logger = LoggerFactory.getLogger(BatchControl.class);

	// used for inner class in setBatchInterest()
//	private boolean interestSetFlag = false;
	private ISicsProxy sicsProxy;

//	private IListenerManager<IBatchListener> listenerManager;
	
	private List<IBatchListener> batchListeners;

	private ISicsCallback outputListener;

	private BatchStatus status = BatchStatus.DISCONNECTED;
	
	private String batchId = "";
	
	private String batchName = "";
	
	private String batchText = "";

	private String batchRangeText = "";
	
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
			sicsProxy.asyncRun("exe info", new SicsCallbackAdapter() {
				@Override
				public void receiveFinish(ISicsReplyData data) {
					String info = data.getString().trim();
					try {
						if (info.startsWith(BatchStatus.EXECUTING_PREFIX)) {
							if (info.length() > BatchStatus.EXECUTING_PREFIX.length()) {
								setBatchName(info.substring(BatchStatus.EXECUTING_PREFIX.length()));
								status = BatchStatus.EXECUTING;
							} else {
								status = BatchStatus.ERROR;
							}
						} else {
							status = BatchStatus.parseStatus(info);
						}
					} catch (Exception e) {
						e.printStackTrace();
						BatchControl.logger.error("failed to set batch status: " + e.getMessage());
					}
				}
			});
		} catch (SicsException e) {
			logger.error("failed to get batch status: " + e.getMessage());
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
		System.err.println("status = " + status);
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
								String sicsObject = data.getFullReply().getString(JSONTag.REPLY.getText());
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

	private class ProxyListener extends SicsProxyListenerAdapter {

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
	}

	@Override
	public void fireBatchEvent(String type, String value) {
		if (type.equals(PropertyConstants.PROP_BATCH_START)) {
//			System.err.println("batch started");
			for (IBatchListener listener : batchListeners) {
				listener.scriptChanged(value);
			}
		} else if (type.equals(PropertyConstants.PROP_BATCH_RANGE)) {
			for (IBatchListener listener : batchListeners) {
//				listener.lineExecuted(Integer.valueOf(value));;
				listener.rangeExecuted(value);
			}
		} 
		else if (type.equals(PropertyConstants.PROP_BATCH_TEXT)) {
			for (IBatchListener listener : batchListeners) {
				listener.scriptChanged(value);;
			}
		} else if (type.equals(PropertyConstants.PROP_BATCH_FINISH)) {
			for (IBatchListener listener : batchListeners) {
//				listener.lineExecuted(Integer.valueOf(value));;
				listener.stop();
			}
		}
	}

	@Override
	public void parseState(String stateName, String stateValue) {
		logger.warn("parse batch: " + stateName + " " + stateValue);
		if (stateName.equalsIgnoreCase(BatchStatus.START_STATE)) {
			setBatchName(stateValue);
			setBatchStatus(BatchStatus.EXECUTING);
		} else if (stateName.equalsIgnoreCase(BatchStatus.FINISH_STATE)) {
			clearBatchName();
			setBatchStatus(BatchStatus.IDLE);
		} else if (stateName.equalsIgnoreCase(BatchStatus.RANGE_STATE)) {
			setBatchRangeText(stateValue);
		}
	}

	public String getBatchId() {
		return batchId;
	}
	
	/**
	 * @return the batchName
	 */
	public String getBatchName() {
		return batchName;
	}

	private void setBatchName(final String batchName) {
		this.batchId = String.valueOf(System.currentTimeMillis());
		System.err.println("batch name = " + batchName);
		this.batchName = batchName;
		try {
			sicsProxy.asyncRun("exe print " + batchName, new SicsCallbackAdapter() {
				@Override
				public void receiveFinish(final ISicsReplyData data) {
					try {
						setBatchText(data.getString());
//						batchText = data.getString();
					} catch (Exception e) {
						BatchControl.logger.error("failed to get batch text of file: " + batchName);
					}
				}
			});
		} catch (SicsException e) {
			logger.error("failed to get batch status: " + e.getMessage());
		}

	}
	
	private void setBatchText(String text) {
		batchText = text;
		System.err.println("batch text = " + batchText);
		fireBatchEvent(PropertyConstants.PROP_BATCH_TEXT, text);
		getBatchRange();
	}
	
	private void clearBatchName() {
		this.batchId = "";
		this.batchName = "";
		this.batchText = "";
	}
	
	public String getBatchText() {
		return batchText;
	}
	
	public String getBatchRange() {
		if (status.equals(BatchStatus.EXECUTING)) {
			try {
				sicsProxy.asyncRun("exe info range", new SicsCallbackAdapter() {
					@Override
					public void receiveFinish(final ISicsReplyData data) {
						try {
							String range = data.getString();
							if (range.contains(".range")) {
								setBatchRangeText(range);
							}
						} catch (Exception e) {
							BatchControl.logger.error("failed to get batch text of file: " + batchName);
						}
					}
				});
			} catch (SicsException e) {
				logger.error("failed to get buffer range: " + e.getMessage());
			}
		}
		return "";
	}
	
	public class BatchInfo {
		private BatchStatus status;
		private String batchId;
		private String batchName;
		private String batchText;
		private String batchRange;
		
		public BatchInfo(BatchStatus status, String id, String name, String text, String range) {
			this.status = status;
			this.batchId = id;
			this.batchName = name;
			this.batchText = text;
			this.batchRange = range;
		}
		
		public BatchStatus getStatus() {
			return status;
		}
		
		public String getBatchId() {
			return batchId;
		}
		
		public String getBatchName() {
			return batchName;
		}
		
		public String getBatchRange() {
			return batchRange;
		}
		
		public String getBatchText() {
			return batchText;
		}
	}
	
	public BatchInfo getBatchInfo() {
		return new BatchInfo(status, batchId, batchName, batchText, batchRangeText);
	}
	
	@Override
	public void resetStatus() {
		if (status.equals(BatchStatus.PREPARING) || status.equals(BatchStatus.ERROR)) {
			setBatchStatus(BatchStatus.IDLE);
		}
	}

	/**
	 * @return the batchRangeText
	 */
	public String getBatchRangeText() {
		return batchRangeText;
	}

	/**
	 * @param batchRangeText the batchRangeText to set
	 */
	private void setBatchRangeText(String batchRangeText) {
		this.batchRangeText = batchRangeText;
		System.err.println("batch range = " + batchRangeText);
		fireBatchEvent(PropertyConstants.PROP_BATCH_RANGE, batchRangeText);
	}
}
