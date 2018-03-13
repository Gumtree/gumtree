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

package org.gumtree.control.ui.batch;

import org.gumtree.core.service.ServiceUtils;
import org.gumtree.workflow.ui.util.ITaskDescriptor;
import org.gumtree.workflow.ui.util.ITaskRegistry;

public class CommandRegistry implements ICommandRegistry {

	private static final String TAG_TCL = "tcl";
	
	public ITaskDescriptor[] getCommandDescriptors() {
		ITaskRegistry taskRegistry = ServiceUtils.getService(ITaskRegistry.class);
		return taskRegistry.getTaskDescriptorsByTag(TAG_TCL);
	}
	
	
}
