package org.gumtree.msw.ui.ktable.internal;

import org.eclipse.swt.widgets.Control;

import org.gumtree.msw.ui.ktable.ITableCellEditorListener;
import org.gumtree.msw.ui.ktable.KTableCellEditor;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorComboText;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorText2;

// used to bind text control to model (read-only)
public class TableCellEditorListener implements ITableCellEditorListener {
	// fields
	private final TextModifyListener textModifyListener;
	
	// construction
	public TableCellEditorListener(TextModifyListener textModifyListener) {
		this.textModifyListener = textModifyListener;
	}
	
	// methods
	@Override
	public void onOpen(Control control) {
		textModifyListener.open(control);
	}
	@Override
	public void onClose(Control control) {
		textModifyListener.close(control);
	}
	
	// helpers
	public static boolean isValidEditor(KTableCellEditor editor) {
    	return
    			(editor instanceof KTableCellEditorText2) ||
    			(editor instanceof KTableCellEditorComboText);
	}
}
