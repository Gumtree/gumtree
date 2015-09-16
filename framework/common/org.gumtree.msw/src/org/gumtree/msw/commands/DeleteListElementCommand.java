package org.gumtree.msw.commands;

import org.gumtree.msw.ICommand;
import org.gumtree.msw.IModel;
import org.gumtree.msw.RefId;

public class DeleteListElementCommand extends ListElementCommand {
	// construction
	public DeleteListElementCommand(RefId id, Iterable<String> listPath, String elementName) {
		this(id, listPath, elementName, false);
	}
	public DeleteListElementCommand(RefId id, Iterable<String> listPath, String elementName, boolean isUndo) {
		super(id, listPath, elementName, isUndo);
	}
	
	// methods
	@Override
	public ICommand execute(IModel model) {
		if (!model.deleteListElement(getTargetPath(), getElementName()))
			return null;

		return new RecoverListElementCommand(getId(), getTargetPath(), getElementName(), !isUndo());
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DeleteListElementCommand)
			return super.equals(obj);
		
		return false;
	}
}
