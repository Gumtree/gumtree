package org.gumtree.control.ui.batch.command;

import org.gumtree.control.batch.tasks.ISicsCommand;

public class CountableCommand extends DynamicCommand {
	
	public CountableCommand() {
		super("countable");
	}
	
	@Override
	public ISicsCommand[] generateAvailableCommands() {
		return SicsCommandFactory.createCountableCommands();
	}

}
