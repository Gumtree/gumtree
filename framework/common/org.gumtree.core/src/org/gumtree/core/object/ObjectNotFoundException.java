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
 * ObjectNotFoundException represent an error which an entry is
 * not found when a search method is called.
 * 
 * @since 1.2
 * 
 */
public class ObjectNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -9205904208404173393L;

	public ObjectNotFoundException() {
		super();
	}

	public ObjectNotFoundException(String message) {
		super(message);
	}

	public ObjectNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ObjectNotFoundException(Throwable cause) {
		super(cause);
	}
	
}
