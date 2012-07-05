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
 * @author nxi
 * 
 */
public class InvalidArrayTypeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1484341159297838875L;

	/**
	 * 
	 */
	public InvalidArrayTypeException() {
	}

	/**
     * @param arg0 String value
	 */
	public InvalidArrayTypeException(final String arg0) {
		super(arg0);
	}

	/**
     * @param arg0 Throwable object
	 */
	public InvalidArrayTypeException(final Throwable arg0) {
		super(arg0);
	}

	/**
     * @param arg0 String value
     * @param arg1 Throwable object
	 */
	public InvalidArrayTypeException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

}
