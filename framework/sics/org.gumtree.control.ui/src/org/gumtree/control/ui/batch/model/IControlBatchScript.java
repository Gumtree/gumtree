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

package org.gumtree.control.ui.batch.model;

import org.gumtree.control.batch.tasks.ISicsCommand;

public interface IControlBatchScript {

	public void addCommandBlock(ISicsCommand block);
	
	public void removeCommandBlock(ISicsCommand block);
	
	public void insertCommandBlock(int index, ISicsCommand block);
	
	public String toScript();
	
}
