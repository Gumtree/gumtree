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

public class LoadAlgorithmFileFailedException extends Exception {

	/**
	 * Thrown by Algorithm_ when loading algorithm structure from an XML recipe file.
	 * Caught by Algorithm_ constructor when initialising
	 * and by AlgorithmManager_ when load an algorithm.
	 * @see au.gov.ansto.bragg.cicada.core.internal.Algorithm_#loadAlgorithm(java.io.File)  
	 */
	private static final long serialVersionUID = 2L;

	public LoadAlgorithmFileFailedException() {
		// TODO Auto-generated constructor stub
	}

	public LoadAlgorithmFileFailedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public LoadAlgorithmFileFailedException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public LoadAlgorithmFileFailedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
