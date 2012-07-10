/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bragg Institute - initial API and implementation
 ******************************************************************************/

package org.gumtree.core.object;

/**
 * Exception to represent object creation error.
 * 
 * This is a runtime exception for the reason that the program
 * can recover itself from another object creation if this is failed.
 * 
 * @since 1.2
 *
 */
public class ObjectCreateException extends RuntimeException {

	private static final long serialVersionUID = -6542000952055865375L;

	public ObjectCreateException() {
		super();
	}

	public ObjectCreateException(String message) {
		super(message);
	}

	public ObjectCreateException(String message, Throwable cause) {
		super(message, cause);
	}

	public ObjectCreateException(Throwable cause) {
		super(cause);
	}
	
}
