/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package org.gumtree.gumnix.sics.batch.ui.views;

import org.gumtree.gumnix.sics.batch.ui.model.ISicsCommandElement;
import org.gumtree.ui.util.workbench.IPartControlProvider;
import org.gumtree.workflow.ui.ITaskView;

public interface ISicsCommandView<T extends ISicsCommandElement> extends IPartControlProvider {

	public T getCommand();
	
	public void setCommand(T command);
	
	public void setTaskView(ITaskView taskView);
	
}
