package org.gumtree.sics.batch;

import java.util.List;

import org.eclipse.core.runtime.jobs.Job;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.sics.core.ISicsManager;
import org.gumtree.sics.core.support.SicsManager;
import org.gumtree.sics.io.ISicsCallback;
import org.gumtree.sics.io.ISicsData;
import org.gumtree.sics.io.ISicsProxy;
import org.gumtree.sics.io.ISicsProxy.ProxyState;
import org.gumtree.sics.io.SicsCallbackAdapter;
import org.gumtree.sics.io.SicsEventHandler;
import org.gumtree.sics.io.SicsIOException;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;
import org.gumtree.util.LoopRunnerStatus;
import org.gumtree.util.PlatformUtils;
import org.gumtree.util.bean.AbstractModelObject;
import org.osgi.service.event.Event;

public class BatchBufferManager extends AbstractModelObject implements IBatchBufferManager {

//	private static final String TCL_SCRIPT_COPY_FOLDER_PROPERTY = "gumtree.sics.tclScriptCopyFolder";
	
//	private static final String TCL_ESCAPE_CURLY_BRACKETS_PROPERTY = "gumtree.sics.escapeCurlyBrackets";
	
	// Schedule to check queue every 500ms
//	private static final int SCHEDULING_INTERVAL = 500;
	
	// The batch buffer container
	private List<IBatchBuffer> batchBufferQueue;
	
	// The support back-end service
	private ISicsManager sicsManager;
	
	// Current status
	private BatchBufferManagerStatus status = BatchBufferManagerStatus.DISCONNECTED;
	
	// Scheduler
	private Job scheduler;
	
	// Flag to run
//	private boolean autoRun;
	
	// Execution lock
//	private Object executionLock = new Object();
	
	// Proxy listener
	private SicsEventHandler proxyListener;
	
	// SICS batch buffer manager callback
	private ISicsCallback exeInterestCallback;
	
//	private boolean escapeBrackets = false;
	
	public BatchBufferManager(SicsManager sicsManager) {
		super();
		this.sicsManager = sicsManager;
		setStatus(BatchBufferManagerStatus.DISCONNECTED);
		// Handles proxy connect and disconnect events
		proxyListener = new SicsEventHandler(ISicsProxy.EVENT_TOPIC_PROXY_ALL) {
			@Override
			public void handleSicsEvent(Event event) {
				if (getTopic(event).equals(
						ISicsProxy.EVENT_TOPIC_PROXY_STATE_CONNECTED)) {
					handleSicsConnect();
				} else if (getTopic(event).equals(
						ISicsProxy.EVENT_TOPIC_PROXY_STATE_DISCONNECTED)) {
					handleSicsDisconnect();
				} else if (getTopic(event).equals(
						ISicsProxy.EVENT_TOPIC_PROXY_MESSAGE_RECEIVED)) {
					// Handle interrupt event
					String message = getString(event,
							ISicsProxy.EVENT_PROP_MESSAGE);
					if (message.startsWith("INTERRUPT")) {
						try {
							// Get interrupt level
							setStatus(BatchBufferManagerStatus.IDLE);
						} catch (Exception e) {
						}
					}
				}
			}
		};
		// Setup scheduler
//		scheduler = new Job("") {
//			protected IStatus run(IProgressMonitor monitor) {
//				// Execute if:
//				// * Batch buffer manager is connected and idle
//				// * Queue is non empty
//				// * Triggered to run
//				if (getStatus().equals(BatchBufferManagerStatus.IDLE) && isAutoRun()) {
//					if (getBatchBufferQueue().size() > 0) {
//						try {
//							IBatchBuffer buffer = (IBatchBuffer) getBatchBufferQueue().remove(0);
//							execute(buffer);
//						} catch (Exception e) {
//							// TODO
//						}
//					} else {
//						setAutoRun(false);
//					}
//				}
//				// Continue if not disconnected
//				if (!getStatus().equals(BatchBufferManagerStatus.DISCONNECTED)) {
//					schedule(SCHEDULING_INTERVAL);
//				}
//				return Status.OK_STATUS;
//			}
//		};
//		scheduler.setSystem(true);
//		try {
//			escapeBrackets = Boolean.valueOf(System.getProperty(TCL_ESCAPE_CURLY_BRACKETS_PROPERTY));
//		} catch (Exception e) {
//		}
	}
	
	public void setProxy(ISicsProxy proxy) {
		proxyListener.setProxyId(proxy.getId());
		proxyListener.activate();
		if (proxy.isConnected()) {
			handleSicsConnect();
		}
	}
	
