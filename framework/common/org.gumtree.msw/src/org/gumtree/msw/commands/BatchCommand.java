package org.gumtree.msw.commands;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Stack;

import org.gumtree.msw.ICommand;
import org.gumtree.msw.IModel;
import org.gumtree.msw.RefId;

public class BatchCommand extends Command {
	// fields
	private final Iterable<ICommand> commands;
	
	// construction
	public BatchCommand(RefId id, ICommand ... commands) {
		this(id, commands, false);
	}
	public BatchCommand(RefId id, ICommand[] commands, boolean isUndo) {
		super(id, isUndo);
			
		this.commands = Arrays.asList(commands);
	}

	// methods
	@Override
	public ICommand execute(IModel model) {
		Stack<ICommand> undoCommands = new Stack<>();

		for (ICommand command : commands) {
			ICommand undoCommand = command.execute(model);
			if (undoCommand != null)
				undoCommands.push(undoCommand);
			else {
				if (!undoCommands.contains(ICommand.IRREVERSIBLE))
					while (!undoCommands.empty())
						if (undoCommands.pop().execute(model) == null)
							break;
				
				return null;
			}
		}
		
		if (undoCommands.contains(ICommand.IRREVERSIBLE))
			return ICommand.IRREVERSIBLE;

		int i = 0;
		ICommand[] list = new ICommand[undoCommands.size()];
		while (!undoCommands.empty())
			list[i++] = undoCommands.pop();
		
		return new BatchCommand(getId(), list, !isUndo());
	}
	@Override
	public boolean equals(Object obj) {		
		if (obj instanceof BatchCommand) {
			BatchCommand other = (BatchCommand)obj;
			if (!super.equals(other))
				return false;

			Iterator<ICommand> itr1 = commands.iterator();
			Iterator<ICommand> itr2 = other.commands.iterator();
			
			while (itr1.hasNext()) {
				if (!itr2.hasNext() || !Objects.equals(
						itr1.next(),
						itr2.next()))
					return false;
			}
			if (itr2.hasNext())
				return false;
			
			return true;
		}
		return false;
	}
}
