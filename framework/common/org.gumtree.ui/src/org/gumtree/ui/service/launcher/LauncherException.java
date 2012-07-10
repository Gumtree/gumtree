package org.gumtree.ui.service.launcher;

public class LauncherException extends Exception {

	private static final long serialVersionUID = 6570693205984599962L;

	public LauncherException(String message) {
		super(message);
	}
	
	public LauncherException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
