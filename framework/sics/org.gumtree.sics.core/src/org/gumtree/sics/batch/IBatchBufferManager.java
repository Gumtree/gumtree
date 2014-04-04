package org.gumtree.sics.batch;

import java.util.List;

import org.gumtree.core.service.IService;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.sics.core.ISicsManager;
import org.gumtree.sics.io.ISicsProxy;

public interface IBatchBufferManager extends IService {
	
	/***************************************************************
	 * Getters and setters
	 ***************************************************************/
	public ISicsManager getSicsManager();
	
	public void setSicsManager(ISicsManager sicsManager);

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
	
	public void setProxy(ISicsProxy proxy);
	
	public String getRunningText();
	
}
