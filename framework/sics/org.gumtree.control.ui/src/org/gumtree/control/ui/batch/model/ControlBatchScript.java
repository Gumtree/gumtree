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

import java.util.ArrayList;
import java.util.List;

import org.gumtree.control.batch.tasks.ISicsCommand;

public class ControlBatchScript implements IControlBatchScript {

	private List<ISicsCommand> blocks;
	
	public ControlBatchScript() {
		blocks = new ArrayList<ISicsCommand>(2);
	}
	
	public void addCommandBlock(ISicsCommand block) {
		// Do not allow duplication
		if (blocks.contains(block)) {
			return;
		}
		blocks.add(block);
	}
	
	public void removeCommandBlock(ISicsCommand block) {
		blocks.remove(block);
	}
	
	public void insertCommandBlock(int index, ISicsCommand block) {
		blocks.add(index, block);
	}
	
	public String toScript() {
		StringBuilder builder = new StringBuilder();
		for (ISicsCommand block : blocks) {
			builder.append(block.toScript());
			if (blocks.indexOf(block) != blocks.size() - 1) {
				// Append new line between blocks
				builder.append("\n");
			}
		}
		return builder.toString();
	}
	
	@Override
	public String toString() {
		return toScript();
	}
	
}
