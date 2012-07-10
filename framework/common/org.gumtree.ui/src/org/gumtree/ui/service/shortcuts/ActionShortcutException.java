/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.ui.service.shortcuts;

public class ActionShortcutException extends Exception {

	private static final long serialVersionUID = -1033212294011516542L;

	public ActionShortcutException(String message) {
		super(message);
	}

	public ActionShortcutException(String message, Throwable cause) {
		super(message, cause);
	}

}
