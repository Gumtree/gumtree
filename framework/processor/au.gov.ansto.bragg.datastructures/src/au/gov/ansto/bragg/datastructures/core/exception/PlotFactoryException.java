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
public class PlotFactoryException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4541513382350212463L;

	/**
	 * 
	 */
	public PlotFactoryException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public PlotFactoryException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public PlotFactoryException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public PlotFactoryException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
