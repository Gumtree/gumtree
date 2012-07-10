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

public class InvalidResourceException extends DataAccessException {

	private static final long serialVersionUID = 7675875878560411468L;

	public InvalidResourceException() {
		super();
	}
	
	public InvalidResourceException(String message) {
		super(message);
	}
	
	public InvalidResourceException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public InvalidResourceException(Throwable cause) {
		super(cause);
	}
	
}
