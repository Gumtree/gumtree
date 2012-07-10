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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;

public interface IActionShortcutDescriptor {

	public String getId();

	public String getLabel();

	public String getCategory();

	public ImageDescriptor getIcon16();

	public ImageDescriptor getIcon32();

	public IAction getAction() throws ActionShortcutException;

}
