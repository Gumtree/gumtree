package org.gumtree.msw.commands;

import org.gumtree.msw.ICommand;
import org.gumtree.msw.IModel;
import org.gumtree.msw.RefId;

public class StartExperimentCommand extends Command {
	// construction
	public StartExperimentCommand(RefId id) {
		this(id, false);
	}
	public StartExperimentCommand(RefId id, boolean isUndo) {
		super(id, isUndo);
	}
	
	// methods
	@Override
	public ICommand execute(IModel model) {
		if (!model.startExperiment())
			return null;
		
		return new PauseExperimentCommand(getId(), !isUndo());
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StartExperimentCommand)
			return super.equals(obj);
		
		return false;
	}
}
