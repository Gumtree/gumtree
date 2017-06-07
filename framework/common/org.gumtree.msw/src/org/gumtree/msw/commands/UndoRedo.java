package org.gumtree.msw.commands;

import java.util.ArrayDeque;
import java.util.Deque;

import org.gumtree.msw.ICommand;
import org.gumtree.msw.IModelProxy;
import org.gumtree.msw.util.ModelListenerAdapter;

public class UndoRedo {
	// fields
	private final IModelProxy modelProxy;
	private final Deque<ICommand> undoStack;
	private final Deque<ICommand> redoStack;

	// construction
	public UndoRedo(IModelProxy modelProxy) {
		this.modelProxy = modelProxy;
		this.undoStack = new ArrayDeque<ICommand>();
		this.redoStack = new ArrayDeque<ICommand>();
		
		modelProxy.addListener(new ModelCommandListener());
	}
	
	// methods
	public boolean undo() {
		if (undoStack.isEmpty())
			return false;
		
		modelProxy.command(undoStack.peekLast());
		return true;
	}
	public boolean redo() {
		if (redoStack.isEmpty())
			return false;

		modelProxy.command(redoStack.peekLast());
		return true;
	}
	
	// helper
	private class ModelCommandListener extends ModelListenerAdapter {
		// command
		@Override
		public void onCommandSucceeded(ICommand command, ICommand undoCommand) {
			if (undoCommand != ICommand.IRREVERSIBLE) {
				if (undoCommand.isUndo()) {
					undoStack.addLast(undoCommand);
					
					// the command could have been sent from a different client
					// and that's why peekLast() is used
					if (command.equals(redoStack.peekLast()))
						redoStack.removeLast();
					else
						redoStack.clear();
				}
				else {
					redoStack.addLast(undoCommand);
					
					if (command.equals(undoStack.peekLast()))
						undoStack.removeLast();
				}
			}
		}
		@Override
		public void onCommandFailed(ICommand command) {
			if (!command.equals(undoStack.peekLast()) && !command.equals(redoStack.peekLast()))
				redoStack.addLast(command);
		}
	}
}
