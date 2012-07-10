/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bragg Institute - initial API and implementation
 ******************************************************************************/

package org.gumtree.scripting;

import java.io.Reader;
import java.io.StringReader;

public class ScriptBlock implements IScriptBlock {

	private boolean skip;
	
	private StringBuilder builder;
	
	public ScriptBlock() {
		super();
	}
	
	public void append(String script) {
		if (builder == null) {
			builder = new StringBuilder();
		}
		builder.append(script);
		builder.append("\n");
	}

	public String getScript() {
		return builder != null ? builder.toString() : "";
	}
	
	public Reader getReader() {
		String script = getScript();
		StringReader reader = new StringReader(script);
		return reader;
	}
	
	public boolean isSkip() {
		return skip;
	}

	public void setSkip(boolean skip) {
		this.skip = skip;
	}

	// Subclass may reimplement this
	public void postProcess() {
		// Do nothing
	}
	
}
