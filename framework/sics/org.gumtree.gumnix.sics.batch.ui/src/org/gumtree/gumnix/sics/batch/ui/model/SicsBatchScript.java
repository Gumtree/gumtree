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

import java.util.ArrayList;
import java.util.List;

public class SicsBatchScript implements ISicsBatchScript {

	private List<ISicsCommandBlock> blocks;
	
	public SicsBatchScript() {
		blocks = new ArrayList<ISicsCommandBlock>(2);
	}
	
	public void addCommandBlock(ISicsCommandBlock block) {
		// Do not allow duplication
		if (blocks.contains(block)) {
			return;
		}
		blocks.add(block);
	}
	
	public void removeCommandBlock(ISicsCommandBlock block) {
		blocks.remove(block);
	}
	
	public void insertCommandBlock(int index, ISicsCommandBlock block) {
		blocks.add(index, block);
	}
	
	public String toScript() {
		StringBuilder builder = new StringBuilder();
		for (ISicsCommandBlock block : blocks) {
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
