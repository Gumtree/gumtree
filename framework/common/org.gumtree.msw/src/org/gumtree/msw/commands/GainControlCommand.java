package org.gumtree.msw.commands;

import org.gumtree.msw.ICommand;
import org.gumtree.msw.IModel;
import org.gumtree.msw.RefId;

public class GainControlCommand extends Command {
	// construction
	public GainControlCommand(RefId id) {
		this(id, false);
	}
	public GainControlCommand(RefId id, boolean isUndo) {
		super(id, isUndo);
	}

	// methods
	@Override
	public ICommand execute(IModel model) {
		if (!model.gainControl(getId().getSourceId()))
			return null;
		
		return new ReleaseControlCommand(getId(), !isUndo());
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GainControlCommand)
			return super.equals(obj);
		
		return false;
	}
}
