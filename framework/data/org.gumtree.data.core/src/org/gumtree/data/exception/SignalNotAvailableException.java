/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation,
 * Synchrotron SOLEIL and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     Norman XIONG (Bragg Institute) - initial API and implementation
 *     Clément RODRIGUEZ (SOLEIL) - initial API and implementation
 *     Tony LAM (Bragg Institute) - implementation
 ******************************************************************************/

package org.gumtree.data.exception;

/**
 * This exception is thrown when the IArray storage is not reachable in a
 * IDataItem instance.
 * 
 * @author nxi
 * 
 */
public class SignalNotAvailableException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4635308951458654040L;

	/**
	 * 
	 */
	public SignalNotAvailableException() {
	}

	/**
     * @param message String value
	 */
	public SignalNotAvailableException(final String message) {
		super(message);
	}

	/**
     * @param cause Throwable object
	 */
	public SignalNotAvailableException(final Throwable cause) {
		super(cause);
	}

	/**
     * @param message String value
     * @param cause Throwable object
	 */
	public SignalNotAvailableException(final String message,
			final Throwable cause) {
		super(message, cause);
	}

}
