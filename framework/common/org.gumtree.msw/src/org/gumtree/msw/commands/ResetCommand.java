package org.gumtree.msw.commands;

import org.gumtree.msw.ICommand;
import org.gumtree.msw.IModel;
import org.gumtree.msw.RefId;

public class ResetCommand extends Command {
	// construction
	public ResetCommand(RefId id) {
		super(id, false);
	}
	
	// methods
	@Override
	public ICommand execute(IModel model) {
		if (!model.reset())
			return null;
		
		return ICommand.IRREVERSIBLE;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ResetCommand)
			return super.equals(obj);
		
		return false;
	}
}
