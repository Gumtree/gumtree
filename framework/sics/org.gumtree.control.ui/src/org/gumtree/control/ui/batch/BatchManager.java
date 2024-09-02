package org.gumtree.control.ui.batch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.gumtree.control.batch.BatchStatus;
import org.gumtree.control.batch.IBatchControl;
import org.gumtree.control.batch.IBatchListener;
import org.gumtree.control.batch.IBatchScript;
import org.gumtree.control.batch.SicsMessageAdapter;
import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.core.ISicsReplyData;
import org.gumtree.control.core.ServerStatus;
import org.gumtree.control.core.SicsCommunicationConstants.JSONTag;
import org.gumtree.control.events.ISicsCallback;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.events.SicsCallbackAdapter;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.control.exception.SicsBatchException;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.imp.SicsReplyData;
import org.gumtree.control.ui.batch.BatchQueue.IQueueEventListener;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;
import org.gumtree.util.bean.AbstractModelObject;
import org.json.JSONObject;

public class BatchManager extends AbstractModelObject implements IBatchManager {

	private static final String TCL_SCRIPT_COPY_FOLDER_PROPERTY = "gumtree.sics.tclScriptCopyFolder";
	
	private static final String TCL_BATCH_FOLDER_PROPERTY = "gumtree.sics.tclBatchFolder";
	
	// Schedule to check queue every 1s
	private static final int SCHEDULING_INTERVAL = 1000;
	
	private static IBatchManager batchManger;
	
	// The batch buffer container
	private BatchQueue batchQueue;
	
	private long timestampOnEstimation;
	
	private int estimatedTimeForBuffer;
	
	// Current status
//	private BatchManagerStatus status = BatchManagerStatus.DISCONNECTED;
	private IBatchControl batchControl;
	
	private IBatchListener batchListener;
	
	// Scheduler
	private Job scheduler;
	
	// Flag to run
	private boolean autoRun;
	
	// Execution lock
	private Object executionLock = new Object();
	
	// Proxy listener
//	private ISicsProxyListener proxyListener;
	
	private SicsMessageAdapter messageListener;
	
	// SICS batch buffer manager callback
	private ISicsCallback exeInterestCallback;
	
	private IQueueEventListener queueEventListener;
	
	// Interrupt listener
//	private ISicsListener sicsListener;
	private List<IBatchManagerListener> batchManagerListeners;
	
	private String batchFolderPath;
	private ISicsProxy sicsProxy;
	
