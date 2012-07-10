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
package au.gov.ansto.bragg.process.common.exception;

public class IllegalNameSetException extends Exception {

		private static final long serialVersionUID = 10L;
		
		public IllegalNameSetException(){
			super();
		}
		
		public IllegalNameSetException(String arg0){
			super(arg0);
		}
		
		public IllegalNameSetException(Throwable arg0){
			super(arg0);
		}
		
		public IllegalNameSetException(String arg0, Throwable arg1){
			super(arg0, arg1);
		}
		

}
