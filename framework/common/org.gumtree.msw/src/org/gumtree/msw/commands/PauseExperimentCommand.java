package org.gumtree.msw.commands;

import org.gumtree.msw.ICommand;
import org.gumtree.msw.IModel;
import org.gumtree.msw.RefId;

public class PauseExperimentCommand extends Command {
	// construction
	public PauseExperimentCommand(RefId id) {
		this(id, false);
	}
	public PauseExperimentCommand(RefId id, boolean isUndo) {
		super(id, isUndo);
	}

	// methods
	@Override
	public ICommand execute(IModel model) {
		if (!model.pauseExperiment())
			return null;
		
		return new StartExperimentCommand(getId(), !isUndo());
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PauseExperimentCommand)
			return super.equals(obj);
		
		return false;
	}
}
