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

package org.gumtree.workflow.ui.internal.util;

import static org.gumtree.workflow.ui.internal.util.TaskRegistryConstants.ELEMENT_TASK;
import static org.gumtree.workflow.ui.internal.util.TaskRegistryConstants.EXTENSION_TASKS;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.gumtree.util.eclipse.ExtensionRegistryReader;
import org.gumtree.workflow.ui.internal.Activator;

public class TaskRegistryReader extends ExtensionRegistryReader {

	private TaskRegistry registry;

	protected TaskRegistryReader(TaskRegistry registry) {
		super(Activator.getDefault());
		this.registry = registry;
	}
	
	protected boolean readElement(IConfigurationElement element) {
		if (element.getName().equals(ELEMENT_TASK)) {
			registry.addTaskDescriptor(new TaskDescriptor(element));
			return true;
		}
		return false;
	}
	
	protected void readTasks() {
		IExtensionRegistry in = Platform.getExtensionRegistry();
		readRegistry(in, getPlugin().getBundle().getSymbolicName(),
				EXTENSION_TASKS);
	}

}
