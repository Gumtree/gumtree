package org.gumtree.gumnix.sics.io;

public class SicsIOException extends Exception {

	private static final long serialVersionUID = -3243971387265005521L;

	public SicsIOException(String message) {
		super(message);
	}

	public SicsIOException(String message, Throwable cause) {
        super(message, cause);
	}

}
