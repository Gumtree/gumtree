package org.gumtree.msw.commands;

import org.gumtree.msw.ICommand;
import org.gumtree.msw.IModel;
import org.gumtree.msw.RefId;

public class ReleaseControlCommand extends Command {
	// construction
	public ReleaseControlCommand(RefId id) {
		this(id, false);
	}
	public ReleaseControlCommand(RefId id, boolean isUndo) {
		super(id, isUndo);
	}

	// methods
	@Override
	public ICommand execute(IModel model) {
		if (!model.releaseControl(getId().getSourceId()))
			return null;

		return new GainControlCommand(getId(), !isUndo());
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ReleaseControlCommand)
			return super.equals(obj);
		
		return false;
	}
}
