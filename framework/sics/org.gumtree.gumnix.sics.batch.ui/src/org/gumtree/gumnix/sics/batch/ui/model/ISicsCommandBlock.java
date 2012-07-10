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

package org.gumtree.gumnix.sics.batch.ui.model;


public interface ISicsCommandBlock {

	public void addCommand(ISicsCommandElement command);
	
	public void removeCommand(ISicsCommandElement command);
	
	public void insertCommand(int index, ISicsCommandElement command);
	
	public int indexOf(ISicsCommandElement command);
	
	public int size();
	
	public ISicsCommandElement[] getCommands();
	
	public String getName();
	
	public void setName(String name);
	
	public String toScript();
	
}