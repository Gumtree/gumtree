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

public class ScriptExecutorStateEvent extends ScriptExecutorEvent {

	private boolean isBusy;

	public ScriptExecutorStateEvent(ScriptExecutor executor, boolean isBusy) {
		super(executor);
		this.isBusy = isBusy;
	}

	public boolean isBusy() {
		return isBusy;
	}

}
