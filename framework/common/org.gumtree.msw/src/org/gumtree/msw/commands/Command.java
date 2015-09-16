package org.gumtree.msw.commands;

import java.util.Objects;

import org.gumtree.msw.ICommand;
import org.gumtree.msw.RefId;

public abstract class Command implements ICommand {
	// fields
	private final RefId id;
	private final boolean isUndo;
	
	// construction
	protected Command(RefId id, boolean isUndo) {
		this.id = id;
		this.isUndo = isUndo;
	}
	
	// properties
	@Override
	public RefId getId() {
		return id;
	}
	@Override
	public boolean isUndo() {
		return isUndo;
	}
	
	// methods
	@Override
	public boolean equals(Object obj) {		
		if (obj instanceof Command) {
			Command other = (Command)obj;
			return Objects.equals(other.id, id);
		}
		return false;
	}
}
