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

package org.gumtree.gumnix.sics.batch.ui.commands;

import org.gumtree.gumnix.sics.batch.ui.definition.ISicsCommand;

public abstract class DynamicCommand extends AbstractSicsCommand {

	private ISicsCommand selectedCommand;
	
	private ISicsCommand[] commands;
	
	public ISicsCommand getSelectedCommand() {
		return selectedCommand;
	}

	public void setSelectedCommand(ISicsCommand selectedCommand) {
		this.selectedCommand = selectedCommand;
	}

	public ISicsCommand[] getAvailableCommands() {
		if (commands == null) {
			commands = generateAvailableCommands();
		}
		return commands;
	}
	
	public abstract ISicsCommand[] generateAvailableCommands();
	
	public String toScript() {
		if (getSelectedCommand() != null) {
			return getSelectedCommand().toScript();
		}
		return "";
	}

}
