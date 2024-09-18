package org.gumtree.control.exception;

public class SicsCommunicationException extends SicsException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4111466029985107447L;

	public SicsCommunicationException(String message) {
		super(message);
	}
	
	public SicsCommunicationException(String message, Exception cause) {
		super(message, cause);
	}
}
