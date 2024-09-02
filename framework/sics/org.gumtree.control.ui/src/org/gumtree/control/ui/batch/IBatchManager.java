package org.gumtree.control.ui.batch;

import java.util.List;

import org.gumtree.control.batch.BatchStatus;
import org.gumtree.control.batch.IBatchListener;
import org.gumtree.control.batch.IBatchScript;
import org.gumtree.control.batch.SicsMessageAdapter;
import org.gumtree.control.core.ISicsReplyData;
import org.gumtree.control.events.ISicsCallback;
import org.gumtree.control.exception.SicsBatchException;
import org.gumtree.control.exception.SicsException;
import org.gumtree.core.service.IService;

public interface IBatchManager extends IService {
	
	/***************************************************************
	 * Getters and setters
	 ***************************************************************/

	public boolean isAutoRun();
	
	public void setAutoRun(boolean autoRun);
	
	public List<IBatchScript> getBatchBufferQueue();
		
	/***************************************************************
	 * Status
	 ***************************************************************/
	public BatchStatus getStatus();

	/**
	 * Queries the currently executing buffer name.
	 * 
	 * @return currently running buffer name, null if no running buffer is found
	 */
	public void getRunningBuffername(ISicsCallback callback) throws SicsException;
	
	/**
	 * Queries the buffer content based on the provided buffer name.
	 * 
	 * @param buffername
	 * @return
	 * @throws BatchBufferManagerException when buffer not found
	 */
	public void getBuffer(String buffername, ISicsCallback callback);
	public void getRunningBufferContent(ISicsCallback callback);
	public void getRunningBufferRangeString(ISicsCallback callback);
	
	/***************************************************************
	 * Event
	 ***************************************************************/
//	public void addEventHandler(IEventHandler<BatchBufferManagerEvent> eventHandler);
	
//	public void removeEventHandler(IEventHandler<BatchBufferManagerEvent> eventHandler);
	
	public void addBatchManagerListener(IBatchManagerListener listener);
	
	public void removeBatchManagerListener(IBatchManagerListener listener);
	
	public void resetBufferManagerStatus();

	public ISicsReplyData syncSend(String command) throws SicsBatchException;
	
//	public ISicsReplyData syncSend(String command, ISicsCallback callback) throws SicsBatchException;
	
	public void asyncSend(String command, ISicsCallback callback) throws SicsBatchException;
	
	public void setMessageListener(SicsMessageAdapter messageListener);
}
