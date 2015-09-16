package org.gumtree.msw.commands;

import org.gumtree.msw.ICommand;
import org.gumtree.msw.IModel;
import org.gumtree.msw.RefId;

public class RecoverListElementCommand extends ListElementCommand {
	// construction
	public RecoverListElementCommand(RefId id, Iterable<String> listPath, String elementName) {
		this(id, listPath, elementName, false);
	}
	public RecoverListElementCommand(RefId id, Iterable<String> listPath, String elementName, boolean isUndo) {
		super(id, listPath, elementName, isUndo);
	}
	
	// methods
	@Override
	public ICommand execute(IModel model) {
		if (!model.recoverListElement(getTargetPath(), getElementName()))
			return null;

		return new DeleteListElementCommand(getId(), getTargetPath(), getElementName(), !isUndo());
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RecoverListElementCommand)
			return super.equals(obj);
		
		return false;
	}
}
