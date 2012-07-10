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
 * Runtime exception to represent error from object configuration.
 * 
 * This is an unchecked exception because configuration error is
 * usually no need to be recovered.
 * 
 * @since 1.2
 */
public class ObjectConfigException extends RuntimeException {

	private static final long serialVersionUID = -106466163844429064L;

	public ObjectConfigException() {
		super();
	}
	
	public ObjectConfigException(String message) {
		super(message);
	}

	public ObjectConfigException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ObjectConfigException(Throwable cause) {
		super(cause);
	}

}
