package org.gumtree.msw.commands;

import java.util.Objects;

import org.gumtree.msw.ICommand;
import org.gumtree.msw.IModel;
import org.gumtree.msw.RefId;

public class ChangePropertyCommand extends TargetCommand {
	// fields
	private final String propertyName;
	private final Object newValue;
	
	// construction
	public ChangePropertyCommand(RefId id, Iterable<String> elementPath, String propertyName, Object newValue) {
		this(id, elementPath, propertyName, newValue, false);
	}
	public ChangePropertyCommand(RefId id, Iterable<String> elementPath, String propertyName, Object newValue, boolean isUndo) {
		super(id, elementPath, isUndo);
		this.propertyName = propertyName;
		this.newValue = newValue;
	}
	
	// properties
	public String getPropertyName() {
		return propertyName;
	}
	public Object getNewValue() {
		return newValue;
	}

	// methods
	@Override
	public ICommand execute(IModel model) {
		Object oldValue = model.getProperty(getTargetPath(), propertyName);
		if (!model.changeProperty(getTargetPath(), propertyName, newValue))
			return null;

		return new ChangePropertyCommand(getId(), getTargetPath(), propertyName, oldValue, !isUndo());
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ChangePropertyCommand) {
			ChangePropertyCommand other = (ChangePropertyCommand)obj;
			return
					super.equals(other) &&
					Objects.equals(other.propertyName, propertyName) &&
					Objects.equals(other.newValue, newValue);			
		}
		return false;
	}
}
