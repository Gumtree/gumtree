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

package org.gumtree.control.batch.tasks;

public interface ISicsCommandBlock {

	public void addCommand(ISicsCommand command);
	
	public void removeCommand(ISicsCommand command);
	
	public void insertCommand(int index, ISicsCommand command);
	
	public int indexOf(ISicsCommand command);
	
	public int size();
	
	public ISicsCommand[] getCommands();
	
	public String getName();
	
	public void setName(String name);
	
	public String toScript();
	
}