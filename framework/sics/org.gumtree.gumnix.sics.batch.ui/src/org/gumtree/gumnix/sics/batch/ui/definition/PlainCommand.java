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

package org.gumtree.gumnix.sics.batch.ui.definition;

import java.util.ArrayList;
import java.util.List;

public class PlainCommand extends AbstractSicsCommand implements IPlainCommand {
	
	private List<ICommandArg> args;
	
	public PlainCommand(String name) {
		super(name);
		args = new ArrayList<ICommandArg>(2);
	}
	
	public void addArgumentDefinition(String name, ArgType type) {
		args.add(new CommandArg(name, type));
	}
	
	public ICommandArg[] getArguments() {
		return args.toArray(new ICommandArg[args.size()]);
	}
	
	public int getArgumentSize() {
		return args.size();
	}
	
	public String toScript() {
		StringBuilder builder = new StringBuilder();
		// Append command
		builder.append(getName());
		builder.append(" ");
		// Append argument
		for (ICommandArg arg : args) {
			if (arg.getValue() != null) {
				builder.append(arg.getValue());
			} else {
				// Oops...can't go any further if arg value is missing
				break;
			}
			if (args.indexOf(arg) != args.size() - 1) {
				// Append space except for the last one
				builder.append(" ");
			}
		}
		return builder.toString();
	}
	
}
