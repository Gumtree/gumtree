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

package org.gumtree.workflow.ui.util;

import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.gumtree.core.object.ObjectCreateException;
import org.gumtree.workflow.ui.ITask;

public interface ITaskDescriptor {

	public String getLabel();
	
	public String getClassname();
	
	public Image getIcon();
	
	public ITask createNewTask() throws ObjectCreateException;
	
	public List<String> getTags();
	
	public Image getLargeIcon();
	
	public String getProvider();
	
	public String getDescription();
	
	public String getColorString();
}
