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

import java.util.ArrayList;
import java.util.List;


public class SicsCommandBlock implements ISicsCommandBlock {

	private List<ISicsCommand> commands;
	
	private String name;
	
	public SicsCommandBlock() {
		commands = new ArrayList<ISicsCommand>(2);
	}
	
	public void addCommand(ISicsCommand command) {
		commands.add(command);
	}

	public void removeCommand(ISicsCommand command) {
		commands.remove(command);
	}
	
	public void insertCommand(int index, ISicsCommand command) {
		commands.add(index, command);
	}
	
	public int indexOf(ISicsCommand command) {
		return commands.indexOf(command);
	}
	
	public int size() {
		return commands.size();
	}
	
	public ISicsCommand[] getCommands() {
		return commands.toArray(new ISicsCommand[commands.size()]);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String toScript() {
		StringBuilder builder = new StringBuilder();
		// Add block header as comment if command block name is available
		if (getName() != null && getName().length() != 0) {
			builder.append("# ");
			builder.append(getName());
			builder.append("\n");
		}
		for (ISicsCommand command : commands) {
			builder.append(command.toScript());
			builder.append("\n");
		}
		return builder.toString();
	}

}
