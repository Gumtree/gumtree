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
package au.gov.ansto.bragg.datastructures.core.exception;

/**
 * @author nxi
 * Created on 12/03/2008
 */
public class StructureTypeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3305221757398741872L;

	/**
	 * 
	 */
	public StructureTypeException() {
	}

	/**
	 * @param message
	 */
	public StructureTypeException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public StructureTypeException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public StructureTypeException(String message, Throwable cause) {
		super(message, cause);
	}

}
