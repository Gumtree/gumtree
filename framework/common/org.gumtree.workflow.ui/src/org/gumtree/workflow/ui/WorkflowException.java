package org.gumtree.workflow.ui;

public class WorkflowException extends RuntimeException {

	private static final long serialVersionUID = 5212495279220067586L;

	public WorkflowException(String message) {
		super(message);
	}
	
	public WorkflowException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
