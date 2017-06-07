package org.gumtree.msw.commands;

import java.util.Iterator;
import java.util.Objects;

import org.gumtree.msw.RefId;

public abstract class TargetCommand extends Command {
	// fields
	private final Iterable<String> targetPath;
	
	// construction
	protected TargetCommand(RefId id, Iterable<String> tarrgetPath, boolean isUndo) {
		super(id, isUndo);
		this.targetPath = tarrgetPath;
	}
	
	// properties
	public Iterable<String> getTargetPath() {
		return targetPath;
	}

	// methods
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TargetCommand) {
			TargetCommand other = (TargetCommand)obj;
			return
					super.equals(other) &&
					equalPaths(other.targetPath, targetPath);
		}
		return false;
	}
	
	// helper
	private static boolean equalPaths(Iterable<String> path1, Iterable<String> path2) {
		Iterator<String> iterator1 = path1.iterator();
		for (String value2 : path2)
			if (!iterator1.hasNext())
				return false;
			else if (!Objects.equals(iterator1.next(), value2))
				return false;
		
		if (iterator1.hasNext())
			return false;
		
		return true;
	}
}
