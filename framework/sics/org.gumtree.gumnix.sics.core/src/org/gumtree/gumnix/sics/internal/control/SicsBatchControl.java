package org.gumtree.gumnix.sics.internal.control;

import org.gumtree.gumnix.sics.control.IBatchListener;
import org.gumtree.gumnix.sics.control.ISicsBatchControl;
import org.gumtree.gumnix.sics.control.IStateMonitorListener;
import org.gumtree.gumnix.sics.core.ISicsManager;
import org.gumtree.gumnix.sics.core.SicsCoreProperties;
import org.gumtree.gumnix.sics.internal.io.SicsCommunicationConstants.JSONTag;
import org.gumtree.gumnix.sics.io.ISicsCallback;
import org.gumtree.gumnix.sics.io.ISicsProxy;
import org.gumtree.gumnix.sics.io.ISicsProxy.ProxyState;
import org.gumtree.gumnix.sics.io.ISicsReplyData;
import org.gumtree.gumnix.sics.io.SicsCallbackAdapter;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;
import org.gumtree.util.messaging.IListenerManager;
import org.gumtree.util.messaging.ListenerManager;
import org.gumtree.util.messaging.SafeListenerRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SicsBatchControl implements ISicsBatchControl {

	private static final int WAIT_INTERVAL = 10;

//	private static final String BATCH_START = "BATCHSTART";

//	private static final String BATCH_END = "BATCHEND";

	private static final String CMD_GUMPUT = "gumput";

	private static Logger logger;

	// used for inner class in setBatchInterest()
//	private boolean interestSetFlag = false;

	private IListenerManager<IBatchListener> listenerManager;

	private ISicsCallback outputListener;

	private BatchStatus status;

	private ISicsManager manager;

	private ProxyListener proxyListener;

	public SicsBatchControl(ISicsManager manager) {
		super();
		this.manager = manager;
		if(manager.proxy().getProxyState().equals(ProxyState.DISCONNECTED)) {
			setStatus(BatchStatus.DISCONNECTED);
		} else {
			setStatus(BatchStatus.IDLE);
			initialise();
		}
		proxyListener = new ProxyListener();
		manager.proxy().addProxyListener(proxyListener);
	}

	private void initialise() {
		// Prepare listener
		getOutputListener();
	}
	
//	public void run(String filename) throws SicsIOException, IOException {
//		if(getStatus() == BatchStatus.IDLE) {
//			setBatchInterest();
//			setStatus(BatchStatus.READY);
//		}
//		if(getStatus() == BatchStatus.READY) {
//			BufferedReader reader = new BufferedReader(new FileReader(filename));
//			synchronousSend("exe upload");
//			String line = null;
//			while((line = reader.readLine()) != null) {
//				synchronousSend("exe append " + line);
//			}
//			synchronousSend("exe forcesave " + BATCH_NAME);
//			synchronousSend("exe enqueue " + BATCH_NAME);
//			manager.proxy().send("exe run", null, ISicsProxy.CHANNEL_BATCH);
//		} else {
//			throw new SicsIOException("Bath system is not ready to run.");
//		}
//	}

	public void run(String[] commands, String scriptName) throws SicsIOException {
		if(getStatus() == BatchStatus.IDLE) {
//			setBatchInterest();
			setStateListener();
			setStatus(BatchStatus.READY);
		}
		if(getStatus() == BatchStatus.READY) {
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

			manager.proxy().send("exe run", getOutputListener(), ISicsProxy.CHANNEL_GENERAL);
		} else {
			throw new SicsIOException("Bath system is not ready to run.");
		}
	}

	public BatchStatus getStatus() {
		return status;
	}

	private synchronized void setStatus(final BatchStatus status) {
		this.status = status;
		getListenerManager().asyncInvokeListeners(new SafeListenerRunnable<IBatchListener>() {
			public void run(IBatchListener listener) throws Exception {
				listener.statusChanged(status);
			}			
		});
	}

	public void addListener(IBatchListener listener) {
		getListenerManager().addListenerObject(listener);
	}

	public void removeListener(IBatchListener listener) {
		getListenerManager().removeListenerObject(listener);
	}

	private IListenerManager<IBatchListener> getListenerManager() {
		if(listenerManager == null) {
			listenerManager = new ListenerManager<IBatchListener>();
		}
		return listenerManager;
	}

	private void synchronousSend(String command) throws SicsIOException {
		ISicsCallback callback = new SicsCallbackAdapter() {
			public void receiveReply(ISicsReplyData data) {
				setCallbackCompleted(true);
			}
		};
		manager.proxy().send(command, callback, ISicsProxy.CHANNEL_GENERAL);
		int timeCount = 0;
		while(callback.isCallbackCompleted()) {
			if(timeCount > SicsCoreProperties.PROXY_TIMEOUT.getLong()) {
				throw new SicsIOException("Timeout in executing" + command);
			}
			if(callback.hasError()) {
				throw new SicsIOException("Fail to execute " + command);
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
							setStatus(BatchStatus.READY);
						} else {
							try {
								String sicsObject = data.getFullReply().getString(JSONTag.OBJECT.getText());
								if(!sicsObject.equals(CMD_GUMPUT)) {
									return;
								}
								final int line = data.getInteger();
								getListenerManager().asyncInvokeListeners(new SafeListenerRunnable<IBatchListener>() {
									public void run(IBatchListener listener) throws Exception {
										listener.lineExecuted(line);
									}									
								});
							} catch (Exception e) {
							}
						}
					}
				}
			};
		}
		return outputListener;
	}

	private void setStateListener() {
		manager.control().getSicsController().addStateMonitor("exe", new IStateMonitorListener() {
			public void stateChanged(SicsMonitorState state, String infoMessage) {
				getLogger().info("state change called");
				if(state.isRunning()) {
					getLogger().info("state change called for STARTED");
					setStatus(BatchStatus.RUNNING);
				} else {
					getLogger().info("state change called for FINISH");
					setStatus(BatchStatus.READY);
				}
			}
		});
	}