	public BatchManager(ISicsProxy sicsProxy) {
		super();
		this.sicsProxy = sicsProxy;
		batchQueue = new BatchQueue(this, "batchBufferQueue");
		batchFolderPath = System.getProperty(TCL_BATCH_FOLDER_PROPERTY);
		batchManagerListeners = new ArrayList<IBatchManagerListener>();
//		setBatchStatus(BatchManagerStatus.DISCONNECTED);
		
		// Handles buffer queue change event
		queueEventListener = new IQueueEventListener() {
			
			@Override
			public void queueChanged() {
				if (isAutoRun()){
					updateTimeEstimation();
				}
			}
		};
		batchQueue.addQueueEventListener(queueEventListener);
		
		// Handles proxy connect and disconnect events
//		proxyListener = new SicsProxyListenerAdapter() {
//			
//			@Override
//			public void interrupt(boolean isInterrupted) {
//				// Batch is interrupt with level 3 or above
////				setBatchStatus(BatchManagerStatus.IDLE);
//				// Pause for the rest of queue
//				setAutoRun(false);
//			}
//			
//			@Override
//			public void disconnect() {
//				handleSicsDisconnect();
//			}
//			
//			@Override
//			public void connect() {
//				handleSicsConnect();
//			}
//		};
//		sicsProxy.addProxyListener(proxyListener);
		batchControl = sicsProxy.getBatchControl();
		
		batchListener = new IBatchListener() {
			
			@Override
			public void stop() {
				
			}
			
			@Override
			public void statusChanged(BatchStatus newStatus) {
				fireBatchStatusEvent(newStatus);
			}
			
			@Override
			public void start() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void scriptChanged(String scriptName) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void lineExecutionError(int line) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void lineExecuted(int line) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void charExecuted(int start, int end) {
				// TODO Auto-generated method stub
				
			}
		};
		
		batchControl.addListener(batchListener);
		fireBatchStatusEvent(batchControl.getStatus());
		
//		if (sicsProxy.isConnected()) {
//			BatchStatus batchStatus = sicsProxy.getBatchControl().getStatus();
//			switch (batchStatus) {
//			case IDLE:
//				setBatchStatus(BatchManagerStatus.IDLE);
//				break;
//			case ERROR:
//				setBatchStatus(BatchManagerStatus.ERROR);
//				break;
//			case EXECUTING:
//				setBatchStatus(BatchManagerStatus.EXECUTING);
//				break;
//			case DISCONNECTED:
//				setBatchStatus(BatchManagerStatus.DISCONNECTED);
//			default:
//				setBatchStatus(BatchManagerStatus.IDLE);
//				break;
//			}
//		} else {
//			setBatchStatus(BatchManagerStatus.DISCONNECTED);
//		}

		// Setup scheduler
		scheduler = new Job("") {
			protected IStatus run(IProgressMonitor monitor) {
				// Execute if:
				// * Batch buffer manager is connected and idle
				// * Queue is non empty
				// * Triggered to run
				if (getStatus().equals(BatchStatus.IDLE) && isAutoRun()) {
//					boolean isTimeEstimationAvailable = true;
//					int time = 0;
//					List<IBatchScript> queue = getBatchBufferQueue();
//					for (IBatchScript buffer : queue) {
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
							estimatedTimeForBuffer = ((IBatchScript) getBatchBufferQueue().get(0)).getTimeEstimation();
							timestampOnEstimation = System.currentTimeMillis();
							IBatchScript buffer = (IBatchScript) getBatchBufferQueue().remove(0);
							execute(buffer);
						} catch (Exception e) {
							handleException("failed to execute buffer: " + e.getMessage());
						}
					} else {
						try {
							asyncSend("hset /experiment/gumtree_time_estimate 0", null);
						} catch (SicsBatchException e) {
							handleException(e.getMessage());
						}
						setAutoRun(false);
					}
				}
				// Continue if not disconnected
				if (!getStatus().equals(BatchStatus.DISCONNECTED)) {
//					System.err.println("schedule next");
					schedule(SCHEDULING_INTERVAL);
				}
//				System.err.println("schedule finished");
				return Status.OK_STATUS;
			}
		};
		scheduler.setSystem(true);
		if (sicsProxy.isConnected()) {
			scheduler.schedule();
		}
	}
	
	private void updateTimeEstimation() {
		boolean isTimeEstimationAvailable = true;
		int time = 0;
		List<IBatchScript> queue = getBatchBufferQueue();
		for (IBatchScript buffer : queue) {
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
			try {
				asyncSend("hset /experiment/gumtree_time_estimate " + (System.currentTimeMillis() / 1000 + time), null);
			} catch (SicsBatchException e) {
				handleException(e.getMessage());
			}
		} else {
			try {
				asyncSend("hset /experiment/gumtree_time_estimate 0", null);
			} catch (SicsBatchException e) {
				handleException(e.getMessage());
			}
		}
	}
	
//	protected void handleSicsConnect() {
//		setBatchStatus(BatchManagerStatus.IDLE);
//		try {
//			asyncSend("exe info", new SicsCallbackAdapter() {
//				@Override
//				public void receiveFinish(ISicsReplyData data) {
//					if (data.getString().equalsIgnoreCase("Idle")) {
//						setBatchStatus(BatchManagerStatus.IDLE);
//					} else {
//						setBatchStatus(BatchManagerStatus.EXECUTING);
//						try {
//							asyncSend("exe interest", null);
//						} catch (SicsBatchException e) {
//							handleException(e.getMessage());
//						}
//					}					
//				}
//			});
//
//		} catch (Exception e) {
//		}
//		
//		// Schedule queue
//		scheduler.schedule();
//	}
	
