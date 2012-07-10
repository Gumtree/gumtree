package org.gumtree.gumnix.sics.control.controllers;

import org.gumtree.gumnix.sics.core.SicsCoreException;

public class ComponentDataFormatException extends SicsCoreException {

	private static final long serialVersionUID = 852754618148493386L;

	public ComponentDataFormatException(String message) {
		super(message);
	}

	public ComponentDataFormatException(String message, Throwable cause) {
		super(message, cause);
	}

}