//	private void setBatchInterest() throws SicsIOException {
//		String command = "exe interest";
//		ISicsCallback interestCallback = new SicsCallbackAdapter() {
//			public void receiveReply(ISicsReplyData data) {
//				interestSetFlag = true;
//			}
//			public void receiveWarning(ISicsReplyData data) {
//				String[] values = data.getString().split("=");
//				if(values.length == 2) {
////					if(values[0].equals(BATCH_START) && values[1].equals(BATCH_NAME)) {
////						setStatus(BatchStatus.RUNNING);
////					} else if(values[0].equals(BATCH_END) && values[1].equals(BATCH_NAME)) {
////						setStatus(BatchStatus.READY);
////					}
//				}
//				else if(values.length == 3) {
//					final int start = Integer.parseInt(values[1].trim());
//					final int end = Integer.parseInt(values[2].trim());
//					Set<IBatchListener> listeners = getListeners();
//					synchronized (listeners) {
//						for(final IBatchListener listener : getListeners()) {
//							Thread notifyer = new Thread(new Runnable() {
//								public void run() {
//									listener.charExecuted(start, end);
//								}
//							});
//							notifyer.run();
//						}
//					}
//				}
//			}
//		};
//		manager.proxy().send("exe interest", interestCallback, ISicsProxy.CHANNEL_BATCH);
//		int timeCount = 0;
//		while(!interestSetFlag) {
//			if(timeCount > TIME_OUT) {
//				throw new SicsIOException("Timeout in executing \"" + command + "\"");
//			}
//			if(interestCallback.hasError()) {
//				throw new SicsIOException("Fail to execute " + command);
//			}
//			try {
//				Thread.sleep(WAIT_INTERVAL);
//				timeCount += WAIT_INTERVAL;
//			} catch (InterruptedException e) {
//				logger.error("Error in sending \"exe interest\" to SICS.", e);
//			}
//		}
//	}

	public void interrupt() throws SicsIOException {
		synchronousSend("INT1712 3");
	}

	private class ProxyListener extends SicsProxyListenerAdapter {
		public void proxyConnected() {
			if(getStatus().equals(BatchStatus.DISCONNECTED)) {
				setStatus(BatchStatus.IDLE);
				if (outputListener == null) {
					initialise();
				}
			}
		}
		public void proxyDisconnected() {
			setStatus(BatchStatus.DISCONNECTED);
		}
	}

	private static Logger getLogger() {
		if(logger == null) {
			logger = LoggerFactory.getLogger(SicsBatchControl.class);
		}
		return logger;
	}

}
