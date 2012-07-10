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

package org.gumtree.core.service;

public class ServiceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -1946373056882041973L;

	public ServiceNotFoundException() {
		super();
	}
	
	public ServiceNotFoundException(String message) {
		super(message);
	}

	public ServiceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ServiceNotFoundException(Throwable cause) {
		super(cause);
	}
	
}
