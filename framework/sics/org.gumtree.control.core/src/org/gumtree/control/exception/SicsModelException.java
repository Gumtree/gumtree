package org.gumtree.control.exception;

public class SicsModelException extends SicsException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7491363043173719988L;

	public SicsModelException(String message) {
		super(message);
	}

	public SicsModelException(String message, Exception e) {
		super(message, e);
	}

}
