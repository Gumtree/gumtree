package org.gumtree.msw.ui.ktable;

import org.gumtree.msw.elements.IDependencyProperty;

public interface IModelCellDefinition {
	// properties
	public IDependencyProperty getProperty();

	// methods
	public Object convertFromModel(Object value);
	public Object convertToModelValue(Object value);
}
