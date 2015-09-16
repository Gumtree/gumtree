package org.gumtree.msw.ui.ktable;

import org.gumtree.msw.elements.IDependencyProperty;

import org.gumtree.msw.ui.ktable.KTableCellEditor;
import org.gumtree.msw.ui.ktable.KTableCellRenderer;

public interface IModelCellDefinition {
	// properties
	public IDependencyProperty getProperty();
	public KTableCellRenderer getCellRenderer();
	public KTableCellEditor getCellEditor();

	// methods
	public Object convertFromModel(Object value);
	public Object convertToModelValue(Object value);
}
