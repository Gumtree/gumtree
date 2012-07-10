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

package org.gumtree.ui.internal.cli.doms;

import org.gumtree.core.service.ServiceUtils;
import org.gumtree.ui.service.shortcuts.IActionShortcutRegistry;

// Should be replaced by Groovy Monkey
@Deprecated
public class ActionShortcutRegistryDOMFactory {

	public ActionShortcutRegistryDOMFactory() {
		super();
	}

	public Object getDOMroot() {
		// Returns the workbench service registry as the DOM object
		return ServiceUtils.getService(IActionShortcutRegistry.class);
	}

}
