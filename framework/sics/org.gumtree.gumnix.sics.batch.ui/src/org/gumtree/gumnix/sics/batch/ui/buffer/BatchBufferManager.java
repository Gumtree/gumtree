package org.gumtree.gumnix.sics.batch.ui.buffer;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.gumtree.gumnix.sics.batch.ui.internal.Activator;
import org.gumtree.gumnix.sics.control.ISicsListener;
import org.gumtree.gumnix.sics.core.ISicsManager;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.ISicsCallback;
import org.gumtree.gumnix.sics.io.ISicsProxy;
import org.gumtree.gumnix.sics.io.ISicsProxy.ProxyState;
import org.gumtree.gumnix.sics.io.ISicsProxyListener;
import org.gumtree.gumnix.sics.io.ISicsReplyData;
import org.gumtree.gumnix.sics.io.SicsCallbackAdapter;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;
import org.gumtree.util.LoopRunnerStatus;
import org.gumtree.util.PlatformUtils;
import org.gumtree.util.bean.AbstractModelObject;

@SuppressWarnings("restriction")
public class BatchBufferManager extends AbstractModelObject implements IBatchBufferManager {

	// Schedule to check queue every 500ms
	private static final int SCHEDULING_INTERVAL = 500;
	
	// The batch buffer container
	private List<IBatchBuffer> batchBufferQueue;
	
	// The support back-end service
	private ISicsManager sicsManager;
	
	// Current status
	private BatchBufferManagerStatus status = BatchBufferManagerStatus.DISCONNECTED;
	
	// Scheduler
	private Job scheduler;
	
	// Flag to run
	private boolean autoRun;
	
	// Execution lock
	private Object executionLock = new Object();
	
	// Proxy listener
	private ISicsProxyListener proxyListener;
	
	// SICS batch buffer manager callback
	private ISicsCallback exeInterestCallback;
	
	// Interrupt listener
	private ISicsListener sicsListener;
	
	public BatchBufferManager() {
		super();
		batchBufferQueue = new BatchBufferQueue(this, "batchBufferQueue");
		ContextInjectionFactory.inject(batchBufferQueue, Activator.getDefault()
				.getEclipseContext());
		setStatus(BatchBufferManagerStatus.DISCONNECTED);
		// Handles proxy connect and disconnect events
		proxyListener = new SicsProxyListenerAdapter() {
			public void proxyDisconnected() {
				handleSicsDisconnect();
			}
			public void proxyConnected() {
				handleSicsConnect();
			}
		};
		// Setup scheduler
		scheduler = new Job("") {
			protected IStatus run(IProgressMonitor monitor) {
				// Execute if:
				// * Batch buffer manager is connected and idle
				// * Queue is non empty
				// * Triggered to run
				if (getStatus().equals(BatchBufferManagerStatus.IDLE) && isAutoRun()) {
					if (getBatchBufferQueue().size() > 0) {
						try {
							IBatchBuffer buffer = (IBatchBuffer) getBatchBufferQueue().remove(0);
							execute(buffer);
						} catch (Exception e) {
							// TODO
						}
					} else {
						setAutoRun(false);
					}
				}
				// Continue if not disconnected
				if (!getStatus().equals(BatchBufferManagerStatus.DISCONNECTED)) {
					schedule(SCHEDULING_INTERVAL);
				}
				return Status.OK_STATUS;
			}
		};
		scheduler.setSystem(true);
	}
	
	protected void handleSicsConnect() {
		// Discover current batch buffer manager status
		ISicsReplyData data = syncSend("exe info");
		if (data.getString().equalsIgnoreCase("Idle")) {
			setStatus(BatchBufferManagerStatus.IDLE);
		} else {
			setStatus(BatchBufferManagerStatus.EXECUTING);
		}
		// Listen to exe interest on the status channel
		exeInterestCallback = new SicsCallbackAdapter() {
			public void receiveWarning(ISicsReplyData data) {
				if (data.getString().startsWith("BATCHSTART=")) {
					// Handle start
					setStatus(BatchBufferManagerStatus.EXECUTING);
				} else if (data.getString().startsWith("BATCHEND=")) {
					// Handle end
					setStatus(BatchBufferManagerStatus.IDLE);
				} else {
					// Handle range info
					handleExecutionEvent(data.getString());
				}
			}
		};
		asyncSend("exe interest", exeInterestCallback, ISicsProxy.CHANNEL_STATUS);
		// Set interest on raw batch channel for logging
		asyncSend("exe interest", null, ISicsProxy.CHANNEL_RAW_BATCH);
		// Listen to interrupt event
		sicsListener = new ISicsListener() {
			public void interrupted(int level) {
				if (level >= 3) {
					// Batch is interrupt with level 3 or above
					setStatus(BatchBufferManagerStatus.IDLE);
					// Pause for the rest of queue
					setAutoRun(false);
				}
			}
		};
		getSicsManager().monitor().addSicsListener(sicsListener);
		// Schedule queue
		scheduler.schedule();
	}
	
