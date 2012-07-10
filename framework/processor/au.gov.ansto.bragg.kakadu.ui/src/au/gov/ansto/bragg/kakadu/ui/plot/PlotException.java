/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.ui.plot;

/**
 * The exception used to throw internal errors or highlight restrictions and boundaries. 
 * @author Danil Klimontov (dak)
 */
public class PlotException extends Exception {

	/**
	 * @param message
	 */
	public PlotException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public PlotException(String message, Throwable cause) {
		super(message, cause);
	}

}
