package org.gumtree.msw.commands;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.msw.ICommand;
import org.gumtree.msw.IModel;
import org.gumtree.msw.RefId;

public class ClearElementListCommand extends TargetCommand {
	// construction
	public ClearElementListCommand(RefId id, Iterable<String> listPath) {
		super(id, listPath, false);
	}

	// methods
	@Override
	public ICommand execute(IModel model) {
		List<DeleteListElementCommand> deleteCommands = new ArrayList<>();
		for (String elementName : model.getListElements(getTargetPath()))
			deleteCommands.add(new DeleteListElementCommand(
					getId(),
					getTargetPath(),
					elementName));
		
		Command batchCommand = new BatchCommand(
				getId(),
				deleteCommands.toArray(new ICommand[deleteCommands.size()]));
		
		return batchCommand.execute(model);
	}
	@Override
	public boolean equals(Object obj) {		
		if (obj instanceof ClearElementListCommand)
			return super.equals(obj);
		
		return false;
	}
}
