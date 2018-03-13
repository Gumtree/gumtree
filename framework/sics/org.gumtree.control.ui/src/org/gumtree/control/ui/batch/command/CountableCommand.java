package org.gumtree.control.ui.batch.command;

import org.gumtree.control.batch.tasks.ISicsCommand;

public class CountableCommand extends DynamicCommand {
	
	@Override
	public ISicsCommand[] generateAvailableCommands() {
		return SicsCommandFactory.createCountableCommands();
	}

}