//	protected void handleSicsDisconnect() {
//		if (exeInterestCallback != null) {
//			exeInterestCallback.setCallbackCompleted(true);
//			exeInterestCallback = null;
//		}
//		// Set manager to disconnected state
//		setBatchStatus(BatchManagerStatus.DISCONNECTED);
//		// Unschedule queue
//		scheduler.cancel();
//	}
	
	public boolean isAutoRun() {
		return autoRun;
	}
	
	public void setAutoRun(boolean autoRun) {
		Object oldValue = this.autoRun;
		this.autoRun = autoRun;
		if (!autoRun) {
			estimatedTimeForBuffer = 0;
			timestampOnEstimation = 0;
			try {
				asyncSend("hset /experiment/gumtree_time_estimate 0", null);
			} catch (SicsBatchException e) {
				handleException(e.getMessage());
			}
		}
		firePropertyChange("autoRun", oldValue, autoRun);
	}
	
	public List<IBatchScript> getBatchBufferQueue() {
		return batchQueue;
	}
	
	/***************************************************************
	 * Operations
	 * @throws IOException 
	 ***************************************************************/
	private void execute(IBatchScript buffer) throws IOException {
		synchronized (executionLock) {
			// Go to preparing mode
//			setBatchStatus(BatchManagerStatus.PREPARING);
			fireBatchStatusEvent(BatchStatus.PREPARING);
			
//	Modified by nxi. Change the uploading strategy. Save the script in the mounted folder instead.			
//			// Ready to upload
			File folderFolder = new File(batchFolderPath);
			if (folderFolder.exists() && !folderFolder.isDirectory()) {
				throw new IOException("batch folder doesn't exist.");
			} 
			if (!folderFolder.exists()) {
				if (!folderFolder.mkdirs()) {
					throw new IOException("failed to create batch folder.");
				}
			}
			String newTCLFilePath = batchFolderPath + "/" + buffer.getName();
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
				asyncSend("exe enqueue {" + buffer.getName() + "}", new SicsCallbackAdapter() {
					
					public void receiveReply(ISicsReplyData data) {
						enqueued[0] = true;
					}
					
					@Override
					public void receiveError(ISicsReplyData data) {
						handleException(data.toString());
					}
					
				});
			} catch (SicsBatchException e1) {
				handleException(e1.getMessage());
			}
			LoopRunner.run(new ILoopExitCondition() {				
				public boolean getExitCondition() {
					return enqueued[0];
				}
			});
			// Execute (in the raw batch channel)
			try {
				if (messageListener != null) {
					messageListener.setEnabled(true);
				}
				asyncSend("exe run", new SicsCallbackAdapter() {
					
					public void receiveReply(ISicsReplyData data) {
//						if (getStatus() != BatchStatus.EXECUTING) {
//							setBatchStatus(BatchStatus.EXECUTING);
//						}
//						fireBatchStatusEvent(BatchStatus.PREPARING);
					}
					
					@Override
					public void receiveFinish(ISicsReplyData data) {
						setCallbackCompleted(true);
//						setBatchStatus(BatchStatus.IDLE);
					}
					
					@Override
					public void receiveError(ISicsReplyData data) {
						setCallbackCompleted(true);
//						setBatchStatus(BatchStatus.ERROR);
						fireBatchStatusEvent(BatchStatus.ERROR);
					}
					
					@Override
					public void setCallbackCompleted(boolean completed) {
						super.setCallbackCompleted(completed);
						if (completed) {
							if (messageListener != null) {
								messageListener.setEnabled(false);
							}
						}
					}

				});
			} catch (SicsBatchException e1) {
				handleException(e1.getMessage());
			}
			
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
	public void getBuffer(String buffername, ISicsCallback callback) {
		if (buffername != null) {
			try {
				asyncSend("exe print " + buffername, callback);
			} catch (SicsBatchException e) {
				handleException(e.getMessage());
			}
		}
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
	public void getRunningBuffername(ISicsCallback callback) {
		try {
			asyncSend("exe info", callback);
		} catch (SicsBatchException e) {
			handleExecutionEvent(e.getMessage());
		}
	}
	
	public void getRunningBufferContent(final ISicsCallback callback) {
		getRunningBuffername(new SicsCallbackAdapter() {
			@Override
			public void receiveFinish(ISicsReplyData data) {
				if (data.getString().indexOf(" ") > 0) {
					String scriptName = data.getString().substring(data.getString().indexOf(" ") + 1);
					getBuffer(scriptName, callback);
				}
			}
		});
	}
	
	public void getRunningBufferRangeString(ISicsCallback callback) {
		try {
			asyncSend("exe info range", callback);
		} catch (Exception e) {
			handleExecutionEvent(e.getMessage());
		}
	}

//	public BatchManagerStatus getBatchStatus() {
//		return status;
//	}
	
//	protected void setBatchStatus(BatchStatus status) {
//		synchronized (this.status) {
//			// Sets status
//			this.status = status;
//			
////			if (status == BatchStatus.IDLE) {
////				if (!isAutoRun()) {
////					try{
////						asyncSend("hset /experiment/gumtree_time_estimate 0", null);
////					}catch (Exception e) {
////					
////					}
////				}
////			}
//			// Fires event
//			fireBatchStatusEvent(status);
//		}
//	}

	/***************************************************************
	 * Event
	 ***************************************************************/
	@Override
	public void addBatchManagerListener(IBatchManagerListener listener) {
		batchManagerListeners.add(listener);
	}

	@Override
	public void removeBatchManagerListener(IBatchManagerListener listener) {
		batchManagerListeners.remove(listener);
	}

	private void fireBatchStatusEvent(BatchStatus status) {
		for (IBatchManagerListener listener : batchManagerListeners) {
			listener.statusChanged(status);
		}
	}
	
	private void handleException(String err) {
		synchronized (batchControl.getStatus()) {
//			this.status = BatchManagerStatus.ERROR;
			fireBatchStatusEvent(BatchStatus.ERROR);
//			BatchScriptManagerStatusEvent event = new BatchScriptManagerStatusEvent(this, status);
//			event.setMessage(err);
//			PlatformUtils.getPlatformEventBus().postEvent(event);
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
//			PlatformUtils.getPlatformEventBus().postEvent(
//					new BatchBufferManagerExecutionEvent(this, buffername,
//							startPosition, endPosition));
			fireBatchScriptEvent(buffername, startPosition, endPosition);
		} catch (Exception e) {
//			e.printStackTrace();
		}
	}

	private void fireBatchScriptEvent(String buffername,
			long startPosition, long endPosition) {
		
	}

	/***************************************************************
	 * I/O
	 ***************************************************************/
	public ISicsReplyData syncSend(String command) throws SicsBatchException {
		try {
			String val =  sicsProxy.syncRun(command);
			JSONObject json = new JSONObject();
			json.put(JSONTag.REPLY.getText(), val);
			return new SicsReplyData(json);
		} catch (Exception e) {
			handleException(e.getMessage());
		}
		throw new SicsBatchException("failed to send " + command);
	}
	
	public void asyncSend(String command, ISicsCallback callback) throws SicsBatchException {
		try {
			sicsProxy.asyncRun(command, callback);
		} catch (SicsException e) {
			throw new SicsBatchException("failed to send command", e);
		}
	}

	@Override
	public void resetBufferManagerStatus() {
		
		if (batchControl != null) {
			batchControl.resetStatus();
		}
	}

	@Override
	public BatchStatus getStatus() {
		if (batchControl != null) {
			return batchControl.getStatus();
		} else {
			return BatchStatus.DISCONNECTED;
		}
	}
	
	public static IBatchManager getBatchScriptManager(ISicsProxy sicsProxy) {
		if (batchManger == null) {
			batchManger = new BatchManager(sicsProxy);
		}
		return batchManger;
	}

	public void setMessageListener(SicsMessageAdapter messageListener) {
		this.messageListener = messageListener;
		sicsProxy.addMessageListener(messageListener);
	}
	

}
