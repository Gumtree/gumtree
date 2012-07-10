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
 * Runtime exception to present error from object conversion.
 * 
 * @author Tony Lam
 * @since 1.4
 *
 */
public class ObjectConvertException extends RuntimeException {

	private static final long serialVersionUID = -186765912630183144L;

	public ObjectConvertException() {
		super();
	}
	
	public ObjectConvertException(String message) {
		super(message);
	}

	public ObjectConvertException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ObjectConvertException(Throwable cause) {
		super(cause);
	}
	
}
