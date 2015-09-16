package org.gumtree.msw.ui.ktable;

import org.eclipse.swt.widgets.Control;

public interface ITableCellEditorListener {
	// methods
	void onOpen(Control control);
	void onClose(Control control);
}
