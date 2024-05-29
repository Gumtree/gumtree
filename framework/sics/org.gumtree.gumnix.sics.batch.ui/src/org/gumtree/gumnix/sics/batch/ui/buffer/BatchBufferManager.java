package org.gumtree.gumnix.sics.batch.ui.buffer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.gumtree.gumnix.sics.batch.ui.buffer.BatchBufferQueue.IQueueEventListener;
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

	private static final String TCL_SCRIPT_COPY_FOLDER_PROPERTY = "gumtree.sics.tclScriptCopyFolder";
	
	private static final String TCL_ESCAPE_CURLY_BRACKETS_PROPERTY = "gumtree.sics.escapeCurlyBrackets";
	
	private static final String TCL_BATCH_FOLDER_PROPERTY = "gumtree.sics.tclBatchFolder";
	
	// Schedule to check queue every 1s
	private static final int SCHEDULING_INTERVAL = 1000;
	
	// The batch buffer container
	private BatchBufferQueue batchBufferQueue;
	
	private long timestampOnEstimation;
	
	private int estimatedTimeForBuffer;
	
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
	
	private IQueueEventListener queueEventListener;
	
	// Interrupt listener
	private ISicsListener sicsListener;
	
	private boolean escapeBrackets = false;
	
	private String batchFolderPath;
	
	public BatchBufferManager() {
		super();
		batchBufferQueue = new BatchBufferQueue(this, "batchBufferQueue");
		batchFolderPath = System.getProperty(TCL_BATCH_FOLDER_PROPERTY);
		ContextInjectionFactory.inject(batchBufferQueue, Activator.getDefault()
				.getEclipseContext());
		setStatus(BatchBufferManagerStatus.DISCONNECTED);
		
		// Handles buffer queue change event
		queueEventListener = new IQueueEventListener() {
			
			@Override
			public void queueChanged() {
				if (isAutoRun()){
					updateTimeEstimation();
				}
			}
		};
		batchBufferQueue.addQueueEventListener(queueEventListener);
		
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
//					boolean isTimeEstimationAvailable = true;
//					int time = 0;
//					List<IBatchBuffer> queue = getBatchBufferQueue();
//					for (IBatchBuffer buffer : queue) {
//						int estimation = buffer.getTimeEstimation();
//						if (estimation > 0) {
//							time += estimation;
//						} else {
//							isTimeEstimationAvailable = false;
//							break;
//						}
//					}
//					if (isTimeEstimationAvailable) {
//						estimatedTotalTime = time;
//						asyncSend("hset /experiment/gumtree_time_estimate " + time, null);
//					} else {
//						estimatedTotalTime = -1;
//						asyncSend("hset /experiment/gumtree_time_estimate -1", null);
//					}
					if (getBatchBufferQueue().size() > 0) {
						try {
							estimatedTimeForBuffer = ((IBatchBuffer) getBatchBufferQueue().get(0)).getTimeEstimation();
							timestampOnEstimation = System.currentTimeMillis();
							IBatchBuffer buffer = (IBatchBuffer) getBatchBufferQueue().remove(0);
							execute(buffer);
						} catch (Exception e) {
							handleException(e.getMessage());
//							handleExecutionEvent("failed to execute buffer: " + e.getMessage());
						}
					} else {
						asyncSend("hset /experiment/gumtree_time_estimate 0", null);
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
		try {
			escapeBrackets = Boolean.valueOf(System.getProperty(TCL_ESCAPE_CURLY_BRACKETS_PROPERTY));
		} catch (Exception e) {
		}
	}
	
	private void updateTimeEstimation() {
		boolean isTimeEstimationAvailable = true;
		int time = 0;
		List<IBatchBuffer> queue = getBatchBufferQueue();
		for (IBatchBuffer buffer : queue) {
			int estimation = buffer.getTimeEstimation();
			if (estimation > 0) {
				time += estimation;
			} else {
				isTimeEstimationAvailable = false;
				break;
			}
		}
		if (isTimeEstimationAvailable && estimatedTimeForBuffer >= 0) {
			time += estimatedTimeForBuffer - (System.currentTimeMillis() - timestampOnEstimation) / 1000;
			asyncSend("hset /experiment/gumtree_time_estimate " + (System.currentTimeMillis() / 1000 + time), null);
		} else {
			asyncSend("hset /experiment/gumtree_time_estimate 0", null);
		}
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
		if (!autoRun) {
			estimatedTimeForBuffer = 0;
			timestampOnEstimation = 0;
			asyncSend("hset /experiment/gumtree_time_estimate 0", null);
		}
		firePropertyChange("autoRun", oldValue, autoRun);
	}
	
	public List<IBatchBuffer> getBatchBufferQueue() {
		return batchBufferQueue;
	}
	
	/***************************************************************
	 * Operations
	 * @throws IOException 
	 ***************************************************************/
	private void execute(IBatchBuffer buffer) throws IOException {
		synchronized (executionLock) {
			// Go to preparing mode
			setStatus(BatchBufferManagerStatus.PREPARING);
			
//	Modified by nxi. Change the uploading strategy. Save the script in the mounted folder instead.			
//			// Ready to upload
			asyncSend("exe clear", null, ISicsProxy.CHANNEL_RAW_BATCH);
			asyncSend("exe clearupload", null, ISicsProxy.CHANNEL_RAW_BATCH);
//			asyncSend("exe upload", null, ISicsProxy.CHANNEL_RAW_BATCH);
//			// Upload
//			BufferedReader reader = new BufferedReader(new StringReader(buffer.getContent()));
//			String line = null;
//			try {
//				while((line = reader.readLine()) != null) {
//					if (escapeBrackets) {
//						line = line.replace("{", "\\{").replace("}", "\\}").replace("\"", "\\\"");
//					}
//					asyncSend("exe append " + line, null, ISicsProxy.CHANNEL_RAW_BATCH);
//				}
//			} catch (Exception e) {
//				handleExecutionEvent("failed to append line: " + line);
//			}
//			// Save
//			asyncSend("exe forcesave " + buffer.getName(), null, ISicsProxy.CHANNEL_RAW_BATCH);
			
			File folderFolder = new File(batchFolderPath);
			if (folderFolder.exists() && !folderFolder.isDirectory()) {
				throw new IOException("batch folder doesn't exist.");
			} 
			if (!folderFolder.exists()) {
				if (!folderFolder.mkdirs()) {
					throw new IOException("failed to create batch folder.");
				}
			}
			String filename = buffer.getName();
			String newTCLFilePath = batchFolderPath + "/" + filename;
			File checkFile = new File(newTCLFilePath);
			if (checkFile.exists()) {
				if (!checkFile.delete()) {
					String timeStamp = new SimpleDateFormat("yyMMdd_HHmmss").format(Calendar.getInstance().getTime());
					if (filename.contains(".")) {
						int dotIndex = filename.lastIndexOf(".");
						filename = filename.substring(0, dotIndex) + "_" + timeStamp + filename.substring(dotIndex, filename.length());
					} else {
						filename = filename + "_" + timeStamp;
					}
					newTCLFilePath = batchFolderPath + "/" + filename;
				}
			}
			BufferedReader reader = new BufferedReader(new StringReader(buffer.getContent()));

			BufferedWriter batchWriter = new BufferedWriter(new FileWriter(newTCLFilePath, false));
			
			String line = null;
			try {
				while((line = reader.readLine()) != null) {
//					if (escapeBrackets) {
//						line = line.replace("{", "\\{").replace("}", "\\}").replace("\"", "\\\"");
//					}
//					asyncSend("exe append " + line, null, ISicsProxy.CHANNEL_RAW_BATCH);
					batchWriter.write(line + "\n");
				}
			} catch (Exception e) {
//				handleExecutionEvent("failed to append line: " + line);
				handleException("failed to append line: " + line);
			} finally {
				batchWriter.flush();
				batchWriter.close();
				reader.close();
			}
			
			// Enqueue (due to the delay in general channel, wait until it is ready)
			final boolean[] enqueued = new boolean[] { false };
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
			}
			asyncSend("exe enqueue {" + filename + "}", new SicsCallbackAdapter() {
				public void receiveReply(ISicsReplyData data) {
					enqueued[0] = true;
				}
			}, ISicsProxy.CHANNEL_RAW_BATCH);
			LoopRunner.run(new ILoopExitCondition() {				
				public boolean getExitCondition() {
					return enqueued[0];
				}
			});
			// Execute (in the raw batch channel)
			asyncSend("exe run", null, ISicsProxy.CHANNEL_RAW_BATCH);
			
			String folderProperty = System.getProperty(TCL_SCRIPT_COPY_FOLDER_PROPERTY);
			if (folderProperty != null) {
				try {
					File folder = new File(folderProperty);
					if (!folder.exists()){
						folder.mkdirs();
					}
					String timeStamp = new SimpleDateFormat("yyMMdd_HHmmss").format(Calendar.getInstance().getTime());
					String newFilename = folderProperty + "/" + timeStamp + "_" + buffer.getName();
					if (!newFilename.toLowerCase().endsWith(".tcl")){
						newFilename += ".tcl";
					}
					File newFile = new File(newFilename);
					if (!newFile.exists()){
						newFile.createNewFile();
					}
					BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
					writer.write(buffer.getContent());
					writer.flush();
					writer.close();
				} catch (Exception e) {
				}
			}
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
			
			try{
				asyncSend("gumtree_status " + status, null);
			}catch (Exception e) {
			
			}
			
			if (status == BatchBufferManagerStatus.IDLE) {
				if (!isAutoRun()) {
					try{
						asyncSend("hset /experiment/gumtree_time_estimate 0", null);
					}catch (Exception e) {
					
					}
				}
			}
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
	
	private void handleException(String err) {
//		setStatus(BatchBufferManagerStatus.ERROR);
		synchronized (this.status) {
			this.status = BatchBufferManagerStatus.ERROR;
			BatchBufferManagerStatusEvent event = new BatchBufferManagerStatusEvent(this, status);
			event.setMessage(err);
			PlatformUtils.getPlatformEventBus().postEvent(event);
		}
	}
	
	// Handle range message
	private void handleExecutionEvent(String message) {
		try {
			String[] rangeInfos = message.split("=");
			for (int i = 0; i < rangeInfos.length; i++) {
				rangeInfos[i] = rangeInfos[i].trim(); 
			}
			// remove ".range" to get buffername
			String buffername = rangeInfos[0].substring(0, rangeInfos[0].length() - 6);
			long startPosition = 0;
			long endPosition = 0;
			try {
				startPosition = Long.parseLong(rangeInfos[1]);
				endPosition = Long.parseLong(rangeInfos[1]);
			} catch (Exception e) {
			}
			// Fire event
			PlatformUtils.getPlatformEventBus().postEvent(
					new BatchBufferManagerExecutionEvent(this, buffername,
							startPosition, endPosition));
		} catch (Exception e) {
//			e.printStackTrace();
		}
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

	@Override
	public void resetBufferManagerStatus() {
		
		if (getStatus() == BatchBufferManagerStatus.PREPARING || getStatus() == BatchBufferManagerStatus.ERROR){
			setStatus(BatchBufferManagerStatus.IDLE);
		}
	}
}
