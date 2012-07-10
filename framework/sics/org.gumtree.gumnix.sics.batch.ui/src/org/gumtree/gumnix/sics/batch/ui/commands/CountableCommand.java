package org.gumtree.gumnix.sics.batch.ui.commands;

import org.gumtree.gumnix.sics.batch.ui.definition.ISicsCommand;
import org.gumtree.gumnix.sics.batch.ui.util.SicsCommandFactory;

public class CountableCommand extends DynamicCommand {
	
	@Override
	public ISicsCommand[] generateAvailableCommands() {
		return SicsCommandFactory.createCountableCommands();
	}

}