	protected void handleSicsConnect() {
		// Discover current batch buffer manager status
		ISicsData data = syncSend("exe info");
		if (data.getString().equalsIgnoreCase("Idle")) {
			setStatus(BatchBufferManagerStatus.IDLE);
		} else {
			setStatus(BatchBufferManagerStatus.EXECUTING);
		}
		// Listen to exe interest on the status channel
		exeInterestCallback = new SicsCallbackAdapter() {
			public void receiveWarning(ISicsData data) {
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
	}
	
	protected void handleSicsDisconnect() {
		if (exeInterestCallback != null) {
			exeInterestCallback.setCallbackCompleted(true);
			exeInterestCallback = null;
		}
		// Set manager to disconnected state
		setStatus(BatchBufferManagerStatus.DISCONNECTED);
		// Unschedule queue
	}
	
	/***************************************************************
	 * Getters and setters 
	 ***************************************************************/
	public ISicsManager getSicsManager() {
//		if (sicsManager == null) {
//			setSicsManager(SicsCore.getSicsManager());
//		}
		return sicsManager;
	}
	
	public void setSicsManager(ISicsManager sicsManager) {
		// Disposes old sics
		if (this.sicsManager != null) {
			proxyListener.deactivate();
		}
		// Sets manager
		this.sicsManager = sicsManager;
		// Bind sics if it is connected
		if (this.sicsManager.getProxy().getProxyState().equals(ProxyState.CONNECTED)) {
			handleSicsConnect();
		}
		// Prepare new sics
		proxyListener.activate();
	}
	
	public List<IBatchBuffer> getBatchBufferQueue() {
		return batchBufferQueue;
	}
	
	/***************************************************************
	 * Operations
	 ***************************************************************/
//	private void execute(IBatchBuffer buffer) {
//		synchronized (executionLock) {
//			// Go to preparing mode
//			setStatus(BatchBufferManagerStatus.PREPARING);
//			// Ready to upload
//			asyncSend("exe clear", null, ISicsProxy.CHANNEL_RAW_BATCH);
//			asyncSend("exe clearupload", null, ISicsProxy.CHANNEL_RAW_BATCH);
//			asyncSend("exe upload", null, ISicsProxy.CHANNEL_RAW_BATCH);
//			// Upload
//			BufferedReader reader = new BufferedReader(new StringReader(buffer.getContent()));
//			String line = null;
//			try {
//				while((line = reader.readLine()) != null) {
//					if (escapeBrackets) {
//						line = line.replace("{", "\\{").replace("}", "\\}");
//					}
//					asyncSend("exe append " + line, null, ISicsProxy.CHANNEL_RAW_BATCH);
//				}
//			} catch (Exception e) {
//				handleExecutionEvent("failed to append line: " + line);
//			}
//			// Save
//			asyncSend("exe forcesave " + buffer.getName(), null, ISicsProxy.CHANNEL_RAW_BATCH);
//			// Enqueue (due to the delay in general channel, wait until it is ready)
//			final boolean[] enqueued = new boolean[] { false };
//			asyncSend("exe enqueue " + buffer.getName(), new SicsCallbackAdapter() {
//				public void receiveReply(ISicsData data) {
//					enqueued[0] = true;
//				}
//			}, ISicsProxy.CHANNEL_RAW_BATCH);
//			LoopRunner.run(new ILoopExitCondition() {				
//				public boolean getExitCondition() {
//					return enqueued[0];
//				}
//			});
//			// Execute (in the raw batch channel)
//			asyncSend("exe run", null, ISicsProxy.CHANNEL_RAW_BATCH);
//			
//			String folderProperty = System.getProperty(TCL_SCRIPT_COPY_FOLDER_PROPERTY);
//			if (folderProperty != null) {
//				try {
//					File folder = new File(folderProperty);
//					if (!folder.exists()){
//						folder.mkdirs();
//					}
//					String timeStamp = new SimpleDateFormat("yyMMdd_HHmmss").format(Calendar.getInstance().getTime());
//					String newFilename = folderProperty + "/" + timeStamp + "_" + buffer.getName();
//					if (!newFilename.toLowerCase().endsWith(".tcl")){
//						newFilename += ".tcl";
//					}
//					File newFile = new File(newFilename);
//					if (!newFile.exists()){
//						newFile.createNewFile();
//					}
//					BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
//					writer.write(buffer.getContent());
//					writer.flush();
//					writer.close();
//				} catch (Exception e) {
//				}
//			}
//		}
//	}
	
	/***************************************************************
	 * Status
	 ***************************************************************/
	public String getBuffer(String buffername) {
		if (buffername != null) {
			ISicsData data = syncSend("exe print " + buffername);
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
		ISicsData data = syncSend("exe info");
		// Either in format of "Idle" or "Executing xxx"
		// So we look for space delimeter
		if (data.getString().indexOf(" ") > 0) {
			return data.getString().substring(data.getString().indexOf(" ") + 1);
		}
		return null;
	}
	
	public String getRunningText() {
		ISicsData data = syncSend("exe info text");
		// Either in format of "Idle" or "Executing xxx"
		// So we look for space delimeter
		if (data.getString().indexOf(" ") > 0) {
			return data.getString();
		}
		return null;
	}
	
	public String getRunningBufferContent() {
		return getBuffer(getRunningBuffername());
	}
	
	public String getRunningBufferRangeString() {
		ISicsData data = syncSend("exe info range");
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
	protected ISicsData syncSend(String command) {
		// Run with standard timeout of 20 sec
		return syncSend(command, 20 * 1000);
	}
	
	protected ISicsData syncSend(String command, int timeout) {
		final ISicsData[] replyData = new ISicsData[1];
		final String[] errorMessage = new String[1];
		try {
			getSicsManager().getProxy().send(command, new SicsCallbackAdapter() {
				public void receiveReply(ISicsData data) {
					replyData[0] = data;
					// One off
					setCallbackCompleted(true);
				}
				public void receiveError(ISicsData data) {
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
			getSicsManager().getProxy().send(command, callback, channelId);
		} catch (SicsIOException e) {
			throw new BatchBufferManagerException(e);
		}
	}

}
