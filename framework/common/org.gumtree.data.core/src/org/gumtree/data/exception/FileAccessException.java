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
public class FileAccessException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5360865109579178570L;

	/**
	 * 
	 */
	public FileAccessException() {
	}

	/**
     * @param arg0 String value
	 */
	public FileAccessException(final String arg0) {
		super(arg0);
	}

	/**
     * @param arg0 Throwable object
	 */
	public FileAccessException(final Throwable arg0) {
		super(arg0);
	}

	/**
     * @param arg0 String value
     * @param arg1 Throwable object
	 */
	public FileAccessException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

}
