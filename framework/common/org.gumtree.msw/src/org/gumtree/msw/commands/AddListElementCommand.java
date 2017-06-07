package org.gumtree.msw.commands;

import java.util.Objects;

import org.gumtree.msw.ICommand;
import org.gumtree.msw.IModel;
import org.gumtree.msw.RefId;

public class AddListElementCommand extends ListElementCommand {
	// fields
	private final int targetIndex;
	
	// construction
	public AddListElementCommand(RefId id, Iterable<String> listPath, String elementName) {
		this(id, listPath, elementName, Integer.MAX_VALUE);
	}
	public AddListElementCommand(RefId id, Iterable<String> listPath, String elementName, int targetIndex) {
		super(id, listPath, elementName, false);
		this.targetIndex = targetIndex;
	}
	
	// properties
	public int getTargetIndex() {
		return targetIndex;
	}
	
	// methods
	@Override
	public ICommand execute(IModel model) {
		if (!model.addListElement(getTargetPath(), getElementName(), targetIndex))
			return null;
		
		return new DeleteListElementCommand(getId(), getTargetPath(), getElementName(), !isUndo());
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AddListElementCommand) {
			AddListElementCommand other = (AddListElementCommand)obj;
			return
					super.equals(obj) &&
					Objects.equals(other.targetIndex, targetIndex);
		}
		return false;
	}
}
