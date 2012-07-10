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

import org.gumtree.workflow.ui.internal.Activator;

public final class TaskRegistryConstants {

	public static String EXTENSION_TASKS = "tasks";
	
	public static String ELEMENT_TASK = "task";
	
	public static String ATTRIBUTE_ICON_32 = "icon32";
	
	public static String ATTRIBUTE_PROVIDER = "provider";
	
	public static String EXTENTION_POINT_TASKS = Activator.PLUGIN_ID + "." + EXTENSION_TASKS;
	
	private TaskRegistryConstants() {
		super();
	}
	
}
