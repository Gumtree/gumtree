package org.gumtree.gumnix.sics.batch.ui.buffer;

public class BatchBufferManagerStatusEvent extends BatchBufferManagerEvent {

	private BatchBufferManagerStatus status;
	private String message;
	
	public BatchBufferManagerStatusEvent(IBatchBufferManager manager,
			BatchBufferManagerStatus status) {
		super(manager);
		this.status = status;
	}

	public BatchBufferManagerStatus getStatus() {
		return status;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
}
