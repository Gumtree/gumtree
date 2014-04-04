package org.gumtree.sics.batch;

import org.gumtree.service.eventbus.Event;

public class BatchBufferManagerEvent extends Event {

	public BatchBufferManagerEvent(IBatchBufferManager manager) {
		super(manager);
	}

	public IBatchBufferManager getManager() {
		return (IBatchBufferManager) getPublisher();
	}
	
}
