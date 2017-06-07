package org.gumtree.gumnix.sics.batch.ui.buffer;

import java.util.List;

import org.gumtree.core.service.IService;
import org.gumtree.gumnix.sics.core.ISicsManager;
import org.gumtree.service.eventbus.IEventHandler;

public interface IBatchBufferManager extends IService {
	
	/***************************************************************
	 * Getters and setters
	 ***************************************************************/
	public ISicsManager getSicsManager();
	
	public void setSicsManager(ISicsManager sicsManager);

	public boolean isAutoRun();
	
	public void setAutoRun(boolean autoRun);
	
	public List<IBatchBuffer> getBatchBufferQueue();
		
	/***************************************************************
	 * Status
	 ***************************************************************/
	public BatchBufferManagerStatus getStatus();

	/**
	 * Queries the currently executing buffer name.
	 * 
	 * @return currently running buffer name, null if no running buffer is found
	 */
	public String getRunningBuffername();
	
	public String getRunningBufferContent();
	
	public String getRunningBufferRangeString();
	
	/**
	 * Queries the buffer content based on the provided buffer name.
	 * 
	 * @param buffername
	 * @return
	 * @throws BatchBufferManagerException when buffer not found
	 */
	public String getBuffer(String buffername) throws BatchBufferManagerException;

	/***************************************************************
	 * Event
	 ***************************************************************/
	public void addEventHandler(IEventHandler<BatchBufferManagerEvent> eventHandler);
	
	public void removeEventHandler(IEventHandler<BatchBufferManagerEvent> eventHandler);
	
	public void resetBufferManagerStatus();
	
}
