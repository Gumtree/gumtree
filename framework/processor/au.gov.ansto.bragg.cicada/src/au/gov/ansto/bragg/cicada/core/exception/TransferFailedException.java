/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package au.gov.ansto.bragg.cicada.core.exception;

public class TransferFailedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TransferFailedException() {
		// TODO Auto-generated constructor stub
	}

	public TransferFailedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public TransferFailedException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public TransferFailedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
