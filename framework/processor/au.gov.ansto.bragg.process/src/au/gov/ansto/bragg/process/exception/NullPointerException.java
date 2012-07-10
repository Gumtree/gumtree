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
package au.gov.ansto.bragg.process.exception;

public class NullPointerException extends Exception {

	public static final long serialVersionUID = 1L;
	
	protected static String message = " pointer is empty.\n";
	
	public NullPointerException() {
		// TODO Auto-generated constructor stub
	}

	public NullPointerException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public NullPointerException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public NullPointerException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}
	
	public String message(){
		return message;
	}

}
