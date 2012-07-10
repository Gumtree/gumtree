package org.gumtree.gumnix.sics.batch.ui.buffer;

public class BatchBufferManagerException extends RuntimeException {

	private static final long serialVersionUID = 4608039435427477058L;

	public BatchBufferManagerException(String message) {
		super(message);
	}
	
	public BatchBufferManagerException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public BatchBufferManagerException(Throwable cause) {
		super(cause);
	}
	
}
