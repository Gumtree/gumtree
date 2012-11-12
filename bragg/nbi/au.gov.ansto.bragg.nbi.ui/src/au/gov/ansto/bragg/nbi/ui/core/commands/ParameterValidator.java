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
package au.gov.ansto.bragg.nbi.ui.core.commands;



/**
 * @author nxi
 * Created on 05/08/2009
 */
public abstract class ParameterValidator {

	public final static ParameterValidator floatValidator = new ParameterValidator() {

		public boolean isValid(String text) {
			if (text == null || text.trim().length() == 0)
				return false;
			if (text.trim().equals("*")) {
				return true;
			}
			try{
				text = text.replaceAll(",", "");
				Double.parseDouble(text);
				return true;
			}catch (Exception e) {
				return false;
			}
		}

		public String getErrorMessage() {
			return "please input a number";
		}
	};

	protected final static ParameterValidator notEmptyValidator = new ParameterValidator() {
		
		public boolean isValid(String text) {
			if (text == null || text.trim().length() == 0)
				return false;
//			if (text.trim().contains(" "))
//				return false;
			return true;
		}

		public String getErrorMessage() {
			return "please input text with no spacing";
		}
	};

	protected final static ParameterValidator integerValidator = new ParameterValidator() {
		
		public boolean isValid(String text) {
			if (text == null || text.trim().length() == 0)
				return false;
			try{
				Integer.parseInt(text);
				return true;
			}catch (Exception e) {
				return false;
			}
		}

		public String getErrorMessage() {
			return "please input a number";
		}
	};

	public abstract boolean isValid(String text);

	public abstract String getErrorMessage();
}
