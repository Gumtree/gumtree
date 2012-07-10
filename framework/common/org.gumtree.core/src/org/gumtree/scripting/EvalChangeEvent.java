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

import javax.script.ScriptContext;

public class EvalChangeEvent extends ScriptingChangeEvent {

	private static final long serialVersionUID = -8052992866824251300L;

	private String script;

	private ScriptContext context;

	public EvalChangeEvent(IObservableComponent component, String script,
			ScriptContext context) {
		super(component);
		this.script = script;
		this.context = context;
	}

	public String getScript() {
		return script;
	}

	public ScriptContext getContext() {
		return context;
	}

}
