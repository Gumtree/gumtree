/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package org.gumtree.control.ui.batch.command;

public class SicsVariableCommand extends AbstractSicsCommand {

	public SicsVariableCommand(String name) {
		super(name);
	}

	private String sicsVariable;
	
	private String value;
	private boolean isQuoted = false;
	
	public String getSicsVariable() {
		return sicsVariable;
	}

	public void setSicsVariable(String sicsVariable) {
		String oldValue = this.sicsVariable;
		this.sicsVariable = sicsVariable;
		firePropertyChange("sicsVariable", oldValue, sicsVariable);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		String oldValue = this.value;
		this.value = value;
		firePropertyChange("value", oldValue, value);
	}

	
	/**
	 * @return the isQuoted
	 */
	public boolean isQuoted() {
		return isQuoted;
	}

	/**
	 * @param isQuoted the isQuoted to set
	 */
	public void setQuoted(boolean isQuoted) {
		this.isQuoted = isQuoted;
	}

	public String toScript() {
		if (getSicsVariable() != null) {
			if(getValue() != null) {
				// Returns sics vairable + arguments
				if (isQuoted)
					return getSicsVariable() + " \"" + getValue() + "\"";
				else
					return getSicsVariable() + " " + getValue();
			} else {
				// Returns sics variable only
				return getSicsVariable();
			}
		}
		// Return empty line if variable is not properly set
		return "";
	}
	
}