	protected void handleSicsDisconnect() {
		if (exeInterestCallback != null) {
			exeInterestCallback.setCallbackCompleted(true);
			exeInterestCallback = null;
		}
		if (sicsListener != null) {
			getSicsManager().monitor().removeSicsListener(sicsListener);
			sicsListener = null;
		}
		// Set manager to disconnected state
		setStatus(BatchBufferManagerStatus.DISCONNECTED);
		// Unschedule queue
		scheduler.cancel();
	}
	
	/***************************************************************
	 * Getters and setters 
	 ***************************************************************/
	public ISicsManager getSicsManager() {
		if (sicsManager == null) {
			setSicsManager(SicsCore.getSicsManager());
		}
		return sicsManager;
	}
	
	public void setSicsManager(ISicsManager sicsManager) {
		// Disposes old sics
		if (this.sicsManager != null) {
			this.sicsManager.proxy().removeProxyListener(proxyListener);
		}
		// Sets manager
		this.sicsManager = sicsManager;
		// Bind sics if it is connected
		if (this.sicsManager.proxy().getProxyState().equals(ProxyState.CONNECTED)) {
			handleSicsConnect();
		}
		// Prepare new sics
		this.sicsManager.proxy().addProxyListener(proxyListener);
	}
	
	public boolean isAutoRun() {
		return autoRun;
	}
	
	public void setAutoRun(boolean autoRun) {
		Object oldValue = this.autoRun;
		this.autoRun = autoRun;
		firePropertyChange("autoRun", oldValue, autoRun);
	}
	
	public List<IBatchBuffer> getBatchBufferQueue() {
		return batchBufferQueue;
	}
	
	/***************************************************************
	 * Operations
	 ***************************************************************/
	private void execute(IBatchBuffer buffer) {
		synchronized (executionLock) {
			// Go to preparing mode
			setStatus(BatchBufferManagerStatus.PREPARING);
			// Ready to upload
			asyncSend("exe clear", null);
			asyncSend("exe clearupload", null);
			asyncSend("exe upload", null);
			// Upload
			BufferedReader reader = new BufferedReader(new StringReader(buffer.getContent()));
			String line = null;
			try {
				while((line = reader.readLine()) != null) {
					asyncSend("exe append " + line, null);
				}
			} catch (Exception e) {
				// TODO
			}
			// Save
			asyncSend("exe forcesave " + buffer.getName(), null);
			// Enqueue (due to the delay in general channel, wait until it is ready)
			final boolean[] enqueued = new boolean[] { false };
			asyncSend("exe enqueue " + buffer.getName(), new SicsCallbackAdapter() {
				public void receiveReply(ISicsReplyData data) {
					enqueued[0] = true;
				}
			});
			LoopRunner.run(new ILoopExitCondition() {				
				public boolean getExitCondition() {
					return enqueued[0];
				}
			});
			// Execute (in the raw batch channel)
			asyncSend("exe run", null, ISicsProxy.CHANNEL_RAW_BATCH);
		}
	}
	
	/***************************************************************
	 * Status
	 ***************************************************************/
	public String getBuffer(String buffername) {
		if (buffername != null) {
			ISicsReplyData data = syncSend("exe print " + buffername);
			// It will be null if not found
			return data.getString();
		}
		return null;
	}

//	public String[] getQueuedBuffers() {
//		ISicsReplyData data = syncSend("exe queue");
//		if (data.getString().equalsIgnoreCase("OK")) {
//			// Case for empty queue
//			return new String[0];
//		} else {
//			// Case for non empty queue
//			List<String> buffernames = new ArrayList<String>();
//			for (String buffername : data.getString().split("\n")) {
//				buffername = buffername.trim();
//				// Only store non empty line
//				if (buffername.length() > 0) {
//					buffernames.add(buffername);
//				}
//			}
//			return buffernames.toArray(new String[buffernames.size()]);
//		}
//	}

