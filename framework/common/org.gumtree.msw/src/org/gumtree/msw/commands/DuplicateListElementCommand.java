package org.gumtree.msw.commands;

import java.util.Objects;

import org.gumtree.msw.ICommand;
import org.gumtree.msw.IModel;
import org.gumtree.msw.RefId;

public class DuplicateListElementCommand extends ListElementCommand {
	// fields
	private final String newElementName;
	
	// construction
	public DuplicateListElementCommand(RefId id, Iterable<String> listPath, String elementName, String newElementName) {
		super(id, listPath, elementName, false);
		this.newElementName = newElementName;
	}
	
	// properties
	public String getNewElementName() {
		return newElementName;
	}
	
	// methods
	@Override
	public ICommand execute(IModel model) {
		if (!model.duplicateListElement(getTargetPath(), getElementName(), newElementName))
			return null;
		
		return new DeleteListElementCommand(getId(), getTargetPath(), newElementName, !isUndo());
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DuplicateListElementCommand) {
			DuplicateListElementCommand other = (DuplicateListElementCommand)obj;
			return
					super.equals(obj) &&
					Objects.equals(other.newElementName, newElementName);
		}
		return false;
	}
}
