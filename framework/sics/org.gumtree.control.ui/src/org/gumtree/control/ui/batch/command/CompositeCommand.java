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

package org.gumtree.control.ui.batch.command;

import java.util.Map;
import java.util.TreeMap;

import org.gumtree.control.batch.tasks.ICompositeCommand;
import org.gumtree.control.batch.tasks.IPlainCommand;

public class CompositeCommand extends AbstractSicsCommand implements ICompositeCommand {

	Map<String, IPlainCommand> subCommands;
	
	IPlainCommand selectedSubCommand;
	
	public CompositeCommand(String command) {
		super(command);
		subCommands = new TreeMap<String, IPlainCommand>();
	}

	public IPlainCommand getSubCommand(String name) {
		return subCommands.get(name);
	}

	public String[] getSubCommandNames() {
		return subCommands.keySet().toArray(new String[subCommands.size()]);
	}

	public IPlainCommand[] getSubCommands() {
		return subCommands.values().toArray(new IPlainCommand[subCommands.size()]);
	}

	public void addSubCommand(IPlainCommand subCommand) {
		subCommands.put(subCommand.getName(), subCommand);
	}

	public int getSubCommandSize() {
		return subCommands.size();
	}
	
	public IPlainCommand getSelectedSubCommand() {
		return selectedSubCommand;
	}
	
	public void setSelectedSubCommand(IPlainCommand subCommand) {
		if (!subCommands.containsValue(subCommand)) {
			throw new IllegalArgumentException("Sub command " + subCommand.getName() + " is not available from this command.");
		}
		IPlainCommand oldValue = selectedSubCommand;
		selectedSubCommand = subCommand;
		firePropertyChange("selectedSubCommand", oldValue, subCommand);
	}
	
	public String toScript() {
		StringBuilder builder = new StringBuilder();
		// Append command
		builder.append(getName());
		if (selectedSubCommand != null) {
			builder.append(" ");
			builder.append(selectedSubCommand.toScript());
		}
		return builder.toString();
	}
	
}
