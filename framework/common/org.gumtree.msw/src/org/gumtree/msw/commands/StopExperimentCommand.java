package org.gumtree.msw.commands;

import org.gumtree.msw.ICommand;
import org.gumtree.msw.IModel;
import org.gumtree.msw.RefId;

public class StopExperimentCommand extends Command {
	// construction
	public StopExperimentCommand(RefId id) {
		super(id, false);
	}
	
	// methods
	@Override
	public ICommand execute(IModel model) {
		if (!model.stopExperiment())
			return null;
		
		return ICommand.IRREVERSIBLE;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StopExperimentCommand)
			return super.equals(obj);
		
		return false;
	}
}
