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

/**
 * @author nxi
 * Created on 08/04/2009
 */
public class SinkSignalException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2095879579437575644L;

	/**
	 * 
	 */
	public SinkSignalException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public SinkSignalException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public SinkSignalException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SinkSignalException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
