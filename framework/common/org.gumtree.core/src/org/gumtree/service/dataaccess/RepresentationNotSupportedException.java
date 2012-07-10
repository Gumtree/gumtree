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

package org.gumtree.service.dataaccess;

public class RepresentationNotSupportedException extends DataAccessException {

	private static final long serialVersionUID = -3652212348299003847L;

	public RepresentationNotSupportedException() {
		super();
	}
	
	public RepresentationNotSupportedException(String message) {
		super(message);
	}
	
	public RepresentationNotSupportedException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public RepresentationNotSupportedException(Throwable cause) {
		super(cause);
	}
	
}
