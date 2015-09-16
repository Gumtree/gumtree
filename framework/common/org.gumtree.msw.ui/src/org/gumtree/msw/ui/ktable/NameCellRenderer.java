package org.gumtree.msw.ui.ktable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import org.gumtree.msw.ui.ktable.KTableModel;
import org.gumtree.msw.ui.ktable.renderers.TextCellRenderer;

public class NameCellRenderer extends TextCellRenderer {
	// construction
	public NameCellRenderer(int style) {
		super(style);
	}

	// methods
	@Override
    public void drawCell(GC gc, Rectangle rect, int col, int row, Object content, 
            boolean focus, boolean fixed, boolean clicked, KTableModel model) {
		
		if (model instanceof ElementTableModel<?, ?>) {
			ElementTableModel<?, ?> tableModel = (ElementTableModel<?, ?>)model;
			
			if (tableModel.getSelectedElement().getTarget() == tableModel.toElement(row)) {
				int originalStyle = getStyle();
				try {
					setStyle(originalStyle | SWT.BOLD);
					super.drawCell(gc, rect, col, row, content, focus, fixed, clicked, model);
				}
				finally {
					setStyle(originalStyle);
				}
				return;
			}
		}
		
		super.drawCell(gc, rect, col, row, content, focus, fixed, clicked, model);
	}
}
