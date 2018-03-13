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

public class ScriptCommand extends AbstractSicsCommand {

	private String text = "";
	
	public ScriptCommand() {
		super();
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		String oldValue = this.text;
		this.text = text;
		firePropertyChange("text", oldValue, text);
	}

	public String toScript() {
		return getText();
	}
	
}
