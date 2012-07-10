package org.gumtree.gumnix.sics.batch.ui.buffer;

public class BatchBufferManagerExecutionEvent extends BatchBufferManagerEvent {

	private String buffername;
	
	private long startPosition;
	
	private long endPosition;
	
	public BatchBufferManagerExecutionEvent(IBatchBufferManager manager,
			String buffername, long startPosition, long endPosition) {
		super(manager);
		this.buffername = buffername;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
	}

	public String getBuffername() {
		return buffername;
	}

	public long getStartPosition() {
		return startPosition;
	}

	public long getEndPosition() {
		return endPosition;
	}

}
