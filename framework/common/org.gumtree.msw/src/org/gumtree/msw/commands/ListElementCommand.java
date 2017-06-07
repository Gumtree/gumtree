package org.gumtree.msw.commands;

import java.util.Objects;

import org.gumtree.msw.RefId;

public abstract class ListElementCommand extends TargetCommand {
	// fields
	private final String elementName;

	// construction
	public ListElementCommand(RefId id, Iterable<String> listPath, String elementName, boolean isUndo) {
		super(id, listPath, isUndo);
		this.elementName = elementName;
	}
	
	// properties
	public String getElementName() {
		return elementName;
	}

	// methods
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ListElementCommand) {
			ListElementCommand other = (ListElementCommand)obj;
			return
					super.equals(other) &&
					Objects.equals(other.elementName, elementName);			
		}
		return false;
	}
}
