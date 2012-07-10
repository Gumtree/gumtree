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

import java.util.EventObject;

public class ScriptingChangeEvent extends EventObject {

	private static final long serialVersionUID = -1086235925559277052L;

	public ScriptingChangeEvent(IObservableComponent component) {
		super(component);
	}

	public IObservableComponent getObservableComponent() {
		return (IObservableComponent) getSource();
	}

}
