package org.gumtree.gumnix.sics.io;

public class SicsExecutionException extends Exception {

	private static final long serialVersionUID = 8972265077946840132L;

	public SicsExecutionException(String message) {
		super(message);
	}
	
	public SicsExecutionException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
