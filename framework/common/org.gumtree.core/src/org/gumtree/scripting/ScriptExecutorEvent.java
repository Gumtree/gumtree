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

import org.gumtree.service.eventbus.Event;

public class ScriptExecutorEvent extends Event {
	
	public ScriptExecutorEvent(ScriptExecutor executor) {
		super(executor);
	}

	public ScriptExecutor getPublisher() {
		return (ScriptExecutor) super.getPublisher();
	}
	
}