	// Return null if no running buffer
	public String getRunningBuffername() {
		ISicsReplyData data = syncSend("exe info");
		// Either in format of "Idle" or "Executing xxx"
		// So we look for space delimeter
		if (data.getString().indexOf(" ") > 0) {
			return data.getString().substring(data.getString().indexOf(" ") + 1);
		}
		return null;
	}
	
	public String getRunningBufferContent() {
		return getBuffer(getRunningBuffername());
	}
	
	public String getRunningBufferRangeString() {
		ISicsReplyData data = syncSend("exe info range");
		// Either in format of "Idle" or "Executing xxx"
		// So we look for space delimeter
		if (data.getString().contains(".range")) {
			return data.getString();
		}
		return null;
	}
	
	public BatchBufferManagerStatus getStatus() {
		return status;
	}
	
	protected void setStatus(BatchBufferManagerStatus status) {
		synchronized (this.status) {
			// Sets status
			this.status = status;
			// Fires event
			PlatformUtils.getPlatformEventBus().postEvent(
					new BatchBufferManagerStatusEvent(this, status));
		}
	}

	/***************************************************************
	 * Event
	 ***************************************************************/
	public void addEventHandler(
			IEventHandler<BatchBufferManagerEvent> eventHandler) {
		PlatformUtils.getPlatformEventBus().subscribe(this, eventHandler);
	}

	public void removeEventHandler(
			IEventHandler<BatchBufferManagerEvent> eventHandler) {
		PlatformUtils.getPlatformEventBus().unsubscribe(this, eventHandler);
	}
	
	// Handle range message
	private void handleExecutionEvent(String message) {
		String[] rangeInfos = message.split("=");
		for (int i = 0; i < rangeInfos.length; i++) {
			rangeInfos[i] = rangeInfos[i].trim(); 
		}
		// remove ".range" to get buffername
		String buffername = rangeInfos[0].substring(0, rangeInfos[0].length() - 6);
		long startPosition = Long.parseLong(rangeInfos[1]);
		long endPosition = Long.parseLong(rangeInfos[1]);
		// Fire event
		PlatformUtils.getPlatformEventBus().postEvent(
				new BatchBufferManagerExecutionEvent(this, buffername,
						startPosition, endPosition));
	}
	
	/***************************************************************
	 * I/O
	 ***************************************************************/
	protected ISicsReplyData syncSend(String command) {
		// Run with standard timeout of 20 sec
		return syncSend(command, 20 * 1000);
	}
	
	protected ISicsReplyData syncSend(String command, int timeout) {
		final ISicsReplyData[] replyData = new ISicsReplyData[1];
		final String[] errorMessage = new String[1];
		try {
			getSicsManager().proxy().send(command, new SicsCallbackAdapter() {
				public void receiveReply(ISicsReplyData data) {
					replyData[0] = data;
					// One off
					setCallbackCompleted(true);
				}
				public void receiveError(ISicsReplyData data) {
					errorMessage[0] = data.getString();
					// One off
					setCallbackCompleted(true);
				}
			});
		} catch (SicsIOException e) {
			throw new BatchBufferManagerException(e);
		}
		// Wait
		LoopRunnerStatus waitStatus = LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return replyData[0] != null || errorMessage[0] != null;
			}
		}, timeout);
		// Handle timeout
		if (waitStatus.equals(LoopRunnerStatus.TIMEOUT)) {
			throw new BatchBufferManagerException("Timeout on sending command " + command);
		}
		// Handle error or normal reply
		if (errorMessage[0] != null) {
			throw new BatchBufferManagerException(errorMessage[0]);
		} else {
			return replyData[0];
		}
	}
	
	protected void asyncSend(String command, ISicsCallback callback) {
		asyncSend(command, callback, ISicsProxy.CHANNEL_GENERAL);
	}
	
	protected void asyncSend(String command, ISicsCallback callback,
			String channelId) {
		try {
			getSicsManager().proxy().send(command, callback, channelId);
		} catch (SicsIOException e) {
			throw new BatchBufferManagerException(e);
		}
	}

}
