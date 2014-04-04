package org.gumtree.sics.batch;

public class BatchBufferManagerStatusEvent extends BatchBufferManagerEvent {

	private BatchBufferManagerStatus status;
	
	public BatchBufferManagerStatusEvent(IBatchBufferManager manager,
			BatchBufferManagerStatus status) {
		super(manager);
		this.status = status;
	}

	public BatchBufferManagerStatus getStatus() {
		return status;
	}
	
}
